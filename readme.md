# 基于 SpringBoot + Vue3 的多网点露营装备租赁系统

前后端分离 B/S 架构的计算机毕业设计项目，支持多网点装备库存独立管理、在线租赁下单、地图选点、订单全流程管理与后台数据可视化统计。

---

## 一、项目介绍

本系统面向露营装备租赁业务场景，实现「普通用户前台」与「后台管理员」双角色：

- **前台（普通用户）**
  - 注册 / 登录 / 退出登录（密码 MD5 加密存储）
  - 网点地图：高德地图展示全部启用网点，支持定位、搜索、城市筛选，点击标记弹窗查看网点详情
  - 装备浏览：分类筛选、关键词搜索、装备卡片展示（归属网点/日租价/押金/库存）、装备详情
  - 租赁下单：选择装备与起止时间，选择取件网点、归还网点，自动校验取件网点库存
  - 个人订单：分页展示、按状态标签切换（待支付/租赁中/待归还/已完成/逾期）、订单详情
  - 归还确认：待归还订单归还确认后释放网点库存、流转为已完成
  - 评价反馈：订单完成后打分 + 文字评价，关联对应网点

- **后台（管理员）**
  - 登录权限拦截，普通用户无法进入后台路由
  - 网点管理：分页、增删改、启用/停用，地图点击选点自动回填经纬度
  - 装备分类管理：增删改、排序
  - 装备管理：分页、增删改、上下架，绑定网点与分类
  - 装备维保管理：分页、新增维保记录（选择装备自动带出所属网点）
  - 订单管理：全部订单、按网点/状态筛选、核验归还、标记逾期、修改备注
  - 评价反馈管理：查看、删除不良评价
  - 数据统计：ECharts 可视化（网点订单数、装备租赁频次、订单趋势）

**核心业务约束**：网点之间库存独立隔离；下单只扣减「取件网点」该装备库存；归还确认只恢复对应网点库存；订单同时记录取件网点与归还网点。

---

## 二、环境配置

| 软件 | 版本要求 | 说明 |
| --- | --- | --- |
| JDK | 8 或 11（推荐 11） | 项目 `pom.xml` 配置 `java.version=1.8`；SpringBoot 2.7.x 原生兼容 JDK 8~17 |
| Maven | 3.6+ | 后端构建工具 |
| Node.js | 16+（实测 v24） | 前端运行环境 |
| MySQL | 8.x | 数据库 |

> 说明：若必须使用 JDK 21，需将 SpringBoot 升级至 3.x 并将 `javax.*` 包替换为 `jakarta.*`，本项目的 SpringBoot 2.7.x 方案不推荐 JDK 21。

---

## 三、高德地图 Key 申请与替换

1. 访问高德开放平台：<https://console.amap.com/>
2. 注册/登录后进入「应用管理 → 我的应用 → 创建新应用」
3. 在应用下「添加 Key」，服务平台选择 **「Web端(JS API)」**（**不要**选「Web服务」，否则前端地图无法加载）
4. 创建成功后，复制生成的 **Key** 与 **安全密钥（securityJsCode）**
5. 打开前端配置文件 `camp-front/src/config/amap.js`，替换两个常量：

```js
// 高德 Web 端(JS API) Key
export const AMAP_KEY = '替换为你自己的高德Web端Key'

// 高德 JS API 2.0 安全密钥（jscode）
export const AMAP_SECURITY_CODE = '替换为你自己的高德安全密钥'
```

> 本项目已内置一组可用的 Key 与安全密钥，开箱即可看到地图；如需换成自己的，按住上面方式替换即可。

### 关于地图版本（重要）

`camp-front/src/utils/amap.js` 中使用的是 **JS API 1.4.15**（Canvas 渲染）而不是 2.0（WebGL 渲染）。
原因是 2.0 依赖 WebGL，在**虚拟机 / 关闭了浏览器硬件加速**的电脑上会出现「只有标记、没有底图」的空白地图；
1.4.15 兼容性最好，效果与交互完全满足本项目需求（自定义标记、信息窗体、定位、选点取经纬度均支持）。

若你的电脑 WebGL 正常、想用 2.0 的新特性，只需把 `utils/amap.js` 中脚本地址里的 `v=1.4.15` 改成 `v=2.0` 即可。

**常见问题排查：**

