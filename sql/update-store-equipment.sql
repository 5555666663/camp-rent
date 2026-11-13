-- ============================================================
-- 1) 新增「襄阳理工学院」网点
-- 2) 给所有网点补充装备（杭州西湖 / 成都青城山 / 广州白云山 / 西安秦岭 / 襄阳）
-- 图片复用已验证可访问的淘宝(1688)商品图，按装备品类一一对应
-- 可重复执行（已存在同名装备时先按 store_id + eq_name 去重）
-- ============================================================
USE camp_rent;

-- ---------- 1. 新增襄阳网点 ----------
INSERT INTO store (store_name, city, address, longitude, latitude, phone, business_hours, remark, status, create_time)
SELECT '襄阳理工学院露营网点', '襄阳', '湖北省襄阳市襄城区隆中路 湖北文理学院理工学院', 112.128700, 32.008900, '0710-8888001', '08:00-20:00', '理工学院东门停车场旁，学生取还装备方便', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM store WHERE store_name = '襄阳理工学院露营网点') t);

SET @xy = (SELECT id FROM store WHERE store_name = '襄阳理工学院露营网点' LIMIT 1);

-- ---------- 2. 杭州西湖（store 3）----------
INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 3, 1, '牧高笛双人自动帐篷', '牧高笛', '2 人 / 速开 / 210×150cm',
 'https://cbu01.alicdn.com/img/ibank/O1CN01HjEJN91Kcm9I156aQ_!!2214580821185-0-cib.jpg',
 39.00, 200.00, 10, '牧高笛经典速开双人帐篷，展开即用、无需繁琐搭杆。双层结构配 3000mm 防水涂层，可应付中雨天气；顶部透气窗设计减少冷凝水，适合近郊露营与公园野餐。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 3 AND eq_name = '牧高笛双人自动帐篷') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 3, 2, '迪卡侬棉质睡袋', '迪卡侬', '中空棉 / 舒适 10℃',
 'https://cbu01.alicdn.com/img/ibank/O1CN01a21jma22uwsRU68hB_!!2513967181-0-cib.jpg',
 18.00, 80.00, 15, '迪卡侬中空棉信封睡袋，可全开当被子使用。舒适温度 10℃，适合春夏与初秋露营，可机洗、易打理。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 3 AND eq_name = '迪卡侬棉质睡袋') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 3, 3, '原始人折叠桌椅套装', '原始人', '一桌四椅 / 铝合金',
 'https://cbu01.alicdn.com/img/ibank/2018/037/390/8974093730_1474175813.jpg',
 30.00, 150.00, 8, '铝合金蛋卷桌 + 四把折叠椅组合，桌面承重 30kg，收纳后仅占后备箱一角，适合家庭与朋友聚会。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 3 AND eq_name = '原始人折叠桌椅套装') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 3, 4, '火枫便携卡式炉', '火枫', '折叠 / 3500W',
 'https://cbu01.alicdn.com/img/ibank/O1CN01IEAAeg1ddDBvild9n_!!957543758-0-cib.jpg',
 20.00, 120.00, 15, '火枫折叠卡式炉，3500W 大火力、带自动熄火保护，折叠后仅巴掌大小，适配市面通用丁烷气罐。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 3 AND eq_name = '火枫便携卡式炉') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 3, 5, '山力士 LED 露营灯', '山力士', '充电式 / 3 档调光',
 'https://cbu01.alicdn.com/img/ibank/O1CN01VDiJeE26XpQNUPfOh_!!2214580821185-0-cib.jpg',
 10.00, 50.00, 30, '山力士充电式 LED 露营灯，3 档调光 + 暖光模式，续航最长 40 小时，可当充电宝应急供电。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 3 AND eq_name = '山力士 LED 露营灯') t);

-- ---------- 3. 成都青城山（store 4）----------
INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 4, 1, '挪客四人家庭帐篷', '挪客', '4 人 / 双层 / 240×210cm',
 'https://cbu01.alicdn.com/img/ibank/2018/713/266/9640662317_249430366.jpg',
 69.00, 300.00, 5, '挪客四人家庭帐篷，双层银胶防晒面料，配前厅与大号透气纱窗，适合家庭亲子营地与长时间驻营。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 4 AND eq_name = '挪客四人家庭帐篷') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 4, 2, '黑冰羽绒睡袋', '黑冰', '充绒 400g / 舒适 -5℃',
 'https://cbu01.alicdn.com/img/ibank/O1CN01LELCgQ2MVZL0xNYKw_!!1650929833-0-cib.jpg',
 25.00, 100.00, 20, '黑冰 400g 充绒羽绒睡袋，表层防泼水、内里亲肤，舒适温标 -5℃，适合高海拔与秋冬露营。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 4 AND eq_name = '黑冰羽绒睡袋') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 4, 6, '挪客六角天幕', '挪客', '3×3m / 涂银防晒',
 'https://cbu01.alicdn.com/img/ibank/O1CN01wWcbOu1lOZWN3hqmA_!!2921024809-0-cib.jpg',
 28.00, 120.00, 8, '六角涂银天幕，UPF50+ 防晒、防小雨，配 6 根调节杆与地钉，可搭出遮阳休息区。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 4 AND eq_name = '挪客六角天幕') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 4, 4, '探险者户外烧烤架', '探险者', '折叠 / 30×30cm / 附烤网',
 'https://cbu01.alicdn.com/img/ibank/2016/346/521/3478125643_715840289.jpg',
 35.00, 200.00, 6, '不锈钢折叠烧烤架，展开 30×30cm 带烤网与炭槽，收纳后为扁平箱体，适合营地烧烤与野餐。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 4 AND eq_name = '探险者户外烧烤架') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 4, 5, '探路者营地灯串', '探路者', '5m / 20 灯 / 防水',
 'https://cbu01.alicdn.com/img/ibank/O1CN01HBVx2L1MTjZzCjsbf_!!2214580821185-0-cib.jpg',
 12.00, 60.00, 20, '5 米 20 灯暖光 LED 灯串，USB 供电、IP44 防水，可挂在天幕或帐篷外围营造营地氛围。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 4 AND eq_name = '探路者营地灯串') t);

