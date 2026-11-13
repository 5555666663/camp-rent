import { AMAP_KEY, AMAP_SECURITY_CODE } from '@/config/amap'

// ============================================================
// 高德地图 JS API 2.0 动态加载器
// 用法：const AMap = await loadAMap()   // 返回 window.AMap
// ============================================================

let amapPromise = null

/**
 * 动态加载高德地图 JS API（v2.0）
 * - 自动写入安全密钥 window._AMapSecurityConfig（必须在脚本加载前设置）
 * - 自动携带常用插件（定位、地理编码）
 * - 并发调用复用同一个 Promise，失败后可重试
 */
export function loadAMap() {
  if (window.AMap) return Promise.resolve(window.AMap)
  if (amapPromise) return amapPromise

  // 关键：安全密钥必须在脚本加载之前写入，否则 JS API 2.0 会拒绝服务
  if (AMAP_SECURITY_CODE) {
    window._AMapSecurityConfig = { securityJsCode: AMAP_SECURITY_CODE }
  }

  amapPromise = new Promise((resolve, reject) => {
    if (!AMAP_KEY) {
      amapPromise = null
      reject(new Error('未配置高德地图 Key，请在 src/config/amap.js 中填写'))
      return
    }

    let settled = false
    const finish = (err, value) => {
      if (settled) return
      settled = true
      clearTimeout(timer)
      if (err) {
        // 失败后清空缓存，允许下次重试
        amapPromise = null
        reject(err)
      } else {
        resolve(value)
      }
    }

    // 超时保护：15 秒内没有回调视为加载失败（多为 Key/安全密钥不正确或网络异常）
    const timer = setTimeout(() => {
      finish(new Error('高德地图加载超时，请检查 Key / 安全密钥 / 网络'))
    }, 15000)

    // 全局回调：脚本加载完成后由高德调用
    window.__amapInit = () => {
      if (window.AMap) {
        finish(null, window.AMap)
      } else {
        finish(new Error('高德地图初始化失败，请检查 Key 类型是否为「Web端(JS API)」'))
      }
    }

    const script = document.createElement('script')
    script.type = 'text/javascript'
    script.async = true
    // 使用 JS API 1.4.15：基于 Canvas/DOM 渲染，兼容性最好（2.0 依赖 WebGL，
    // 部分虚拟机/关闭硬件加速的浏览器会出现「只有标记、没有底图」的问题）
    script.src =
      `https://webapi.amap.com/maps?v=1.4.15&key=${AMAP_KEY}` +
      `&plugin=AMap.Geolocation,AMap.Geocoder&callback=__amapInit`
    script.onerror = () => finish(new Error('高德地图脚本加载失败，请检查网络或 Key 配置'))
    document.head.appendChild(script)
  })

  return amapPromise
}

/**
 * 手动重置加载状态（用于「重新加载地图」按钮）
 */
export function resetAMapLoader() {
  amapPromise = null
  try {
    delete window.__amapInit
  } catch (e) {
    window.__amapInit = undefined
  }
}
