-- ============================================================
-- 图片规范化：让每个商品使用与其对应的图片
-- 1) 天幕类使用真实天幕商品图
-- 2) 配件类使用项目内置插画（无人像、断网可用）
-- 3) 套餐封面与套餐内容对应
-- ============================================================
USE camp_rent;

-- ---------- 装备：天幕类（真实商品图） ----------
UPDATE equipment SET eq_img = 'https://cbu01.alicdn.com/img/ibank/O1CN01wWcbOu1lOZWN3hqmA_!!2921024809-0-cib.jpg'
WHERE eq_name LIKE '六角天幕%';
UPDATE equipment SET eq_img = 'https://cbu01.alicdn.com/img/ibank/O1CN01gVdYYV1NaQzbSzaOC_!!2215599991586-0-cib.jpg'
WHERE eq_name LIKE '蝶形天幕%';
UPDATE equipment SET eq_img = 'https://cbu01.alicdn.com/img/ibank/O1CN01ZX8gSX1NEuKqmkPsu_!!2209583101539-0-cib.jpg'
WHERE eq_name LIKE '防晒天幕围布%';

-- ---------- 装备：配件类（内置插画，物品本身、无真人） ----------
UPDATE equipment SET eq_img = '/img/equip/storage.svg'      WHERE eq_name LIKE '折叠收纳箱%' OR eq_name LIKE '%收纳包%';
UPDATE equipment SET eq_img = '/img/equip/mat.svg'          WHERE eq_name LIKE '防潮地垫%';
UPDATE equipment SET eq_img = '/img/equip/trekking-pole.svg' WHERE eq_name LIKE '登山杖%';
UPDATE equipment SET eq_img = '/img/equip/guy-rope.svg'     WHERE eq_name LIKE '帐篷防风绳%';
UPDATE equipment SET eq_img = '/img/equip/backpack.svg'     WHERE eq_name LIKE '徒步双肩包%';
UPDATE equipment SET eq_img = '/img/equip/pole.svg'         WHERE eq_name LIKE '天幕支撑杆%';
UPDATE equipment SET eq_img = '/img/equip/table.svg'        WHERE eq_name LIKE '蛋卷桌%';

-- ---------- 套餐封面：与套餐主打装备对应 ----------
UPDATE camp_package SET cover_img = 'https://cbu01.alicdn.com/img/ibank/O1CN01HjEJN91Kcm9I156aQ_!!2214580821185-0-cib.jpg'
WHERE id = 1;  -- 精致露营双人套餐（双人帐篷）
UPDATE camp_package SET cover_img = '/img/equip/backpack.svg'
WHERE id = 2;  -- 轻装徒步套餐
UPDATE camp_package SET cover_img = 'https://cbu01.alicdn.com/img/ibank/2018/713/266/9640662317_249430366.jpg'
WHERE id = 3;  -- 家庭露营套餐（大帐篷）
UPDATE camp_package SET cover_img = '/img/equip/trekking-pole.svg'
WHERE id = 4;  -- 山野徒步套餐

-- ---------- 校验：每件商品的图片与名称 ----------
SELECT id, product_type, eq_name, spec, eq_img FROM equipment ORDER BY product_type, id;
SELECT id, package_name, scene_type, cover_img FROM camp_package ORDER BY id;
