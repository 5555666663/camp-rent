<template>
  <div class="map-page">
    <!-- 地图画布 -->
    <div id="amap" class="map-canvas"></div>

    <!-- 左上角浮动面板 -->
    <div class="map-panel">
      <div class="panel-head">
        <div class="panel-logo">🗺️</div>
        <div class="panel-title-wrap">
          <div class="panel-title">网点地图</div>
          <div class="panel-sub">共 {{ filteredStores.length }} 个网点 · 点击标记查看详情</div>
        </div>
      </div>

      <el-input
        v-model="keyword"
        placeholder="搜索网点名称 / 地址"
        clearable
        class="panel-search"
        @keyup.enter="applyFilter"
        @clear="applyFilter"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select v-model="city" placeholder="选择城市" clearable class="panel-city" @change="applyFilter">
        <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
      </el-select>

      <div class="panel-actions">
        <el-button type="primary" class="act-btn" @click="applyFilter">
          <el-icon><Search /></el-icon>&nbsp;查询
        </el-button>
        <el-button class="act-btn ghost" @click="locateMe">
          <el-icon><Aim /></el-icon>&nbsp;定位
        </el-button>
      </div>

      <!-- 网点列表 -->
      <div class="store-list" v-if="filteredStores.length">
        <div
          v-for="s in filteredStores"
          :key="s.id"
          class="store-item"
          :class="{ active: activeId === s.id }"
          @click="focusStore(s)"
        >
          <div class="store-dot"></div>
          <div class="store-info">
            <div class="store-name">{{ s.storeName }}</div>
            <div class="store-addr">{{ s.address }}</div>
          </div>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
      </div>
      <div v-else class="store-empty">没有符合条件的网点</div>
    </div>

    <!-- 加载 / 错误提示 -->
    <div v-if="loading" class="map-mask">
      <div class="mask-card">
        <div class="spinner"></div>
        <div>地图加载中…</div>
      </div>
    </div>
    <div v-else-if="errorMsg" class="map-mask">
      <div class="mask-card error">
        <div class="err-icon">⚠️</div>
        <div class="err-title">地图加载失败</div>
        <div class="err-msg">{{ errorMsg }}</div>
        <el-button type="primary" size="small" @click="retry">重新加载</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Aim, ArrowRight } from '@element-plus/icons-vue'
import { getStoreList, getStoreCities } from '@/api/public'
import { loadAMap, resetAMapLoader } from '@/utils/amap'
import { AMAP_DEFAULT_CENTER, AMAP_DEFAULT_ZOOM } from '@/config/amap'

const router = useRouter()

const storeList = ref([])
const cities = ref([])
const keyword = ref('')
const city = ref('')
const activeId = ref(null)

const loading = ref(true)
const errorMsg = ref('')

let AMapRef = null
let map = null
let infoWindow = null
let geolocation = null
let markers = []
const markerMap = new Map()

/* ---------------- 数据筛选 ---------------- */
const filteredStores = computed(() => {
  const kw = keyword.value.trim()
  return storeList.value.filter((s) => {
    const matchCity = !city.value || s.city === city.value
    const matchKw =
      !kw ||
      (s.storeName || '').includes(kw) ||
      (s.address || '').includes(kw) ||
      (s.city || '').includes(kw)
    return matchCity && matchKw
  })
})

/* ---------------- 生命周期 ---------------- */
onMounted(async () => {
  try {
    const [stores, cityList] = await Promise.all([getStoreList(), getStoreCities()])
    storeList.value = stores || []
    cities.value = cityList || []
  } catch (e) {
    // 接口异常不影响地图加载
    storeList.value = []
    cities.value = []
  }
  await initMap()
})

onBeforeUnmount(() => {
  if (geolocation) {
    geolocation.destroy && geolocation.destroy()
    geolocation = null
  }
  if (infoWindow) {
    infoWindow.close()
    infoWindow = null
  }
  markers.forEach((m) => m.setMap && m.setMap(null))
  markers = []
  markerMap.clear()
  if (map) {
    map.destroy()
    map = null
  }
})

/* ---------------- 地图初始化 ---------------- */
async function initMap() {
  loading.value = true
  errorMsg.value = ''
  try {
    AMapRef = await loadAMap()
    await nextTick()

    map = new AMapRef.Map('amap', {
      zoom: AMAP_DEFAULT_ZOOM,
      center: AMAP_DEFAULT_CENTER,
      mapStyle: 'amap://styles/normal',
      viewMode: '2D',
      resizeEnable: true,
      showLabel: true
    })

    infoWindow = new AMapRef.InfoWindow({
      offset: new AMapRef.Pixel(0, -18),
      isCustom: true
    })

    // 定位控件（右下角）
    geolocation = new AMapRef.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
      buttonPosition: 'RB',
      showButton: true,
      showMarker: true,
      showCircle: false
    })
    map.addControl(geolocation)

    renderMarkers()
    loading.value = false
  } catch (e) {
    loading.value = false
    errorMsg.value = e && e.message ? e.message : '未知错误'
  }
}