| 现象 | 原因 | 处理 |
| --- | --- | --- |
| 页面提示「地图加载失败」 | Key 类型不对（用了「Web服务」）或 Key 填错 | 换成「Web端(JS API)」的 Key |
| 有标记但没有底图 | 浏览器无 WebGL（JS API 2.0） | 本项目已改用 1.4.15，无需处理 |
| 控制台报 `INVALID_USER_SCODE` | 安全密钥未配置或填错 | 检查 `AMAP_SECURITY_CODE` |
| 地图提示 `INVALID_USER_DOMAIN` | Key 配置了域名白名单 | 控制台把 `localhost` 加入白名单 |

---

## 四、数据库导入步骤

1. 确保本机 MySQL 8 已启动
2. 使用任意 MySQL 客户端（Navicat / DataGrip / 命令行）执行 `sql/init.sql`

命令行导入示例：

```bash
mysql -uroot -p373394589 --default-character-set=utf8mb4 -e "source D:/DSH/camp-rent/sql/init.sql"
```

3. 脚本会自动执行以下操作：
   - 创建数据库 `camp_rent`（字符集 `utf8mb4`，排序规则 `utf8mb4_unicode_ci`）
   - 创建 8 张业务表（user、store、category、equipment、orders、deposit_record、equipment_maintain、comment）
   - 写入基础测试数据（管理员、2 个网点、5 个分类、10 件装备等）

> 数据库连接信息已在后端 `application.yml` 中预设：
> - 地址：`localhost:3306`
> - 数据库：`camp_rent`
> - 账号：`root`
> - 密码：`373394589`

---

## 五、后端启动步骤

```bash
# 进入后端目录
cd camp-back

# 编译打包
mvn clean package -DskipTests

# 启动（或直接用 IDEA 打开工程后运行 CampApplication）
mvn spring-boot:run
```

后端启动后默认运行在 `http://localhost:8080`，接口前缀 `/api`。

---

## 六、前端启动步骤

```bash
# 进入前端目录
cd camp-front

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端默认运行在 `http://localhost:5173`，已配置代理将 `/api` 转发至后端 `http://localhost:8080`。

---

## 七、管理员默认账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `123456` |
| 普通用户 | `zhangsan` | `123456` |
| 普通用户 | `lisi` | `123456` |

---

## 八、项目结构

```
camp-rent/
├── camp-back/                 # 后端 SpringBoot 工程
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/camp/
│       │   ├── controller/    # 控制器（含 controller/admin/）
│       │   ├── service/       # 服务接口
│       │   ├── service/impl/  # 服务实现
│       │   ├── mapper/        # MyBatis-Plus Mapper
│       │   ├── entity/        # 实体
│       │   ├── vo/            # 视图对象
│       │   ├── dto/           # 数据传输对象
│       │   ├── config/        # 配置（安全/跨域/分页/序列化）
│       │   ├── exception/     # 全局异常
│       │   ├── security/      # JWT 认证
│       │   ├── common/        # Result/PageResult
│       │   └── util/          # 工具类
│       └── resources/application.yml
├── camp-front/                # 前端 Vue3 工程
│   └── src/
│       ├── api/               # 接口封装
│       ├── views/             # 页面（含 views/admin/）
│       ├── router/            # 路由与守卫
│       ├── store/             # Pinia 状态
│       ├── utils/             # axios / 高德加载
│       ├── config/amap.js     # 高德 Key 配置
│       └── assets/style.css   # 全局样式
└── sql/init.sql               # 建库建表 + 测试数据
```

---

## 九、露营装备智能推荐（扩展功能）

### 1. 功能说明

用户在前台「装备推荐」页选择**未来 7 天内的出行日期**并输入**露营/徒步地点**，系统会：

1. 校验日期范围：超出未来 7 天直接提示，**不调用天气与 AI**；
2. 调用天气接口获取当日**温度、降水概率、是否下雨、风力、紫外线、雷电预警**；
3. 把日期、地点、天气、系统在架套餐列表交给**大模型**，按规则生成推荐；
4. 返回并渲染：**风险等级与安全提示**、**场景类型（露营/徒步）**、**推荐套餐卡片**、**建议补充装备**、**出行提醒**；
5. 点击推荐套餐卡片进入套餐详情页，可**一键预订整个套餐**或**自己组合装备**下单（每件装备各生成一条租赁订单，任一件库存不足则整单回滚）。

### 2. 数据库脚本

装备推荐依赖两张新表（套餐表、套餐明细表），需额外执行一次：

