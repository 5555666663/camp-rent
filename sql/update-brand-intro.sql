-- =====================================================
-- 为商品补充「品牌」与「商品介绍」，并微调部分商品图片
-- 数据库：camp_rent
-- 说明：本脚本可重复执行（ALTER 前会先判断列是否存在）
-- =====================================================

USE camp_rent;

-- 1. 新增品牌字段（放在商品名称后面）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = 'camp_rent' AND TABLE_NAME = 'equipment' AND COLUMN_NAME = 'brand');
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE equipment ADD COLUMN brand VARCHAR(50) DEFAULT NULL COMMENT ''品牌'' AFTER eq_name',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 装备类：补充品牌、规格、商品介绍
UPDATE equipment SET brand = '牧高笛', spec = '2 人 / 速开 / 210×150cm',
  description = '牧高笛经典速开双人帐篷，展开即用、无需繁琐搭杆。双层结构配 3000mm 防水涂层，可应付中雨天气；顶部透气窗设计减少冷凝水，适合周末近郊露营和公园野餐。'
WHERE id = 1;

UPDATE equipment SET brand = '挪客', spec = '4 人 / 210×210×160cm',
  description = '挪客四人家庭帐篷，内部空间宽敞可平躺两大两小。铝合金杆骨架抗风稳定，配大面积网纱门，夏季通风不闷热；前厅可放置装备，适合家庭亲子露营。'
WHERE id = 2;

UPDATE equipment SET brand = '黑冰', spec = '充绒 400g / 舒适 -5℃',
  description = '黑冰羽绒睡袋，90% 白鸭绒填充，蓬松保暖、压缩后体积小。信封式开口可完全展开当被子使用，适合春秋及初冬露营，也可作为徒步过夜的主睡袋。'
WHERE id = 3;

UPDATE equipment SET brand = '原始人', spec = '一桌四椅 / 铝合金',
  description = '原始人折叠桌椅套装，铝合金桌板轻便耐腐蚀，四把月亮椅承重稳定。整套可收纳进随附收纳袋，适合 4-6 人营地用餐、野炊与户外火锅。'
WHERE id = 4;

UPDATE equipment SET brand = '火枫', spec = '2800W / 单头 / 丁烷气罐',
  description = '火枫便携卡式炉，2800W 大火力 3 分钟烧开水。带防风圈与自动断气保护，火焰可无级调节，适合营地煮面、炒菜、涮锅，配标准丁烷气罐使用。'
WHERE id = 5;

UPDATE equipment SET brand = '山力士', spec = '800 流明 / 充电式',
  description = '山力士充电式 LED 露营灯，最高 800 流明，五档亮度与 SOS 模式。内置大容量电池可当充电宝使用，底部磁吸与挂钩设计，可挂帐篷顶或吸在车体上。'
WHERE id = 6;

UPDATE equipment SET brand = '探险者', spec = '2 人 / 速开 / 双层防雨',
  description = '探险者双层防雨速开帐篷，弹压式骨架 3 秒展开。外帐防水、内帐透气，带防蚊网纱门；收纳后仅矿泉水瓶大小，适合说走就走的周末短途。'
WHERE id = 7;

UPDATE equipment SET brand = '迪卡侬', spec = '中空棉 / 舒适 10℃',
  description = '迪卡侬 Quechua 中空棉睡袋，可机洗、易打理，亲肤内衬不闷汗。适合春夏及室内露营，也可作客用被褥，性价比高、耐用耐脏。'
WHERE id = 8;

UPDATE equipment SET brand = '探险者', spec = '折叠 / 30×30cm / 附烤网',
  description = '探险者折叠烧烤架，不锈钢烤网 + 加厚炭槽，展开即用、收折后仅手提包大小。适合 3-5 人营地烧烤，配隔热手柄方便移动，是露营聚会的核心装备。'
WHERE id = 9;