/* ---------------- 渲染网点标记 ---------------- */
function renderMarkers() {
  if (!map || !AMapRef) return
  markers.forEach((m) => m.setMap(null))
  markers = []
  markerMap.clear()

  filteredStores.value.forEach((store) => {
    if (store.longitude == null || store.latitude == null) return
    const marker = new AMapRef.Marker({
      position: [Number(store.longitude), Number(store.latitude)],
      anchor: 'bottom-center',
      content: `
        <div class="amap-pin">
          <div class="amap-pin-body">
            <span class="amap-pin-icon">⛺</span>
            <span class="amap-pin-name">${escapeHtml(store.storeName)}</span>
          </div>
          <div class="amap-pin-tail"></div>
        </div>`,
      zIndex: 100
    })

    marker.on('click', () => {
      activeId.value = store.id
      openInfoWindow(store)
    })
    marker.setMap(map)
    markers.push(marker)
    markerMap.set(String(store.id), marker)
  })

  if (markers.length) {
    map.setFitView(markers, false, [90, 320, 90, 60])
  }
}

/* ---------------- 信息窗体 ---------------- */
function openInfoWindow(store) {
  if (!infoWindow) return
  const html = `
    <div class="amap-info">
      <div class="info-top">
        <div class="info-icon">⛺</div>
        <div>
          <div class="info-name">${escapeHtml(store.storeName)}</div>
          <div class="info-city">${escapeHtml(store.city || '')}</div>
        </div>
      </div>
      <div class="info-row"><span>地址</span><b>${escapeHtml(store.address || '-')}</b></div>
      <div class="info-row"><span>电话</span><b>${escapeHtml(store.phone || '-')}</b></div>
      <div class="info-row"><span>营业</span><b>${escapeHtml(store.businessHours || '-')}</b></div>
      <div class="info-btns">
        <button class="btn ghost" data-act="view">查看装备</button>
        <button class="btn primary" data-act="book">立即预约</button>
      </div>
    </div>`

  infoWindow.setContent(html)
  infoWindow.open(map, [Number(store.longitude), Number(store.latitude)])

  // 绑定按钮事件（自定义窗体需手动绑定）
  setTimeout(() => {
    const btns = document.querySelectorAll('.amap-info .btn')
    btns.forEach((btn) => {
      btn.onclick = () => {
        if (btn.dataset.act === 'view') {
          router.push({ path: '/equipment', query: { storeId: store.id } })
        } else {
          router.push({ path: '/equipment', query: { storeId: store.id } })
        }
      }
    })
  }, 0)
}

/* ---------------- 交互 ---------------- */
function applyFilter() {
  activeId.value = null
  if (infoWindow) infoWindow.close()
  renderMarkers()
}

function focusStore(store) {
  if (!map) return
  activeId.value = store.id
  map.setZoomAndCenter(14, [Number(store.longitude), Number(store.latitude)])
  openInfoWindow(store)
}

function locateMe() {
  if (!geolocation || !map) return
  geolocation.getCurrentPosition((status, result) => {
    if (status === 'complete' && result.position) {
      map.setZoomAndCenter(14, [result.position.lng, result.position.lat])
    } else {
      // 定位失败（用户拒绝授权或无定位服务）时，定位到首个网点
      if (filteredStores.value.length) {
        focusStore(filteredStores.value[0])
      }
    }
  })
}

function retry() {
  resetAMapLoader()
  if (map) {
    map.destroy()
    map = null
  }
  initMap()
}