```bash
mysql -uroot -p373394589 --default-character-set=utf8mb4 < sql/update-weather-package.sql
```

脚本会创建 `camp_package`、`camp_package_item` 两张表并写入 **4 个测试套餐**（精致露营双人套餐、轻装徒步套餐、家庭露营套餐、山野徒步套餐）。

### 3. 天气接口

项目使用 **Open-Meteo** 免费天气接口（**无需申请 Key**），配置在 `application.yml` 的 `weather` 节点下。
若外网不可用，后端会自动退化为「按地点+日期哈希生成」的模拟天气，保证演示不中断。

### 4. 大模型配置（可选）

`application.yml` 中 `ai` 节点默认 `enabled: false`，此时使用**本地规则引擎**兜底，功能完全可用。
若要在论文/答辩中展示真实大模型调用，按以下步骤配置（OpenAI 兼容协议，DeepSeek / 通义千问 / 智谱均可）：

```yaml
ai:
  enabled: true
  base-url: https://api.deepseek.com   # 换成对应平台地址
  api-key: sk-你的Key                    # 填自己的 Key
  model: deepseek-chat
  timeout: 20000
```

### 5. 相关接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/weather/recommend` | 露营装备智能推荐（公开，无需登录） |
| GET | `/api/package/list` | 套餐列表（公开） |
| GET | `/api/package/{id}` | 套餐详情（公开） |
| POST | `/api/package/{id}/order` | 一键预订整个套餐（需登录） |
| POST | `/api/package/order-custom` | 自己组合套餐下单（需登录） |

### 6. 大模型返回的 JSON 结构

```json
{
  "risk_level": "安全 / 注意 / 高危",
  "risk_tip": "风险提示语",
  "scene_type": "露营 / 徒步",
  "recommend_package": ["套餐名称"],
  "optional_add": ["装备名称"],
  "remind_text": "出行提醒"
}
```

规则约定：**雷电预警、降水概率 ≥ 70%、风力 ≥ 40km/h 时，风险等级必须为「高危」并优先给出风险警告**。

---

## 十、RBAC 角色权限控制（扩展功能）

### 1. 三种角色

| 角色编码 | 名称 | 权限范围 |
| --- | --- | --- |
| `ROLE_USER` | 普通用户 | 天气 AI 动态组套、自定义套餐、下单、查看个人订单、评价 |
| `ROLE_CS` | 客服 | 进入独立**客服工作台**：查看全部订单、核验归还、标记逾期、修改订单状态、处理售后（删除不良评价）、查看并回复咨询消息；**禁止**装备增删改、改价格、管理账号、修改系统配置 |
| `ROLE_ADMIN` | 超级管理员 | 全部后台权限：装备、套餐、价格、用户、客服账号、数据统计 |

一个用户可以通过 `user_role` 关联表绑定**多个角色**，登录后按角色优先级跳转：超级管理员 → `/admin`，客服 → `/cs`，普通用户 → 前台首页。

### 2. 数据库脚本

```bash
mysql -uroot -p373394589 --default-character-set=utf8mb4 < sql/update-rbac.sql
```

脚本会：

1. 新建 `role`（角色表）、`user_role`（用户角色关联表）、`cs_message`（客服咨询消息表）；
2. 写入 3 个角色；
3. 为 `admin` 绑定 `ROLE_ADMIN`，`zhangsan` / `lisi` 绑定 `ROLE_USER`；
4. 新增客服账号 **kefu / 123456**（姓名：客服小美）并绑定 `ROLE_CS`。

### 3. 后端鉴权

- `JwtAuthenticationFilter` 解析 JWT 后，从 `user_role` 关联表加载该用户的**全部角色**，生成多个 `SimpleGrantedAuthority`；
- `SecurityConfig` 规则：
  - `/api/admin/**` → `hasRole("ADMIN")`
  - `/api/cs/**` → `hasAnyRole("CS","ADMIN")`
  - 其余接口按原规则（登录放行、GET 公共浏览放行）；
- 越权访问返回统一格式提示：`401 未登录或登录已过期` / `403 无权限访问`。

### 4. 前端路由守卫

`router/index.js` 中：

- `requiresAdmin`：非超级管理员访问 `/admin/**` 一律跳转首页；
- `requiresCs`：非客服/管理员访问 `/cs` 一律跳转首页；
- 登录成功后按 `roles` 决定落地页。

