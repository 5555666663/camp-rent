-- ============================================================
-- 迭代优化增量脚本 V1.0
-- 覆盖：支付/押金、定损、到期提醒、时段冲突、食品保质期、
--       个性化推荐、推荐规则、仓储角色、审计日志
-- 可重复执行（表 IF NOT EXISTS，列通过存储过程判断）
-- ============================================================
USE camp_rent;

-- ---------- 0. 列增量工具 ----------
DROP PROCEDURE IF EXISTS add_col_if_missing;
DELIMITER $$
CREATE PROCEDURE add_col_if_missing(IN tbl VARCHAR(64), IN col VARCHAR(64), IN ddl VARCHAR(500))
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col) THEN
    SET @s = CONCAT('ALTER TABLE ', tbl, ' ADD COLUMN ', ddl);
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL add_col_if_missing('orders', 'checkin_time', 'checkin_time DATETIME NULL COMMENT ''实际取件时间'' AFTER order_status');
CALL add_col_if_missing('orders', 'checkout_time', 'checkout_time DATETIME NULL COMMENT ''实际归还时间'' AFTER checkin_time');
CALL add_col_if_missing('orders', 'cancel_reason', 'cancel_reason VARCHAR(200) NULL COMMENT ''取消原因'' AFTER remark');

CALL add_col_if_missing('deposit_record', 'frozen_time', 'frozen_time DATETIME NULL COMMENT ''押金冻结时间'' AFTER deposit_status');
CALL add_col_if_missing('deposit_record', 'refund_amount', 'refund_amount DECIMAL(10,2) NULL DEFAULT 0.00 COMMENT ''实退金额'' AFTER frozen_time');
CALL add_col_if_missing('deposit_record', 'deduct_amount', 'deduct_amount DECIMAL(10,2) NULL DEFAULT 0.00 COMMENT ''扣除金额'' AFTER refund_amount');

CALL add_col_if_missing('equipment', 'shelf_life_days', 'shelf_life_days INT NULL COMMENT ''保质期天数（食品）'' AFTER sale_price');
CALL add_col_if_missing('equipment', 'min_stock', 'min_stock INT NULL DEFAULT 10 COMMENT ''库存预警阈值'' AFTER stock_num');

-- ---------- 1. 支付单 ----------
CREATE TABLE IF NOT EXISTS payment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  pay_no VARCHAR(64) NOT NULL COMMENT '支付单号（幂等）',
  channel VARCHAR(32) NOT NULL DEFAULT 'mock' COMMENT '渠道：alipay_sandbox/wechat_sandbox/mock',
  pay_type TINYINT NOT NULL DEFAULT 1 COMMENT '1押金 2租金',
  amount DECIMAL(10,2) NOT NULL COMMENT '金额',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1支付成功 2已退款 3退款失败',
  trade_no VARCHAR(64) NULL COMMENT '第三方流水号',
  pay_time DATETIME NULL COMMENT '支付时间',
  refund_time DATETIME NULL COMMENT '退款时间',
  raw_json TEXT NULL COMMENT '回调原文',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_pay_no (pay_no),
  KEY idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付单';

-- ---------- 2. 定损记录 ----------
CREATE TABLE IF NOT EXISTS damage_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  equipment_id BIGINT NOT NULL,
  img_urls VARCHAR(1000) NULL COMMENT '照片地址，逗号分隔（1~9张）',
  damage_level TINYINT NOT NULL DEFAULT 0 COMMENT '0无 1轻微 2中度 3严重',
  compensate_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '赔偿金额',
  remark VARCHAR(255) NULL,
  operator VARCHAR(50) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归还定损记录';

