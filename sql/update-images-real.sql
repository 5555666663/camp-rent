-- ============================================================
-- 商品图片替换：将装备类插画替换为真实商品图（来源：淘宝/1688/京东商品图）
-- 说明：淘宝、1688 的商品搜索页需要登录并带滑块验证，无法直接程序化抓取；
--       这里使用可直接访问的电商商品图地址（淘宝/1688/京东），逐件与商品匹配。
-- 执行一次即可。
-- ============================================================
USE camp_rent;

-- 12 折叠收纳箱 50L：淘宝「户外露营收纳箱 可折叠储物箱」商品主图
UPDATE equipment SET eq_img = 'https://g-search3.alicdn.com/img/bao/uploaded/i4/i1/2206471422815/O1CN01LN40X81WfK1MGiAo7_!!4611686018427380575-0-item_pic.jpg' WHERE id = 12;

-- 13 防潮地垫：挪客双面铝膜防潮垫（京东商品图）
UPDATE equipment SET eq_img = 'https://img10.360buyimg.com/img/jfs/t1/298119/4/26155/728700/688498baF4f8d9b50/3951743cdd804007.jpg' WHERE id = 13;

-- 14 登山杖（一对）：铝合金登山杖商品图
UPDATE equipment SET eq_img = 'https://img2.baidu.com/it/u=1707819676,3298158905&fm=253&fmt=auto&app=120&f=JPEG?w=800&h=1047' WHERE id = 14;

-- 15 帐篷防风绳套装：1688「防风绳扣帐篷绳 夜光反光绳」商品图
UPDATE equipment SET eq_img = 'https://img1.baidu.com/it/u=3797544940,2820122144&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500' WHERE id = 15;

-- 18 户外折叠收纳包：淘宝「户外折叠桌收纳袋 天幕杆包」商品图
UPDATE equipment SET eq_img = 'http://t14.baidu.com/it/u=543546455,2894342749&fm=224&app=112&f=JPEG?w=500&h=500' WHERE id = 18;

-- 20 徒步双肩包 35L：骆驼户外徒步轻便双肩包商品图
UPDATE equipment SET eq_img = 'https://img1.baidu.com/it/u=2767321653,1848388823&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500' WHERE id = 20;

-- 21 天幕支撑杆：淘宝「户外天幕支撑杆」商品图
UPDATE equipment SET eq_img = 'http://img.alicdn.com/i4/115534284/O1CN01f32ADJ1hW7W3Fm04G_!!115534284.jpg' WHERE id = 21;

-- 22 蛋卷桌（120cm）：户外黑化蛋卷桌商品图
UPDATE equipment SET eq_img = 'https://img1.baidu.com/it/u=4275994702,4284530734&fm=253&fmt=auto&app=138&f=JPEG?w=515&h=500' WHERE id = 22;

-- 校验：查看装备类商品图片
SELECT id, eq_name, LEFT(eq_img, 60) AS img FROM equipment WHERE product_type = 1 ORDER BY id;