### 5. 相关接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/admin/user/page` | 用户分页（超级管理员） |
| POST | `/api/admin/user/cs` | 新增客服账号（自动绑定 ROLE_CS） |
| PUT | `/api/admin/user/{id}/status` | 启用 / 禁用账号 |
| GET | `/api/cs/order/page` | 客服：全部订单分页 |
| POST | `/api/cs/order/{id}/verify` | 客服：核验归还 |
| POST | `/api/cs/order/{id}/overdue` | 客服：标记逾期 |
| PUT | `/api/cs/order/{id}/status` | 客服：修改订单状态 |
| GET | `/api/cs/comment/page` | 客服：售后评价分页 |
| DELETE | `/api/cs/comment/{id}` | 客服：删除不良评价 |
| GET | `/api/cs/message/page` | 客服：咨询消息分页 |
| PUT | `/api/cs/message/{id}/reply` | 客服：回复咨询消息 |

### 6. 测试账号

| 角色 | 账号 | 密码 | 登录后落地页 |
| --- | --- | --- | --- |
| 超级管理员 | `admin` | `123456` | `/admin` 系统管理后台 |
| 客服 | `kefu` | `123456` | `/cs` 客服工作台 |
| 普通用户 | `zhangsan` | `123456` | 前台首页 |
| 普通用户 | `lisi` | `123456` | 前台首页 |

---

## 十一、就近网点与商品双大类（扩展功能）

### 1. 功能说明

| 能力 | 说明 |
| --- | --- |
| 浏览器定位 | 前台「就近网点」页调用 `navigator.geolocation` 取经纬度，只把经纬度传给后端 |
| 后端算距离 | 后端用 **Haversine 公式**（`util/GeoUtil`）计算球面距离，按距离升序返回网点，高德 Key 不出后端 |
| 网点详情 | 展示距离、地址、电话、营业时间、备注，以及该网点**可租装备**与**可购食品**的独立库存 |
| 商品两大类 | `equipment.product_type`：1-装备（租赁，按天计费 + 押金）；2-食品（一次性售卖，`sale_price`，不归还） |
| 分类两大类 | `category.category_type`：1-露营装备（帐篷/睡袋/天幕/折叠桌椅/炊具/照明/收纳/防护/徒步装备/防风配件）；2-露营食品（自热米饭/冻干食品/能量棒/罐装饮料/便携零食） |
| 食品下单 | 一次性售卖：直接生成「已完成」订单、无押金、无归还流程，只扣减**自提网点**库存 |
| 图片规范 | 装备用商品实拍图；食品用项目内置 SVG 插画（`camp-front/public/img/food/*.svg`），均无真人、只展示物品本身 |
| AI 联动 | 天气 AI 组套返回结果中会自动追加 3 个在售食品作为「可选附加」 |

### 2. 建表语句

执行 `sql/update-store-food.sql`（**只执行一次**，内含 ALTER）：

- `store` 增加 `remark`
- `category` 增加 `category_type`
- `equipment` 增加 `product_type`、`spec`、`sale_price`
- 新增 4 个网点测试数据（杭州西湖 / 成都青城山 / 广州白云山 / 西安秦岭，均带经纬度与备注）
- 新增 10 个分类、12 条装备、10 条食品测试数据

### 3. 相关接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/store/nearest?longitude=&latitude=` | 就近网点列表（按距离升序，含 `distanceKm`） |
| GET | `/api/store/{id}/detail?longitude=&latitude=` | 网点详情（网点信息 + 装备列表 + 食品列表） |
| GET | `/api/equipment/page?productType=1` | 商品分页（1-装备，2-食品，不传为全部） |
| POST | `/api/order/food` | 食品下单（`{storeId, items:[{equipmentId, quantity}]}`） |

### 4. 前端页面

| 页面 | 路径 | 说明 |
| --- | --- | --- |
| 就近网点 | `/nearest` | 一键定位 → 距离升序网点列表（含距离徽标、地址、电话、营业时间、备注） |
| 网点详情 | `/store/:id` | 网点信息 + 「可租装备 / 可购食品」双 Tab；食品卡片可直接加数量并提交购买 |
| 导航栏 | — | 新增「就近网点」入口 |

### 5. 权限说明

- **超级管理员**：网点 / 分类 / 装备 / 食品 / 价格 / 图片全部可维护
- **客服**：只能处理订单与售后，不能修改网点与商品基础信息
- **普通用户**：定位找就近网点、查看网点库存、挑选装备与食品、AI 组套、自定义套餐、下单


