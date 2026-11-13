-- ============================================================
-- 基于 SpringBoot + Vue3 的多网点露营装备租赁系统
-- 数据库初始化脚本
-- 数据库名：camp_rent
-- 字符集：utf8mb4  排序规则：utf8mb4_unicode_ci
-- MySQL 版本：8.0
-- 说明：脚本包含 8 张业务表 + 基础测试数据
--       管理员默认账号：admin / 123456（密码为 MD5 加密存储）
-- ============================================================

-- 创建数据库（若不存在）
CREATE DATABASE IF NOT EXISTS `camp_rent`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `camp_rent`;

-- 为保证可重复执行，先按依赖顺序删除旧表
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `equipment_maintain`;
DROP TABLE IF EXISTS `deposit_record`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `equipment`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `store`;
DROP TABLE IF EXISTS `user`;

-- ============================================================
-- 1. user 用户表
--    role：0-普通用户，1-管理员
--    status：0-禁用，1-正常
-- ============================================================
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`    VARCHAR(50)  NOT NULL COMMENT '用户名(登录账号)',
  `password`    VARCHAR(100) NOT NULL COMMENT '密码(MD5加密)',
  `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  `role`        TINYINT      NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户，1-管理员',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表';

-- ============================================================
-- 2. store 网点表
--    status：0-停用，1-启用
--    longitude/latitude：高德地图经纬度（GCJ-02）
-- ============================================================
CREATE TABLE `store` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_name`     VARCHAR(100)  NOT NULL COMMENT '网点名称',
  `city`           VARCHAR(50)   NOT NULL COMMENT '所在城市',
  `address`        VARCHAR(255)  NOT NULL COMMENT '详细地址',
  `longitude`      DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  `latitude`       DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  `phone`          VARCHAR(20)   DEFAULT NULL COMMENT '联系电话',
  `business_hours` VARCHAR(100)  DEFAULT NULL COMMENT '营业时间',
  `status`         TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `create_time`    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '网点表';

-- ============================================================
-- 3. category 装备分类表
--    sort：分类排序（数值越小越靠前）
-- ============================================================
CREATE TABLE `category` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_name` VARCHAR(50)  NOT NULL COMMENT '分类名称',
  `sort`          INT          NOT NULL DEFAULT 0 COMMENT '排序值(越小越靠前)',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '装备分类表';

