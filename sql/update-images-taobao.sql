-- ============================================================
-- 露营装备租赁系统 - 装备图片更新脚本（淘宝/1688 商家商品图）
-- 说明：图片均为 cbu01.alicdn.com（1688/淘宝商家图床）商品主图，
--       白底/场景产品图，不含人物，与装备品类一一对应。
-- 执行：mysql -uroot -p373394589 --default-character-set=utf8mb4 < 本文件
-- ============================================================
USE camp_rent;

-- 1. 双人自动帐篷（帐篷）—— 双人户外帐篷商品图
UPDATE equipment SET eq_img = 'https://cbu01.alicdn.com/img/ibank/O1CN01HjEJN91Kcm9I156aQ_!!2214580821185-0-cib.jpg' WHERE id = 1;
-- 2. 四人家庭帐篷（帐篷）—— 3-4 人全自动帐篷商品图
UPDATE equipment SET eq_img = 'https://cbu01.alicdn.com/img/ibank/2018/713/266/9640662317_249430366.jpg' WHERE id = 2;
-- 3. 羽绒睡袋（睡袋）—— 羽绒睡袋商品图
UPDATE equipment SET eq_img = 'https://cbu01.alicdn.com/img/ibank/O1CN01LELCgQ2MVZL0xNYKw_!!1650929833-0-cib.jpg' WHERE id = 3;
-- 4. 折叠桌椅套装（桌椅）—— 户外铝合金折叠桌商品图
UPDATE equipment SET eq_img = 'https://cbu01.alicdn.com/img/ibank/2018/037/390/8974093730_1474175813.jpg' WHERE id = 4;
-- 5. 便携卡式炉（炊具）—— 便携卡式炉商品图
UPDATE equipment SET eq_img = 'https://cbu01.alicdn.com/img/ibank/O1CN01IEAAeg1ddDBvild9n_!!957543758-0-cib.jpg' WHERE id = 5;
-- 6. LED露营灯（照明）—— LED 营地灯商品图
UPDATE equipment SET eq_img = 'https://cbu01.alicdn.com/img/ibank/O1CN01VDiJeE26XpQNUPfOh_!!1953027672-0-cib.jpg' WHERE id = 6;
-- 7. 双人自动帐篷（帐篷）—— 六角全自动帐篷商品图
UPDATE equipment SET eq_img = 'https://cbu01.alicdn.com/img/ibank/O1CN01LN1aX929BnBmCVGIk_!!3446478030-0-cib.jpg' WHERE id = 7;
-- 8. 棉质睡袋（睡袋）—— 信封式带帽睡袋商品图
UPDATE equipment SET eq_img = 'https://cbu01.alicdn.com/img/ibank/O1CN01a21jma22uwsRU68hB_!!2513967181-0-cib.jpg' WHERE id = 8;
-- 9. 户外烧烤架（炊具）—— 户外烧烤炉商品图
UPDATE equipment SET eq_img = 'https://cbu01.alicdn.com/img/ibank/2016/346/521/3478125643_715840289.jpg' WHERE id = 9;
-- 10. 营地灯串（照明）—— LED 露营灯串商品图
UPDATE equipment SET eq_img = 'https://cbu01.alicdn.com/img/ibank/O1CN01HBVx2L1MTjZzCjsbf_!!2212958251436-0-cib.jpg' WHERE id = 10;

-- 校验：查看更新结果
SELECT id, eq_name, category_id, eq_img FROM equipment ORDER BY id;
