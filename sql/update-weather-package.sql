-- ============================================================
-- 露营装备租赁系统 - 天气 AI 套餐推荐功能：建表 + 初始化套餐数据
-- 说明：本脚本可重复执行（先删表再建表、先清空再插入）
-- 执行：mysql -uroot -p373394589 --default-character-set=utf8mb4 < 本文件
-- ============================================================
USE camp_rent;

-- ----------------------------
-- 1. 套餐表
-- ----------------------------
DROP TABLE IF EXISTS camp_package_item;
DROP TABLE IF EXISTS camp_package;

CREATE TABLE camp_package (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    package_name    VARCHAR(100) NOT NULL COMMENT '套餐名称',
    scene_type      VARCHAR(20)  NOT NULL COMMENT '适用场景：露营/徒步',
    cover_img       VARCHAR(500)          DEFAULT NULL COMMENT '套餐封面图',
    description     VARCHAR(500)          DEFAULT NULL COMMENT '套餐描述',
    price           DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '套餐日租价（元/天）',
    original_price  DECIMAL(10, 2)         DEFAULT NULL COMMENT '原价（元/天）',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='露营套餐表';

-- ----------------------------
-- 2. 套餐装备明细表
-- ----------------------------
CREATE TABLE camp_package_item (
    id            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    package_id    BIGINT   NOT NULL COMMENT '套餐ID',
    equipment_id  BIGINT   NOT NULL COMMENT '装备ID',
    quantity      INT      NOT NULL DEFAULT 1 COMMENT '装备数量',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_package_id (package_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='套餐装备明细表';

-- ----------------------------
-- 3. 初始化套餐数据
--    提示：一个套餐内的装备应属于同一个网点，否则无法在该网点一键下单
-- ----------------------------
INSERT INTO camp_package (id, package_name, scene_type, cover_img, description, price, original_price, status)
VALUES (1, '精致露营双人套餐', '露营',
        'https://cbu01.alicdn.com/img/ibank/O1CN01HjEJN91Kcm9I156aQ_!!2214580821185-0-cib.jpg',
        '帐篷+折叠桌椅+卡式炉+营地灯，一次搞定双人精致露营，适合近郊营地与公园露营。',
        89.00, 99.00, 1),
       (2, '轻装徒步套餐', '徒步',
        'https://cbu01.alicdn.com/img/ibank/2019/175/452/12444254571_6159724.jpg',
        '睡袋+卡式炉+营地灯，轻量三件套，适合一日徒步与轻量山野露营。',
        49.00, 55.00, 1),
       (3, '家庭露营套餐', '露营',
        'https://cbu01.alicdn.com/img/ibank/2018/713/266/9640662317_249430366.jpg',
        '大帐篷+烧烤架+营地灯串，适合家庭亲子露营与朋友聚会。',
        79.00, 86.00, 1),
       (4, '山野徒步套餐', '徒步',
        'https://cbu01.alicdn.com/img/ibank/O1CN01HBVx2L1MTjZzCjsbf_!!2212958251436-0-cib.jpg',
        '帐篷+棉质睡袋+营地灯串，适合2-3天中距离徒步与山野过夜。',
        62.00, 69.00, 1);

-- 套餐1：精致露营双人套餐（网点1：id=1 双人自动帐篷、4 折叠桌椅套装、5 便携卡式炉、6 LED露营灯）
INSERT INTO camp_package_item (package_id, equipment_id, quantity)
VALUES (1, 1, 1),
       (1, 4, 1),
       (1, 5, 1),
       (1, 6, 1);

-- 套餐2：轻装徒步套餐（网点1：id=3 羽绒睡袋、5 便携卡式炉、6 LED露营灯）
INSERT INTO camp_package_item (package_id, equipment_id, quantity)
VALUES (2, 3, 1),
       (2, 5, 1),
       (2, 6, 1);

-- 套餐3：家庭露营套餐（网点2：id=7 双人自动帐篷、9 户外烧烤架、10 营地灯串）
INSERT INTO camp_package_item (package_id, equipment_id, quantity)
VALUES (3, 7, 1),
       (3, 9, 1),
       (3, 10, 1);

-- 套餐4：山野徒步套餐（网点2：id=8 棉质睡袋、7 双人自动帐篷、10 营地灯串）
INSERT INTO camp_package_item (package_id, equipment_id, quantity)
VALUES (4, 8, 1),
       (4, 7, 1),
       (4, 10, 1);

-- 校验
SELECT p.id, p.package_name, p.scene_type, p.price, COUNT(i.id) AS item_count
FROM camp_package p
         LEFT JOIN camp_package_item i ON i.package_id = p.id
GROUP BY p.id, p.package_name, p.scene_type, p.price
ORDER BY p.id;