-- ---------- 3. 站内消息 ----------
CREATE TABLE IF NOT EXISTS message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  type TINYINT NOT NULL DEFAULT 3 COMMENT '1到期提醒 2逾期提醒 3系统通知 4售后',
  title VARCHAR(100) NOT NULL,
  content VARCHAR(500) NULL,
  biz_id BIGINT NULL COMMENT '关联业务ID（如订单ID）',
  read_flag TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_msg (user_id, type, biz_id),
  KEY idx_user (user_id, read_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息';

-- ---------- 4. 食品批次 ----------
CREATE TABLE IF NOT EXISTS food_batch (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  equipment_id BIGINT NOT NULL,
  store_id BIGINT NOT NULL,
  batch_no VARCHAR(64) NOT NULL,
  production_date DATE NULL,
  expire_date DATE NOT NULL,
  quantity INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0正常 1临期 2过期 3售罄',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_eq (equipment_id, expire_date),
  KEY idx_store (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食品批次（保质期）';

-- ---------- 5. 用户偏好缓存 ----------
CREATE TABLE IF NOT EXISTS user_preference (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  pref_json TEXT NULL,
  summary VARCHAR(500) NULL COMMENT '偏好摘要文本',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expire_time DATETIME NULL,
  UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户推荐偏好缓存';

-- ---------- 6. 推荐规则 ----------
CREATE TABLE IF NOT EXISTS recommend_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_name VARCHAR(100) NOT NULL,
  priority INT NOT NULL DEFAULT 100 COMMENT '数值越小优先级越高',
  condition_json VARCHAR(1000) NOT NULL COMMENT '条件JSON',
  action_json VARCHAR(1000) NOT NULL COMMENT '动作JSON',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  version INT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_enabled (enabled, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐规则（可视化配置）';

-- ---------- 7. 操作审计日志 ----------
CREATE TABLE IF NOT EXISTS sys_oper_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  username VARCHAR(50) NULL,
  role_code VARCHAR(50) NULL,
  module VARCHAR(50) NULL COMMENT '模块',
  action VARCHAR(100) NULL COMMENT '操作',
  method VARCHAR(10) NULL,
  uri VARCHAR(255) NULL,
  params TEXT NULL COMMENT '请求参数（脱敏）',
  result_code INT NULL,
  error_msg VARCHAR(500) NULL,
  cost_ms BIGINT NULL,
  ip VARCHAR(64) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_user (user_id),
  KEY idx_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志';

-- ---------- 8. 仓储角色 + 账号 ----------
INSERT INTO role (role_code, role_name, description)
SELECT 'ROLE_STORAGE', '仓储人员', '仅拥有装备入库、批次与维保权限'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_code = 'ROLE_STORAGE');

INSERT INTO user (username, password, real_name, phone, role, status, create_time)
SELECT 'cangchu', 'e10adc3949ba59abbe56e057f20f883e', '仓储小张', '13800000005', 0, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'cangchu');

INSERT INTO user_role (user_id, role_id, create_time)
SELECT u.id, r.id, NOW() FROM user u, role r
WHERE u.username = 'cangchu' AND r.role_code = 'ROLE_STORAGE'
  AND NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

-- ---------- 9. 默认推荐规则 ----------
INSERT INTO recommend_rule (rule_name, priority, condition_json, action_json, enabled, remark)
SELECT '雷电或强降水-高危',
       10,
       '{"thunder":{"eq":true},"precipitationProbability":{"gte":70}}',
       '{"riskLevel":"high","sceneType":"hiking","riskTip":"存在雷电/强降水风险，不建议户外露营","remindText":"建议改期；如必须出行，请避开高地、水域与孤立树木"}',
       1, '命中即高危，改为徒步并提示改期'
WHERE NOT EXISTS (SELECT 1 FROM recommend_rule WHERE rule_name = '雷电或强降水-高危');

INSERT INTO recommend_rule (rule_name, priority, condition_json, action_json, enabled, remark)
SELECT '大风-注意',
       20,
       '{"windSpeed":{"gte":40}}',
       '{"riskLevel":"medium","sceneType":"hiking","riskTip":"风力较大，注意帐篷固定与用火安全","remindText":"建议携带地钉与防风绳，避免空旷处扎营"}',
       1, '风力≥40km/h'
WHERE NOT EXISTS (SELECT 1 FROM recommend_rule WHERE rule_name = '大风-注意');

INSERT INTO recommend_rule (rule_name, priority, condition_json, action_json, enabled, remark)
SELECT '降雨-注意',
       30,
       '{"willRain":{"eq":true}}',
       '{"riskLevel":"medium","sceneType":"hiking","riskTip":"有降雨，注意防雨防潮","remindText":"建议携带雨具与防水地布，装备注意收纳防潮"}',
       1, '有降水'
WHERE NOT EXISTS (SELECT 1 FROM recommend_rule WHERE rule_name = '降雨-注意');

INSERT INTO recommend_rule (rule_name, priority, condition_json, action_json, enabled, remark)
SELECT '强紫外线-注意',
       40,
       '{"uvIndex":{"gte":8}}',
       '{"riskLevel":"medium","sceneType":"camping","riskTip":"紫外线较强，注意防晒","remindText":"建议携带防晒霜、遮阳帽与墨镜"}',
       1, 'UV≥8'
WHERE NOT EXISTS (SELECT 1 FROM recommend_rule WHERE rule_name = '强紫外线-注意');

INSERT INTO recommend_rule (rule_name, priority, condition_json, action_json, enabled, remark)
SELECT '高温-注意',
       50,
       '{"tempMax":{"gte":33}}',
       '{"riskLevel":"medium","sceneType":"hiking","riskTip":"气温较高，注意防暑降温","remindText":"建议避开正午时段，多带饮用水与电解质饮料"}',
       1, '最高温≥33℃'
WHERE NOT EXISTS (SELECT 1 FROM recommend_rule WHERE rule_name = '高温-注意');

-- ---------- 10. 食品批次种子数据（为现有食品建批次） ----------
INSERT INTO food_batch (equipment_id, store_id, batch_no, production_date, expire_date, quantity, status)
SELECT e.id, e.store_id,
       CONCAT('FB', DATE_FORMAT(NOW(), '%Y%m'), '-', e.id),
       DATE_SUB(CURDATE(), INTERVAL 10 DAY),
       DATE_ADD(CURDATE(), INTERVAL 90 DAY),
       e.stock_num, 0
FROM equipment e
WHERE e.product_type = 2
  AND NOT EXISTS (SELECT 1 FROM food_batch b WHERE b.equipment_id = e.id);

UPDATE equipment SET shelf_life_days = 90 WHERE product_type = 2 AND shelf_life_days IS NULL;

-- ---------- 校验 ----------
SELECT 'payment' t, COUNT(*) c FROM payment
UNION ALL SELECT 'damage_record', COUNT(*) FROM damage_record
UNION ALL SELECT 'message', COUNT(*) FROM message
UNION ALL SELECT 'food_batch', COUNT(*) FROM food_batch
UNION ALL SELECT 'recommend_rule', COUNT(*) FROM recommend_rule
UNION ALL SELECT 'sys_oper_log', COUNT(*) FROM sys_oper_log
UNION ALL SELECT 'role', COUNT(*) FROM role
UNION ALL SELECT 'user', COUNT(*) FROM user;