-- ============================================================
-- 4. equipment 露营装备表
--    store_id：所属网点（库存按网点独立隔离）
--    category_id：所属分类
--    status：0-下架，1-上架
-- ============================================================
CREATE TABLE `equipment` (
  `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_id`      BIGINT         NOT NULL COMMENT '所属网点ID',
  `category_id`   BIGINT         NOT NULL COMMENT '所属分类ID',
  `eq_name`       VARCHAR(100)   NOT NULL COMMENT '装备名称',
  `eq_img`        VARCHAR(255)   DEFAULT NULL COMMENT '装备图片地址',
  `price_per_day` DECIMAL(10,2)  NOT NULL COMMENT '日租金(元/天)',
  `deposit`       DECIMAL(10,2)  NOT NULL DEFAULT 0 COMMENT '押金(元)',
  `stock_num`     INT            NOT NULL DEFAULT 0 COMMENT '库存数量',
  `description`   VARCHAR(500)   DEFAULT NULL COMMENT '装备描述',
  `status`        TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `create_time`   DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_store` (`store_id`),
  KEY `idx_category` (`category_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '露营装备表';

-- ============================================================
-- 5. orders 租赁订单表
--    说明：order 为 MySQL 保留字，故表名使用 orders
--    equipment_id：租赁的装备（下单/归还需针对该装备扣减/恢复库存）
--    pick_store_id：取件网点（下单时扣减该网点库存）
--    return_store_id：归还网点（归还确认时恢复该网点库存）
--    order_status：0-待支付，1-租赁中，2-待归还，3-已完成，4-逾期
-- ============================================================
CREATE TABLE `orders` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`         BIGINT        NOT NULL COMMENT '下单用户ID',
  `equipment_id`    BIGINT        NOT NULL COMMENT '租赁装备ID',
  `quantity`        INT           NOT NULL DEFAULT 1 COMMENT '租赁数量',
  `pick_store_id`   BIGINT        NOT NULL COMMENT '取件网点ID',
  `return_store_id` BIGINT        NOT NULL COMMENT '归还网点ID',
  `rent_start_time` DATETIME      NOT NULL COMMENT '租赁开始时间',
  `rent_end_time`   DATETIME      NOT NULL COMMENT '租赁结束时间',
  `total_price`     DECIMAL(10,2) NOT NULL COMMENT '总租金(元)',
  `deposit_money`   DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '押金(元)',
  `order_status`    TINYINT       NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付,1-租赁中,2-待归还,3-已完成,4-逾期',
  `remark`          VARCHAR(255)  DEFAULT NULL COMMENT '订单备注',
  `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_equipment` (`equipment_id`),
  KEY `idx_pick_store` (`pick_store_id`),
  KEY `idx_return_store` (`return_store_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租赁订单表';

-- ============================================================
-- 6. deposit_record 押金记录表
--    deposit_status：0-未支付，1-已支付，2-已退还，3-已扣除
-- ============================================================
CREATE TABLE `deposit_record` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id`       BIGINT        NOT NULL COMMENT '关联订单ID',
  `deposit_money`  DECIMAL(10,2) NOT NULL COMMENT '押金金额(元)',
  `deposit_status` TINYINT       NOT NULL DEFAULT 0 COMMENT '押金状态：0-未支付,1-已支付,2-已退还,3-已扣除',
  `remark`         VARCHAR(255)  DEFAULT NULL COMMENT '备注',
  `create_time`    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '押金记录表';

-- ============================================================
-- 7. equipment_maintain 装备维保表
--    maintain_type：维修 / 保养 / 报废
-- ============================================================
CREATE TABLE `equipment_maintain` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_id`  BIGINT       NOT NULL COMMENT '装备ID',
  `store_id`      BIGINT       NOT NULL COMMENT '所属网点ID',
  `maintain_type` VARCHAR(20)  NOT NULL COMMENT '维保类型：维修/保养/报废',
  `content`       VARCHAR(500) DEFAULT NULL COMMENT '维保内容',
  `maintain_time` DATETIME     DEFAULT NULL COMMENT '维保时间',
  `operator`      VARCHAR(50)  DEFAULT NULL COMMENT '操作人',
  `remark`        VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_equipment` (`equipment_id`),
  KEY `idx_store` (`store_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '装备维保表';

-- ============================================================
-- 8. comment 评价反馈表
--    score：评分 1~5
-- ============================================================
CREATE TABLE `comment` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id`    BIGINT       NOT NULL COMMENT '关联订单ID',
  `user_id`     BIGINT       NOT NULL COMMENT '评价用户ID',
  `store_id`    BIGINT       NOT NULL COMMENT '关联网点ID',
  `score`       TINYINT      NOT NULL DEFAULT 5 COMMENT '评分(1~5)',
  `content`     VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_store` (`store_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评价反馈表';

-- ============================================================
-- 基础测试数据
-- ============================================================

-- 1. 用户数据（密码均为 123456 的 MD5：e10adc3949ba59abbe56e057f20f883e）
INSERT INTO `user` (`id`, `username`, `password`, `real_name`, `phone`, `avatar`, `role`, `status`) VALUES
(1, 'admin',    'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13800000001', NULL, 1, 1),
(2, 'zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '张三',       '13800000002', NULL, 0, 1),
(3, 'lisi',     'e10adc3949ba59abbe56e057f20f883e', '李四',       '13800000003', NULL, 0, 1);

-- 2. 网点数据
INSERT INTO `store` (`id`, `store_name`, `city`, `address`, `longitude`, `latitude`, `phone`, `business_hours`, `status`) VALUES
(1, '北京望京露营网点', '北京', '北京市朝阳区望京街道阜通东大街6号', 116.481500, 39.990500, '010-88880001', '08:00-20:00', 1),
(2, '上海浦东露营网点', '上海', '上海市浦东新区张杨路100号', 121.514500, 31.235000, '021-88880002', '09:00-21:00', 1);

-- 3. 装备分类数据
INSERT INTO `category` (`id`, `category_name`, `sort`) VALUES
(1, '帐篷',   1),
(2, '睡袋',   2),
(3, '桌椅',   3),
(4, '炊具',   4),
(5, '照明',   5);

-- 4. 装备数据（部分装备，两个网点各若干）
INSERT INTO `equipment` (`id`, `store_id`, `category_id`, `eq_name`, `eq_img`, `price_per_day`, `deposit`, `stock_num`, `description`, `status`) VALUES
(1, 1, 1, '双人自动帐篷',   'https://upload.wikimedia.org/wikipedia/commons/thumb/7/71/Ergaki%2C_Camping_tent_in_the_wild%2C_Sayan_Mountains%2C_Siberia%2C_Russia.jpg/960px-Ergaki%2C_Camping_tent_in_the_wild%2C_Sayan_Mountains%2C_Siberia%2C_Russia.jpg', 39.00,  200.00, 10, '轻便双人帐篷，自动速开，防风防雨', 1),
(2, 1, 1, '四人家庭帐篷',   'https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Alpine_Tent_Camping%2C_Olympic_National_Forest_%2823896632615%29.jpg/960px-Alpine_Tent_Camping%2C_Olympic_National_Forest_%2823896632615%29.jpg', 69.00,  300.00, 5,  '宽敞四人帐篷，适合家庭露营', 1),
(3, 1, 2, '羽绒睡袋',       'https://upload.wikimedia.org/wikipedia/commons/thumb/2/2e/Mummy_bag.jpg/960px-Mummy_bag.jpg', 25.00,  100.00, 20, '保暖羽绒睡袋，适用0℃环境', 1),
(4, 1, 3, '折叠桌椅套装',   'https://upload.wikimedia.org/wikipedia/commons/thumb/5/51/Helinox_Ground_Chair_1.jpg/960px-Helinox_Ground_Chair_1.jpg', 30.00,  150.00, 8,  '一桌四椅，便携折叠', 1),
(5, 1, 4, '便携卡式炉',     'https://upload.wikimedia.org/wikipedia/commons/thumb/1/19/Family_campsite_in_Alaska.jpg/960px-Family_campsite_in_Alaska.jpg', 20.00,  120.00, 15, '便携燃气灶，户外烹饪', 1),
(6, 1, 5, 'LED露营灯',      'https://upload.wikimedia.org/wikipedia/commons/d/d2/Night_scene%2C_illuminated_tents_in_woods%2C_campfire.jpg', 10.00,  50.00,  30, '充电式LED露营灯，续航8小时', 1),
(7, 2, 1, '双人自动帐篷',   'https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/White_and_yellow_camping_tent_in_the_middle_of_forest.jpg/960px-White_and_yellow_camping_tent_in_the_middle_of_forest.jpg', 39.00,  200.00, 12, '轻便双人帐篷，自动速开，防风防雨', 1),
(8, 2, 2, '棉质睡袋',       'https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/OutDoor_2018%2C_Friedrichshafen_%281X7A0298%29.jpg/960px-OutDoor_2018%2C_Friedrichshafen_%281X7A0298%29.jpg', 18.00,  80.00,  15, '舒适棉质睡袋，适合春夏', 1),
(9, 2, 4, '户外烧烤架',     'https://upload.wikimedia.org/wikipedia/commons/thumb/1/16/Rv_campsite_hoh_camground_camping_picnic_table_d_archuleta_march_05_2015_%2817185635739%29.jpg/960px-Rv_campsite_hoh_camground_camping_picnic_table_d_archuleta_march_05_2015_%2817185635739%29.jpg', 35.00,  200.00, 6,  '便携折叠烧烤架，附烤网', 1),
(10, 2, 5, '营地灯串',      'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Campers_and_tent_underneath_a_night_sky_%2849950611437%29.jpg/960px-Campers_and_tent_underneath_a_night_sky_%2849950611437%29.jpg', 12.00,  60.00,  20, '氛围灯串，防水设计', 1);

-- 5. 订单测试数据（示例：一条已完成订单，便于演示评价功能）
INSERT INTO `orders` (`id`, `user_id`, `equipment_id`, `quantity`, `pick_store_id`, `return_store_id`, `rent_start_time`, `rent_end_time`, `total_price`, `deposit_money`, `order_status`, `create_time`) VALUES
(1, 2, 1, 1, 1, 1, '2026-11-01 09:00:00', '2026-11-03 09:00:00', 78.00, 200.00, 3, '2026-10-30 10:00:00');

-- 6. 押金记录数据
INSERT INTO `deposit_record` (`id`, `order_id`, `deposit_money`, `deposit_status`, `remark`, `create_time`) VALUES
(1, 1, 200.00, 2, '订单完成，押金已退还', '2026-11-03 10:00:00');

-- 7. 评价数据
INSERT INTO `comment` (`id`, `order_id`, `user_id`, `store_id`, `score`, `content`, `create_time`) VALUES
(1, 1, 2, 1, 5, '装备很新，取还方便，好评！', '2026-11-03 11:00:00');
