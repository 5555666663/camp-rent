// ============================================================
// 高德地图 Web 端 JS API Key 配置（独立配置文件）
// ============================================================
// 【说明】此处已填入项目使用的高德 Web 端(JS API) Key 与安全密钥。
// 如需换成自己的 Key，按下面步骤操作后替换两个常量即可：
//
// 申请步骤：
//   1. 打开高德开放平台官网：https://console.amap.com/
//   2. 注册并登录（支持个人开发者免费注册）
//   3. 进入「应用管理」→「我的应用」→ 点击「创建新应用」
//   4. 应用创建后，点击「添加 Key」
//   5. 服务平台选择「Web端(JS API)」（注意：不是「Web服务」）
//   6. 实名认证后，复制生成的 Key 与「安全密钥 jscode」
//   7. 替换下面两个常量
//
// 说明：
//   - AMAP_KEY：JS API 的 Key（必填）
//   - AMAP_SECURITY_CODE：JS API 2.0 的安全密钥 jscode（必填）
//     加载地图前会写入 window._AMapSecurityConfig 中
// ============================================================

// 高德 Web 端(JS API) Key
export const AMAP_KEY = '45bd163a2a01a9cc8aaffa909e8ec122'

// 高德 JS API 2.0 安全密钥（jscode）
export const AMAP_SECURITY_CODE = '704d56fa092e5ea3290d2cc607cd3a1'

// 地图默认中心点（北京天安门附近）与默认缩放级别
export const AMAP_DEFAULT_CENTER = [116.397428, 39.90923]
export const AMAP_DEFAULT_ZOOM = 11