-- ---------- 4. 广州白云山（store 5）----------
INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 5, 1, '探险者速开双人帐篷', '探险者', '2 人 / 速开 / 双层防雨',
 'https://cbu01.alicdn.com/img/ibank/O1CN01vkpmDa1G9gYvg4E0t_!!2213145640580-0-cib.jpg',
 39.00, 200.00, 12, '探险者液压速开帐篷，3 秒展开、双层防雨结构，适合南方多雨天气与周末短途露营。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 5 AND eq_name = '探险者速开双人帐篷') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 5, 2, '迪卡侬棉质睡袋', '迪卡侬', '中空棉 / 舒适 10℃',
 'https://cbu01.alicdn.com/img/ibank/O1CN01a21jma22uwsRU68hB_!!2513967181-0-cib.jpg',
 18.00, 80.00, 18, '迪卡侬中空棉信封睡袋，可全开当被子使用，舒适温度 10℃，可机洗易打理。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 5 AND eq_name = '迪卡侬棉质睡袋') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 5, 3, '原始人折叠桌椅套装', '原始人', '一桌四椅 / 铝合金',
 'https://cbu01.alicdn.com/img/ibank/2018/037/390/8974093730_1474175813.jpg',
 30.00, 150.00, 10, '铝合金蛋卷桌 + 四把折叠椅，桌面承重 30kg，收纳后仅占后备箱一角。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 5 AND eq_name = '原始人折叠桌椅套装') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 5, 4, '火枫便携卡式炉', '火枫', '折叠 / 3500W',
 'https://cbu01.alicdn.com/img/ibank/O1CN01IEAAeg1ddDBvild9n_!!957543758-0-cib.jpg',
 20.00, 120.00, 16, '火枫折叠卡式炉，3500W 大火力、带自动熄火保护，折叠后仅巴掌大小。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 5 AND eq_name = '火枫便携卡式炉') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 5, 5, '山力士 LED 露营灯', '山力士', '充电式 / 3 档调光',
 'https://cbu01.alicdn.com/img/ibank/O1CN01VDiJeE26XpQNUPfOh_!!2214580821185-0-cib.jpg',
 10.00, 50.00, 25, '山力士充电式 LED 露营灯，3 档调光 + 暖光模式，续航最长 40 小时。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 5 AND eq_name = '山力士 LED 露营灯') t);

-- ---------- 5. 西安秦岭（store 6）----------
INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 6, 1, '挪客四人家庭帐篷', '挪客', '4 人 / 双层 / 240×210cm',
 'https://cbu01.alicdn.com/img/ibank/2018/713/266/9640662317_249430366.jpg',
 69.00, 300.00, 6, '挪客四人家庭帐篷，双层银胶防晒面料，配前厅与大号透气纱窗，适合秦岭山区多日露营。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 6 AND eq_name = '挪客四人家庭帐篷') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 6, 2, '黑冰羽绒睡袋', '黑冰', '充绒 400g / 舒适 -5℃',
 'https://cbu01.alicdn.com/img/ibank/O1CN01LELCgQ2MVZL0xNYKw_!!1650929833-0-cib.jpg',
 25.00, 100.00, 22, '黑冰 400g 充绒羽绒睡袋，防泼水表层，舒适温标 -5℃，适合秦岭高海拔与秋冬露营。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 6 AND eq_name = '黑冰羽绒睡袋') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 6, 6, '黑鹿蝶形天幕', '黑鹿', '4×4m / 蝶形',
 'https://cbu01.alicdn.com/img/ibank/O1CN01gVdYYV1NaQzbSzaOC_!!2215599991586-0-cib.jpg',
 35.00, 150.00, 6, '4×4m 蝶形黑胶天幕，遮阳面积大、抗风结构强，配 2 根主杆与全套地钉风绳。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 6 AND eq_name = '黑鹿蝶形天幕') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 6, 4, '探险者户外烧烤架', '探险者', '折叠 / 30×30cm / 附烤网',
 'https://cbu01.alicdn.com/img/ibank/2016/346/521/3478125643_715840289.jpg',
 35.00, 200.00, 6, '不锈钢折叠烧烤架，展开 30×30cm 带烤网与炭槽，收纳后为扁平箱体。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 6 AND eq_name = '探险者户外烧烤架') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT 6, 5, '探路者营地灯串', '探路者', '5m / 20 灯 / 防水',
 'https://cbu01.alicdn.com/img/ibank/O1CN01HBVx2L1MTjZzCjsbf_!!2214580821185-0-cib.jpg',
 12.00, 60.00, 24, '5 米 20 灯暖光 LED 灯串，USB 供电、IP44 防水，可挂在天幕或帐篷外围。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = 6 AND eq_name = '探路者营地灯串') t);