UPDATE equipment SET brand = '探路者', spec = '5m / 20 灯 / 防水',
  description = '探路者营地氛围灯串，5 米 20 颗暖光灯珠，IP44 防水可挂树梢、天幕或帐篷外沿。暖黄光线营造营地氛围，夜间也能当作基础照明使用。'
WHERE id = 10;

UPDATE equipment SET brand = '挪客', spec = '3×3m / 涂银防晒',
  description = '挪客六角天幕，涂银面料反射紫外线，遮阳面积 9㎡。六点固定结构抗风稳定，可搭成客厅遮阳棚或炊事区顶棚，适合 3-4 人野餐与露营。'
WHERE id = 11;

UPDATE equipment SET brand = '牧高笛', spec = '50L / 可折叠 / 牛津布',
  description = '牧高笛 50L 折叠收纳箱，加厚牛津布内衬钢架，装满装备也不塌陷。侧面可开口取物，不用时可折成薄片收纳，是营地整理锅具、食材的实用大件。'
WHERE id = 12;

UPDATE equipment SET brand = '挪客', spec = '200×200cm / 铝膜',
  description = '挪客加厚铝膜防潮地垫，底面反射热量、隔绝地面湿气，铺在帐篷内或天幕下都合适。四角带固定环，可配地钉防止被风吹起，收起来只有一本书大小。'
WHERE id = 13;

UPDATE equipment SET brand = '探路者', spec = '铝合金 / 三节伸缩 / 避震',
  description = '探路者一对装登山杖，航空铝合金杖身仅 260g/支，三节可调长度并带外锁快扣。内置避震弹簧减轻下坡冲击，软木手柄吸汗防滑，适合徒步、登山与负重行走。'
WHERE id = 14;

UPDATE equipment SET brand = '牧高笛', spec = '8 根绳 + 8 只地钉',
  description = '牧高笛帐篷防风绳套装，含 8 根反光防风绳与 8 只加粗地钉。反光绳夜间可辨识不易绊倒，配调节扣可快速收紧，遇到大风天气能显著提升帐篷稳定性。'
WHERE id = 15;

UPDATE equipment SET brand = '探险者', spec = '1-2 人 / 速开 / 附收纳袋',
  description = '探险者单人多功能帐篷，速开结构一人即可搭好。外帐防雨防晒、内帐可单独使用，附带收纳袋可直接背上山，适合徒步过夜或钓友临时休息。'
WHERE id = 16;

UPDATE equipment SET brand = '黑鹿', spec = '4×4m / 蝶形',
  description = '黑鹿蝶形天幕，16㎡ 超大遮阳面积，蝶形剪裁排水顺畅不兜雨。可搭成客厅、炊事区或影音幕布背景，适合家庭露营和多人聚会。'
WHERE id = 17;

UPDATE equipment SET brand = '挪客', spec = '40L / 防水耐磨',
  description = '挪客 40L 户外收纳包，防水耐磨面料配加厚背带，可装锅具、餐具与食材。包口大开口便于翻找，不用时压扁收纳，是营地后勤的万能收纳袋。'
WHERE id = 18;

UPDATE equipment SET brand = '挪客', spec = '2×2m / UPF50+',
  description = '挪客防晒天幕围布，UPF50+ 防晒面料可作天幕侧挡风或帐篷外延。拉链与挂扣设计安装方便，能有效挡住侧向风雨和午后西晒。'
WHERE id = 19;

UPDATE equipment SET brand = '凯乐石', spec = '35L / 透气背负',
  description = '凯乐石 35L 徒步双肩包，S 型透气背负系统贴合脊背、减少闷热。含腰带承重、侧袋水壶位与雨罩，满足 1-2 日徒步的装载需求。'
WHERE id = 20;

UPDATE equipment SET brand = '牧高笛', spec = '2.4m / 铝合金 / 可调',
  description = '牧高笛天幕支撑杆，加粗铝合金杆身可调高度 1.8-2.4m，配防滑底座与顶帽。单根即可撑起天幕中脊，加粗管壁在风大时也不易弯折。'
WHERE id = 21;

