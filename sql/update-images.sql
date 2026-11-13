-- ============================================================
-- 为已创建的 camp_rent 数据库补充「露营装备图片」
-- 图片全部来自 Wikimedia Commons（可自由使用），如需换成自己的图片，
-- 直接在后台「装备管理」中编辑装备的「图片地址」即可。
-- 使用方式：mysql -uroot -p373394589 --default-character-set=utf8mb4 < update-images.sql
-- ============================================================
USE camp_rent;

UPDATE `equipment` SET eq_img = 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/71/Ergaki%2C_Camping_tent_in_the_wild%2C_Sayan_Mountains%2C_Siberia%2C_Russia.jpg/960px-Ergaki%2C_Camping_tent_in_the_wild%2C_Sayan_Mountains%2C_Siberia%2C_Russia.jpg' WHERE id = 1;
UPDATE `equipment` SET eq_img = 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Alpine_Tent_Camping%2C_Olympic_National_Forest_%2823896632615%29.jpg/960px-Alpine_Tent_Camping%2C_Olympic_National_Forest_%2823896632615%29.jpg' WHERE id = 2;
UPDATE `equipment` SET eq_img = 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/2e/Mummy_bag.jpg/960px-Mummy_bag.jpg' WHERE id = 3;
UPDATE `equipment` SET eq_img = 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/51/Helinox_Ground_Chair_1.jpg/960px-Helinox_Ground_Chair_1.jpg' WHERE id = 4;
UPDATE `equipment` SET eq_img = 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/19/Family_campsite_in_Alaska.jpg/960px-Family_campsite_in_Alaska.jpg' WHERE id = 5;
UPDATE `equipment` SET eq_img = 'https://upload.wikimedia.org/wikipedia/commons/d/d2/Night_scene%2C_illuminated_tents_in_woods%2C_campfire.jpg' WHERE id = 6;
UPDATE `equipment` SET eq_img = 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/White_and_yellow_camping_tent_in_the_middle_of_forest.jpg/960px-White_and_yellow_camping_tent_in_the_middle_of_forest.jpg' WHERE id = 7;
UPDATE `equipment` SET eq_img = 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/OutDoor_2018%2C_Friedrichshafen_%281X7A0298%29.jpg/960px-OutDoor_2018%2C_Friedrichshafen_%281X7A0298%29.jpg' WHERE id = 8;
UPDATE `equipment` SET eq_img = 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/16/Rv_campsite_hoh_camground_camping_picnic_table_d_archuleta_march_05_2015_%2817185635739%29.jpg/960px-Rv_campsite_hoh_camground_camping_picnic_table_d_archuleta_march_05_2015_%2817185635739%29.jpg' WHERE id = 9;
UPDATE `equipment` SET eq_img = 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Campers_and_tent_underneath_a_night_sky_%2849950611437%29.jpg/960px-Campers_and_tent_underneath_a_night_sky_%2849950611437%29.jpg' WHERE id = 10;

SELECT id, eq_name, category_id, LEFT(eq_img, 80) AS img FROM `equipment` ORDER BY id;
