-- ============================================================
-- 露营装备租赁系统 - 扩展：就近网点 + 商品两大类型（装备/食品）
-- 说明：本脚本只需执行一次（含 ALTER，不可重复执行）
-- ============================================================
USE camp_rent;

-- ---------- 1. 网点表增加备注 ----------
ALTER TABLE store ADD COLUMN remark VARCHAR(255) DEFAULT NULL COMMENT '网点备注（取货指引等）' AFTER business_hours;

-- ---------- 2. 分类表增加类型 ----------
ALTER TABLE category ADD COLUMN category_type TINYINT NOT NULL DEFAULT 1 COMMENT '分类类型：1-露营装备，2-露营食品' AFTER category_name;

-- ---------- 3. 商品表增加类型 / 规格 / 售卖价 ----------
ALTER TABLE equipment
    ADD COLUMN product_type TINYINT NOT NULL DEFAULT 1 COMMENT '商品类型：1-装备（租赁），2-食品（一次性售卖）' AFTER category_id,
    ADD COLUMN spec         VARCHAR(100)    DEFAULT NULL COMMENT '规格，如 2 人 / 320g/盒' AFTER eq_name,
    ADD COLUMN sale_price   DECIMAL(10, 2)  DEFAULT NULL COMMENT '售卖价格（食品使用，装备为 NULL）' AFTER price_per_day;

-- ---------- 4. 网点测试数据（补充 4 个网点，带经纬度与备注） ----------
INSERT INTO store (store_name, city, address, longitude, latitude, phone, business_hours, remark, status, create_time)
VALUES ('杭州西湖露营网点', '杭州', '浙江省杭州市西湖区北山街 78 号', 120.148700, 30.253600, '0571-88880003', '08:30-20:30', '西湖景区北门停车场旁，可停车', 1, NOW()),
       ('成都青城山露营网点', '成都', '四川省成都市都江堰市青城山镇环山路 12 号', 103.570300, 30.901000, '028-88880004', '08:00-19:00', '青城山景区游客中心对面', 1, NOW()),
       ('广州白云山露营网点', '广州', '广东省广州市白云区白云大道南 1 号', 113.296800, 23.179000, '020-88880005', '09:00-21:00', '白云山南门入口右侧', 1, NOW()),
       ('西安秦岭露营网点', '西安', '陕西省西安市长安区子午大道秦岭北麓', 108.940200, 34.123500, '029-88880006', '08:00-20:00', '环山路驿站内，适合自驾取货', 1, NOW());

-- 给原有 2 个网点补备注
UPDATE store SET remark = '望京 SOHO 地下车库 B1 取货点' WHERE id = 1 AND remark IS NULL;
UPDATE store SET remark = '浦东世纪公园 3 号门对面' WHERE id = 2 AND remark IS NULL;

-- ---------- 5. 新增分类 ----------
-- 装备类（category_type = 1）
INSERT INTO category (category_name, category_type, sort, create_time)
VALUES ('天幕', 1, 6, NOW()),
       ('收纳', 1, 7, NOW()),
       ('防护', 1, 8, NOW()),
       ('徒步装备', 1, 9, NOW()),
       ('防风配件', 1, 10, NOW()),
       -- 食品类（category_type = 2）
       ('自热米饭', 2, 1, NOW()),
       ('冻干食品', 2, 2, NOW()),
       ('能量棒', 2, 3, NOW()),
       ('罐装饮料', 2, 4, NOW()),
       ('便携零食', 2, 5, NOW());