/* ---------------- 工具 ---------------- */
function escapeHtml(str) {
  return String(str == null ? '' : str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}
</script>

<style scoped>
/* 页面容器：占满除顶部导航外的可视区域 */
.map-page {
  position: relative;
  width: 100%;
  height: calc(100vh - 70px);
  min-height: 560px;
  overflow: hidden;
  background: #eef4f0;
}

.map-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

/* ---------------- 左上浮动面板 ---------------- */
.map-panel {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 10;
  width: 320px;
  max-height: calc(100% - 40px);
  display: flex;
  flex-direction: column;
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(18px);
  box-shadow: 0 18px 40px rgba(15, 31, 26, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.7);
}

.panel-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-logo {
  width: 42px;
  height: 42px;
  border-radius: 13px;
  display: grid;
  place-items: center;
  font-size: 20px;
  background: linear-gradient(135deg, #16b876, #3ddc97);
  box-shadow: 0 8px 18px rgba(22, 184, 118, 0.3);
  flex-shrink: 0;
}

.panel-title {
  font-size: 17px;
  font-weight: 700;
  color: #0f1f1a;
}

.panel-sub {
  font-size: 12px;
  color: #8b9a94;
  margin-top: 2px;
}

.panel-search {
  margin-bottom: 10px;
}

.panel-city {
  width: 100%;
  margin-bottom: 12px;
}

.panel-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.act-btn {
  flex: 1;
  border-radius: 12px;
  height: 38px;
}

.act-btn.ghost {
  background: #f1f8f4;
  border-color: transparent;
  color: #43564f;
}

/* 网点列表 */
.store-list {
  overflow-y: auto;
  max-height: 300px;
  padding-right: 4px;
}

.store-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 13px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.store-item:hover,
.store-item.active {
  background: #eafaf1;
}

.store-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: linear-gradient(135deg, #16b876, #3ddc97);
  box-shadow: 0 0 0 4px rgba(22, 184, 118, 0.14);
  flex-shrink: 0;
}

.store-info {
  flex: 1;
  min-width: 0;
}

.store-name {
  font-size: 14px;
  font-weight: 600;
  color: #0f1f1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.store-addr {
  font-size: 12px;
  color: #8b9a94;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.arrow {
  color: #b6c4be;
  font-size: 13px;
}

.store-empty {
  font-size: 13px;
  color: #8b9a94;
  text-align: center;
  padding: 14px 0;
}

/* ---------------- 遮罩 ---------------- */
.map-mask {
  position: absolute;
  inset: 0;
  z-index: 20;
  display: grid;
  place-items: center;
  background: rgba(238, 244, 240, 0.72);
  backdrop-filter: blur(2px);
}

.mask-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 26px 34px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 18px 40px rgba(15, 31, 26, 0.14);
  font-size: 14px;
  color: #43564f;
}

.spinner {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 3px solid rgba(22, 184, 118, 0.18);
  border-top-color: #16b876;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.mask-card.error {
  max-width: 360px;
  text-align: center;
}

.err-icon {
  font-size: 28px;
}

.err-title {
  font-size: 16px;
  font-weight: 700;
  color: #c0392b;
}

.err-msg {
  font-size: 13px;
  color: #8b9a94;
  line-height: 1.6;
  word-break: break-all;
}
</style>

<style>
/* ===== 高德标记与信息窗体（非 scoped，作用于 AMap 生成的 DOM） ===== */
.amap-pin {
  display: flex;
  flex-direction: column;
  align-items: center;
  transform: translateY(-2px);
  cursor: pointer;
}

.amap-pin-body {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 12px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid rgba(22, 184, 118, 0.35);
  box-shadow: 0 8px 20px rgba(15, 31, 26, 0.18);
  font-size: 12.5px;
  font-weight: 700;
  color: #0f1f1a;
  white-space: nowrap;
  transition: all 0.18s ease;
}

.amap-pin:hover .amap-pin-body {
  background: linear-gradient(135deg, #16b876, #3ddc97);
  color: #fff;
  border-color: transparent;
  transform: translateY(-2px);
}

.amap-pin-icon {
  font-size: 13px;
}

.amap-pin-tail {
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-top: 8px solid #fff;
  filter: drop-shadow(0 2px 2px rgba(15, 31, 26, 0.16));
}

.amap-info {
  width: 268px;
  padding: 16px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 20px 44px rgba(15, 31, 26, 0.22);
  font-family: inherit;
}

.amap-info .info-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.amap-info .info-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-size: 18px;
  background: linear-gradient(135deg, #16b876, #3ddc97);
}

.amap-info .info-name {
  font-size: 15px;
  font-weight: 700;
  color: #0f1f1a;
}

.amap-info .info-city {
  font-size: 12px;
  color: #8b9a94;
  margin-top: 2px;
}

.amap-info .info-row {
  display: flex;
  gap: 8px;
  font-size: 12.5px;
  line-height: 1.9;
  color: #43564f;
}

.amap-info .info-row span {
  color: #8b9a94;
  flex-shrink: 0;
  width: 34px;
}

.amap-info .info-row b {
  font-weight: 600;
  color: #0f1f1a;
  word-break: break-all;
}

.amap-info .info-btns {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

.amap-info .btn {
  flex: 1;
  height: 36px;
  border-radius: 11px;
  border: none;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.18s ease;
}

.amap-info .btn.ghost {
  background: #f1f8f4;
  color: #43564f;
}

.amap-info .btn.ghost:hover {
  background: #e4f4ea;
}

.amap-info .btn.primary {
  background: linear-gradient(135deg, #16b876, #3ddc97);
  color: #fff;
  box-shadow: 0 8px 18px rgba(22, 184, 118, 0.32);
}

.amap-info .btn.primary:hover {
  transform: translateY(-1px);
}

/* 高德自带控件圆角化 */
.amap-geolocation-con,
.amap-logo,
.amap-copyright {
  opacity: 0.85;
}
</style>