UPDATE equipment SET brand = '原始人', spec = '120×60cm / 铝合金',
  description = '原始人蛋卷桌，铝合金卷板桌面收卷后仅手臂粗细。桌腿可调高度适应不平地面，承重 30kg，配四把椅子即可组成完整营地餐桌。'
WHERE id = 22;

-- 3. 食品类：补充品牌、商品介绍
UPDATE equipment SET brand = '自嗨锅', spec = '320g / 盒',
  description = '自嗨锅红烧牛肉自热米饭，自带加热包与餐具，加冷水 15 分钟即可吃上热饭。大块牛肉配浓香酱汁，是露营、徒步和长途出行的应急主食。'
WHERE id = 23;

UPDATE equipment SET brand = '莫小仙', spec = '60g / 袋',
  description = '莫小仙冻干粥（皮蛋瘦肉），冻干锁鲜技术保留米粒口感，开水冲泡 5 分钟即可食用。一袋仅 60g 便于携带，早晨在营地喝一碗热粥很舒服。'
WHERE id = 24;

UPDATE equipment SET brand = '康比特', spec = '35g / 根',
  description = '康比特坚果巧克力能量棒，坚果 + 燕麦 + 巧克力组合，单根约 180 大卡。徒步途中快速补充体力与糖原，独立包装不易压碎，适合随身应急。'
WHERE id = 25;

UPDATE equipment SET brand = '佳得乐', spec = '330ml / 罐',
  description = '佳得乐运动饮料，含电解质与碳水化合物，运动出汗后快速补充水分与盐分。露营时配冰块更爽口，也适合徒步后恢复体力。'
WHERE id = 26;

UPDATE equipment SET brand = '三只松鼠', spec = '混合装 200g',
  description = '三只松鼠便携零食组合，含果干、坚果与饼干小包装，一袋满足 3-4 人分享。独立小袋不占空间，适合营地闲聊、看星星时的休闲零嘴。'
WHERE id = 27;

UPDATE equipment SET brand = '海底捞', spec = '320g / 盒',
  description = '海底捞菌菇鸡丁自热米饭，菌菇鲜香配嫩滑鸡丁，自带加热包与餐具，冷水 15 分钟即可开饭，是露营不想开火时的省心选择。'
WHERE id = 28;

UPDATE equipment SET brand = '三只松鼠', spec = '30g / 袋',
  description = '三只松鼠冻干水果，低温冻干保留果香与酥脆口感，不额外添加糖。轻便耐储存，徒步途中补充维生素，也是小朋友爱吃的健康零食。'
WHERE id = 29;

UPDATE equipment SET brand = '康比特', spec = '40g / 支',
  description = '康比特能量胶，一支约 100 大卡，含麦芽糊精与电解质，长距离徒步或爬坡前 15 分钟食用能快速供能。软包装易撕开，运动中也能单手服用。'
WHERE id = 30;

UPDATE equipment SET brand = '元气森林', spec = '330ml / 罐',
  description = '元气森林 0 糖 0 卡气泡水，冰镇后气泡感强、解腻清爽。露营吃烧烤或火锅时搭配，既能解渴又不增加负担，是不可或缺的解腻饮料。'
WHERE id = 31;

UPDATE equipment SET brand = '百草味', spec = '100g / 袋',
  description = '百草味便携肉干，精选瘦肉低温烘烤，高蛋白低脂肪、嚼劲十足。独立小包装方便携带，是徒步途中补充能量与解馋的经典肉制品零食。'
WHERE id = 32;

-- 4. 图片微调：id=7 换成另一款速开帐篷实拍图，避免与 id=1 视觉重复
UPDATE equipment SET eq_img = 'https://cbu01.alicdn.com/img/ibank/O1CN01vkpmDa1G9gYvg4E0t_!!2213145640580-0-cib.jpg'
WHERE id = 7;

-- 5. 校验
SELECT id, eq_name, brand, spec, LEFT(description, 24) AS desc_head FROM equipment ORDER BY id;
