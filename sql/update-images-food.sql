-- ============================================================
-- 食品类商品图片替换：将食品插画替换为真实商品图（淘宝/京东商品图）
-- 说明：淘宝搜索页需登录+滑块验证，无法程序化抓取；
--       这里使用可直接访问的电商商品图地址，逐件与食品匹配。
-- 执行一次即可。
-- ============================================================
USE camp_rent;

-- 23 自热米饭（红烧牛肉）：康食季红烧牛肉自热米饭
UPDATE equipment SET eq_img = 'https://img2.baidu.com/it/u=3805250370,2675499836&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=667' WHERE id = 23;

-- 24 冻干粥（皮蛋瘦肉）：冻干粥商品图
UPDATE equipment SET eq_img = 'https://img2.baidu.com/it/u=889854166,1190738369&fm=253&fmt=auto&app=138&f=JPEG?w=514&h=500' WHERE id = 24;

-- 25 能量棒（坚果巧克力）：新疆坚果巧克力能量棒
UPDATE equipment SET eq_img = 'https://img1.baidu.com/it/u=2506764731,3281749548&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=654' WHERE id = 25;

-- 26 罐装运动饮料：佳得乐运动饮料
UPDATE equipment SET eq_img = 'https://img0.baidu.com/it/u=1968825980,1498010332&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=889' WHERE id = 26;

-- 27 便携零食组合：三只松鼠坚果零食组合
UPDATE equipment SET eq_img = 'http://t15.baidu.com/it/u=1134552145,1594564256&fm=224&app=112&f=JPEG?w=500&h=500' WHERE id = 27;

-- 28 自热米饭（菌菇鸡丁）：莫小仙菌菇自热米饭（淘宝商品图）
UPDATE equipment SET eq_img = 'http://t15.baidu.com/it/u=3822976528,1943004972&fm=224&app=112&f=JPEG?w=500&h=500' WHERE id = 28;

-- 29 冻干水果：冻干水果商品图
UPDATE equipment SET eq_img = 'https://img2.baidu.com/it/u=200585346,2830738405&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500' WHERE id = 29;

-- 30 能量胶：康比特能量胶
UPDATE equipment SET eq_img = 'https://img1.baidu.com/it/u=909158554,2316099265&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=1002' WHERE id = 30;

-- 31 罐装气泡水：元气森林罐装气泡水
UPDATE equipment SET eq_img = 'https://img1.baidu.com/it/u=2145979129,555526276&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=667' WHERE id = 31;

-- 32 便携肉干：百草味肉干
UPDATE equipment SET eq_img = 'https://img2.baidu.com/it/u=2905117750,123851176&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=500' WHERE id = 32;

-- 校验：查看食品类商品图片
SELECT id, eq_name, LEFT(eq_img, 60) AS img FROM equipment WHERE product_type = 2 ORDER BY id;