-- ---------- 6. 商品测试数据 ----------
-- 6.1 装备：为原有 2 个网点各补充一批（product_type = 1，租赁）
INSERT INTO equipment (store_id, category_id, product_type, eq_name, spec, eq_img, price_per_day, deposit, stock_num, description, status, create_time)
VALUES
-- 北京望京（store_id = 1）
(1, 6, 1, '六角天幕（3×3m）', '3×3m 涂银防晒', 'https://cbu01.alicdn.com/img/ibank/O1CN01LN1aX929BnBmCVGIk_!!3446478030-0-cib.jpg', 28.00, 120.00, 8, '涂银防晒，遮阳防雨，适合 3-4 人野餐', 1, NOW()),
(1, 7, 1, '折叠收纳箱 50L', '50L 可折叠', 'https://cbu01.alicdn.com/img/ibank/2018/274/358/9394853472_249430366.jpg', 8.00, 40.00, 20, '牛津布加厚，可折叠收纳，承重 30kg', 1, NOW()),
(1, 8, 1, '防潮地垫', '200×200cm', 'https://cbu01.alicdn.com/img/ibank/2018/713/266/9640662317_249430366.jpg', 6.00, 30.00, 25, '加厚铝膜防潮垫，隔湿隔凉', 1, NOW()),
(1, 9, 1, '登山杖（一对）', '铝合金 可伸缩', 'https://cbu01.alicdn.com/img/ibank/O1CN01v8KwSv28QcOBkBwyZ_!!2213222767927-0-cib.jpg', 10.00, 50.00, 15, '航空铝材，三节伸缩，带避震', 1, NOW()),
(1, 10, 1, '帐篷防风绳套装', '8 根装', 'https://cbu01.alicdn.com/img/ibank/O1CN01hJ925u1wY9Xd2zQvb_!!2214028566319-0-cib.jpg', 3.00, 15.00, 30, '反光防风绳 + 地钉 8 件套', 1, NOW()),
(1, 1, 1, '单人多功能帐篷', '1-2 人 速开', 'https://cbu01.alicdn.com/img/ibank/O1CN01vkpmDa1G9gYvg4E0t_!!2213145640580-0-cib.jpg', 32.00, 180.00, 10, '速开结构，防雨防晒，附收纳袋', 1, NOW()),
-- 上海浦东（store_id = 2）
(2, 6, 1, '蝶形天幕（4×4m）', '4×4m 蝶形', 'https://cbu01.alicdn.com/img/ibank/O1CN01LN1aX929BnBmCVGIk_!!3446478030-0-cib.jpg', 35.00, 150.00, 6, '蝶形大尺寸，遮阳面积大，适合家庭', 1, NOW()),
(2, 7, 1, '户外折叠收纳包', '40L', 'https://cbu01.alicdn.com/img/ibank/2018/274/358/9394853472_249430366.jpg', 7.00, 35.00, 18, '防水耐磨，可装锅具餐具', 1, NOW()),
(2, 8, 1, '防晒天幕围布', '2×2m', 'https://cbu01.alicdn.com/img/ibank/2018/713/266/9640662317_249430366.jpg', 5.00, 25.00, 20, 'UPF50+ 防晒围布，可作侧挡风', 1, NOW()),
(2, 9, 1, '徒步双肩包 35L', '35L 背负系统', 'https://cbu01.alicdn.com/img/ibank/O1CN01v8KwSv28QcOBkBwyZ_!!2213222767927-0-cib.jpg', 15.00, 80.00, 12, '透气背负，适合 1-2 日徒步', 1, NOW()),
(2, 10, 1, '天幕支撑杆', '2.4m 铝合金', 'https://cbu01.alicdn.com/img/ibank/O1CN01hJ925u1wY9Xd2zQvb_!!2214028566319-0-cib.jpg', 4.00, 20.00, 24, '加粗铝合金支撑杆，可调高度', 1, NOW()),
(2, 3, 1, '蛋卷桌（120cm）', '120×60cm', 'https://cbu01.alicdn.com/img/ibank/2018/037/390/8974093730_1474175813.jpg', 18.00, 90.00, 10, '铝合金蛋卷桌面，可折叠收纳', 1, NOW());

-- 6.2 食品：一次性售卖（product_type = 2，sale_price 为售价）
INSERT INTO equipment (store_id, category_id, product_type, eq_name, spec, eq_img, price_per_day, deposit, stock_num, description, status, create_time)
VALUES
-- 北京望京
(1, 11, 2, '自热米饭（红烧牛肉）', '320g/盒', '/img/food/rice.svg', 0.00, 0.00, 60, '开袋自热，15 分钟即食，出行应急主食', 1, NOW()),
(1, 12, 2, '冻干粥（皮蛋瘦肉）', '60g/袋', '/img/food/freeze.svg', 0.00, 0.00, 80, '冻干锁鲜，冲泡即食，轻便好携带', 1, NOW()),
(1, 13, 2, '能量棒（坚果巧克力）', '35g/根', '/img/food/bar.svg', 0.00, 0.00, 120, '高热量补给，徒步途中快速补充体力', 1, NOW()),
(1, 14, 2, '罐装运动饮料', '330ml/罐', '/img/food/drink.svg', 0.00, 0.00, 100, '补充电解质，冰镇更爽口', 1, NOW()),
(1, 15, 2, '便携零食组合', '混合装 200g', '/img/food/snack.svg', 0.00, 0.00, 90, '果干 + 坚果 + 饼干组合，露营休闲必备', 1, NOW()),
-- 上海浦东
(2, 11, 2, '自热米饭（菌菇鸡丁）', '320g/盒', '/img/food/rice.svg', 0.00, 0.00, 50, '开袋自热，15 分钟即食，出行应急主食', 1, NOW()),
(2, 12, 2, '冻干水果', '30g/袋', '/img/food/freeze.svg', 0.00, 0.00, 70, '冻干工艺保留果香，轻便零食', 1, NOW()),
(2, 13, 2, '能量胶', '40g/支', '/img/food/bar.svg', 0.00, 0.00, 110, '快速供能，适合长距离徒步', 1, NOW()),
(2, 14, 2, '罐装气泡水', '330ml/罐', '/img/food/drink.svg', 0.00, 0.00, 120, '0 糖 0 卡，解腻清爽', 1, NOW()),
(2, 15, 2, '便携肉干', '100g/袋', '/img/food/snack.svg', 0.00, 0.00, 80, '高蛋白肉干，补充能量', 1, NOW());

-- 食品补售价（示例：按分类统一价）
UPDATE equipment SET sale_price = 26.00 WHERE product_type = 2 AND category_id = 11;
UPDATE equipment SET sale_price = 12.00 WHERE product_type = 2 AND category_id = 12;
UPDATE equipment SET sale_price = 9.00  WHERE product_type = 2 AND category_id = 13;
UPDATE equipment SET sale_price = 6.00  WHERE product_type = 2 AND category_id = 14;
UPDATE equipment SET sale_price = 15.00 WHERE product_type = 2 AND category_id = 15;

-- ---------- 7. 校验 ----------
SELECT product_type, COUNT(*) AS cnt FROM equipment GROUP BY product_type;
SELECT id, category_name, category_type, sort FROM category ORDER BY category_type, sort;
SELECT id, store_name, city, longitude, latitude, remark FROM store ORDER BY id;