-- ---------- 6. 襄阳理工学院（新网点）----------
INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT @xy, 1, '牧高笛双人自动帐篷', '牧高笛', '2 人 / 速开 / 210×150cm',
 'https://cbu01.alicdn.com/img/ibank/O1CN01HjEJN91Kcm9I156aQ_!!2214580821185-0-cib.jpg',
 39.00, 200.00, 12, '牧高笛经典速开双人帐篷，展开即用、无需繁琐搭杆。双层结构配 3000mm 防水涂层，适合近郊露营与校园周边野营。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = @xy AND eq_name = '牧高笛双人自动帐篷') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT @xy, 2, '迪卡侬棉质睡袋', '迪卡侬', '中空棉 / 舒适 10℃',
 'https://cbu01.alicdn.com/img/ibank/O1CN01a21jma22uwsRU68hB_!!2513967181-0-cib.jpg',
 18.00, 80.00, 16, '迪卡侬中空棉信封睡袋，可全开当被子使用，舒适温度 10℃，可机洗易打理。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = @xy AND eq_name = '迪卡侬棉质睡袋') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT @xy, 3, '原始人折叠桌椅套装', '原始人', '一桌四椅 / 铝合金',
 'https://cbu01.alicdn.com/img/ibank/2018/037/390/8974093730_1474175813.jpg',
 30.00, 150.00, 10, '铝合金蛋卷桌 + 四把折叠椅，桌面承重 30kg，适合宿舍组团出行。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = @xy AND eq_name = '原始人折叠桌椅套装') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT @xy, 4, '火枫便携卡式炉', '火枫', '折叠 / 3500W',
 'https://cbu01.alicdn.com/img/ibank/O1CN01IEAAeg1ddDBvild9n_!!957543758-0-cib.jpg',
 20.00, 120.00, 14, '火枫折叠卡式炉，3500W 大火力、带自动熄火保护，折叠后仅巴掌大小。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = @xy AND eq_name = '火枫便携卡式炉') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT @xy, 5, '山力士 LED 露营灯', '山力士', '充电式 / 3 档调光',
 'https://cbu01.alicdn.com/img/ibank/O1CN01VDiJeE26XpQNUPfOh_!!2214580821185-0-cib.jpg',
 10.00, 50.00, 28, '山力士充电式 LED 露营灯，3 档调光 + 暖光模式，续航最长 40 小时。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = @xy AND eq_name = '山力士 LED 露营灯') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, create_time)
SELECT @xy, 6, '挪客六角天幕', '挪客', '3×3m / 涂银防晒',
 'https://cbu01.alicdn.com/img/ibank/O1CN01wWcbOu1lOZWN3hqmA_!!2921024809-0-cib.jpg',
 28.00, 120.00, 8, '六角涂银天幕，UPF50+ 防晒、防小雨，配 6 根调节杆与地钉。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = @xy AND eq_name = '挪客六角天幕') t);

-- ---------- 7. 襄阳网点补 2 款食品 ----------
INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, sale_price, create_time)
SELECT @xy, 11, '海底捞自热米饭（菌菇鸡丁）', '海底捞', '320g / 盒',
 'http://t15.baidu.com/it/u=3822976528,1943004972&fm=224&app=112&f=JPEG?w=500&h=500',
 0.00, 0.00, 40, '海底捞自热米饭，自带加热包与餐具，加水 15 分钟即可吃上热饭，露营徒步的省心主食。', 1, 2, 26.00, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = @xy AND eq_name = '海底捞自热米饭（菌菇鸡丁）') t);

INSERT INTO equipment (store_id, category_id, eq_name, brand, spec, eq_img, price_per_day, deposit, stock_num, description, status, product_type, sale_price, create_time)
SELECT @xy, 13, '康比特能量棒（坚果巧克力）', '康比特', '35g / 根',
 'https://img1.baidu.com/it/u=2506764731,3281749548&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=654',
 0.00, 0.00, 80, '康比特坚果巧克力能量棒，一根补充约 150 大卡，徒步途中快速补能、不占空间。', 1, 2, 9.00, NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM equipment WHERE store_id = @xy AND eq_name = '康比特能量棒（坚果巧克力）') t);

-- ---------- 校验 ----------
SELECT s.id, s.store_name, s.city, COUNT(e.id) AS eq_cnt
FROM store s LEFT JOIN equipment e ON e.store_id = s.id
GROUP BY s.id, s.store_name, s.city ORDER BY s.id;
