<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h2 class="page-title">就近网点</h2>
        <p class="page-subtitle">
          开启定位，为你按距离由近到远推荐露营网点
          <span v-if="locInfo" class="loc-badge" :class="locInfo.source">
            {{ locText }}
          </span>
        </p>
      </div>
      <div class="head-actions">
        <el-button @click="openPicker">
          <el-icon><Position /></el-icon>
          在地图上选点
        </el-button>
        <el-button type="primary" :loading="locating" @click="locate">
          <el-icon><LocationFilled /></el-icon>
          {{ stores.length ? '重新定位' : '获取我的位置' }}
        </el-button>
      </div>
    </div>

    <!-- 手动在地图上点选自己的位置（定位不准时最可靠） -->
    <el-dialog
      v-model="pickerVisible"
      title="在地图上点选你的位置"
      width="760px"
      destroy-on-close
      @opened="initPicker"
    >
      <div ref="pickerEl" class="pick-map"></div>
      <p class="pick-tip">在地图上点击你所在的位置，系统会以该点重新计算各网点距离</p>
      <template #footer>
        <el-button @click="pickerVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!pickPoint" @click="confirmPick">
          使用该位置
        </el-button>
      </template>
    </el-dialog>

    <el-alert
      v-if="error"
      :title="error"
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

    <!-- 手动按城市查看（定位不准或不可用时可直接切换） -->
    <div v-if="cities.length" class="city-picker">
      <span class="city-label">位置不准或定位失败？直接选择城市：</span>
      <div class="city-chips">
        <button
          v-for="c in cities"
          :key="c"
          class="city-chip"
          :class="{ active: pickedCity === c }"
          @click="pickCity(c)"
        >
          {{ c }}
        </button>
      </div>
    </div>

    <div v-if="!stores.length && !locating" class="empty-box">
      <el-icon :size="42" color="#b8ccc2"><LocationInformation /></el-icon>
      <p>点击右上角「获取我的位置」，即可查看离你最近的网点</p>
      <el-button text type="primary" @click="$router.push('/map')">或在地图上浏览全部网点</el-button>
    </div>

    <div v-else class="store-grid" v-loading="locating">
      <div v-for="(s, i) in stores" :key="s.id" class="store-card" @click="go(s.id)">
        <div class="rank">{{ i + 1 }}</div>
        <div class="store-main">
          <div class="store-title">
            <span class="name">{{ s.storeName }}</span>
            <span class="distance" v-if="s.distanceKm !== null && s.distanceKm !== undefined">
              {{ s.distanceKm }} km
            </span>
          </div>
          <div class="row"><el-icon><Location /></el-icon>{{ s.city }} · {{ s.address }}</div>
          <div class="row"><el-icon><Phone /></el-icon>{{ s.phone }}</div>
          <div class="row" v-if="s.businessHours"><el-icon><Clock /></el-icon>{{ s.businessHours }}</div>
          <div class="remark" v-if="s.remark">备注：{{ s.remark }}</div>
        </div>
        <div class="store-actions">
          <el-button type="primary" plain round size="small" @click.stop="go(s.id)">查看网点</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  LocationFilled,
  LocationInformation,
  Location,
  Phone,
  Clock,
  Position
} from '@element-plus/icons-vue'
import { getNearestStores, getIpLocation, getStoreCities } from '@/api/public'
import { loadAMap } from '@/utils/amap'

const router = useRouter()
const stores = ref([])
const locating = ref(false)
const error = ref('')
// 定位信息：{ source: 'gps' | 'ip' | 'city', accuracy: 米, city }
const locInfo = ref(null)
// 定位全部失败时展示城市兜底选择
const cities = ref([])
const showCityPicker = ref(false)
const pickedCity = ref('')

const locText = computed(() => {
  if (!locInfo.value) return ''
  const src = locInfo.value.source
  if (src === 'gps') {
    const acc = locInfo.value.accuracy
    return acc ? `GPS/Wi-Fi 定位 · 精度约 ${Math.round(acc)} m` : 'GPS/Wi-Fi 定位'
  }
  if (src === 'pick') return '手动选点定位'
  if (src === 'city') return `按城市筛选 · ${locInfo.value.city}`
  return `IP 定位 · ${locInfo.value.city || '城市级'}`
})

/* ---------------- 地图选点 ---------------- */
const pickerVisible = ref(false)
const pickerEl = ref(null)
const pickPoint = ref(null)
let pickMap = null
let pickMarker = null

function openPicker() {
  pickPoint.value = null
  pickerVisible.value = true
}

async function initPicker() {
  try {
    const AMap = await loadAMap()
    await nextTick()
    // 默认展示全国视野，便于快速找到自己的城市（拖动/缩放后点击选点）
    pickMap = new AMap.Map(pickerEl.value, { zoom: 5, center: [108.95, 34.27], viewMode: '2D' })
    pickMap.on('click', (e) => {
      pickPoint.value = e.lnglat
      if (pickMarker) {
        pickMarker.setPosition(e.lnglat)
      } else {
        pickMarker = new AMap.Marker({ position: e.lnglat, map: pickMap })
      }
    })
  } catch (e) {
    ElMessage.error('地图加载失败，请稍后重试')
  }
}

async function confirmPick() {
  if (!pickPoint.value) return
  const longitude = Number(Number(pickPoint.value.getLng()).toFixed(6))
  const latitude = Number(Number(pickPoint.value.getLat()).toFixed(6))
  pickerVisible.value = false
  locating.value = true
  error.value = ''
  try {
    await applyLocation(longitude, latitude, 'pick', null, null)
    ElMessage.success('已按所选位置重新排序')
  } catch (e) {
    error.value = '网点查询失败，请稍后再试'
  } finally {
    locating.value = false
  }
}

// 记录上次定位，便于网点详情页复用（避免重复授权）
const CACHE_KEY = 'camp_location'

/** 浏览器高精度定位（GPS / Wi-Fi / 基站），拿到最精确的坐标 */
function browserPosition() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('当前浏览器不支持定位'))
      return
    }
    navigator.geolocation.getCurrentPosition(resolve, reject, {
      enableHighAccuracy: true, // 优先 GPS / Wi-Fi，而不是仅用 IP
      timeout: 12000,
      maximumAge: 0
    })
  })
}

/** 用坐标查最近网点并记录定位来源 */
async function applyLocation(longitude, latitude, source, accuracy, city) {
  localStorage.setItem(CACHE_KEY, JSON.stringify({ longitude, latitude }))
  stores.value = await getNearestStores({ longitude, latitude })
  locInfo.value = { source, accuracy, city }
  showCityPicker.value = false
}

/** 手动按城市兜底：以该城市网点中心点算距离 */
async function pickCity(city) {
  locating.value = true
  error.value = ''
  try {
    pickedCity.value = city
    stores.value = await getNearestStores({ city })
    locInfo.value = { source: 'city', city }
    showCityPicker.value = false
  } catch (e) {
    error.value = '网点查询失败，请稍后再试'
  } finally {
    locating.value = false
  }
}

async function locate() {
  if (locating.value) return
  locating.value = true
  error.value = ''
  try {
    // ① 浏览器高精度定位（GPS / Wi-Fi，最准）
    try {
      const pos = await browserPosition()
      await applyLocation(
        Number(pos.coords.longitude.toFixed(6)),
        Number(pos.coords.latitude.toFixed(6)),
        'gps',
        pos.coords.accuracy
      )
      return
    } catch (e) {
      // 用户明确拒绝授权时，直接提示，不再走 IP 兜底
      if (e && e.code === 1) {
        error.value = '定位权限被拒绝：请点击地址栏左侧的权限图标，允许「位置」后重试；也可直接选择下方城市'
        showCityPicker.value = true
        return
      }
    }

    // ② IP 定位兜底（城市级精度，无需授权）
    try {
      const ip = await getIpLocation()
      if (ip && ip.longitude) {
        await applyLocation(ip.longitude, ip.latitude, 'ip', null, ip.city)
        ElMessage.info(`已使用 IP 定位：${ip.city || '当前城市'}（城市级精度）`)
        return
      }
    } catch (e) {
      // 忽略，走城市兜底
    }

    // ③ 城市兜底
    showCityPicker.value = true
    error.value = '精确定位不可用：请允许浏览器定位权限，或在下方选择所在城市'
  } finally {
    locating.value = false
  }
}

onMounted(async () => {
  try {
    cities.value = (await getStoreCities()) || []
  } catch (e) {
    cities.value = []
  }
})

function go(id) {
  router.push(`/store/${id}`)
}
</script>

<style scoped>
.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: none;
}

.pick-map {
  width: 100%;
  height: 380px;
  border: 1px solid var(--camp-line, #e5e7eb);
  border-radius: var(--camp-radius, 12px);
  overflow: hidden;
}

.pick-tip {
  margin: 10px 0 0;
  font-size: 13px;
  color: var(--camp-muted, #64748b);
}

.empty-box {
  background: #fff;
  border-radius: var(--camp-radius, 16px);
  padding: 60px 20px;
  text-align: center;
  color: var(--camp-muted, #8b9a94);
  box-shadow: 0 10px 30px rgba(15, 31, 26, 0.06);
}

.empty-box p {
  margin: 12px 0 6px;
}

.store-grid {
  display: grid;
  gap: 14px;
}

.store-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  background: #fff;
  border-radius: var(--camp-radius, 16px);
  box-shadow: 0 8px 24px rgba(15, 31, 26, 0.06);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.store-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 34px rgba(15, 31, 26, 0.1);
}

.rank {
  width: 34px;
  height: 34px;
  flex: none;
  border-radius: 50%;
  background: #1e40af;
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.store-main {
  flex: 1;
  min-width: 0;
}

.store-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.store-title .name {
  font-size: 16px;
  font-weight: 700;
  color: var(--camp-ink, #0f1f1a);
}

.distance {
  font-size: 12px;
  font-weight: 700;
  color: #1e40af;
  background: #eff6ff;
  padding: 2px 10px;
  border-radius: 999px;
}

.row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--camp-ink-2, #43564f);
  margin-top: 3px;
}

.remark {
  margin-top: 6px;
  font-size: 12px;
  color: var(--camp-muted, #8b9a94);
}

.store-actions {
  flex: none;
}

/* 城市兜底选择 */
.city-picker {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  background: #fff;
  border: 1px solid var(--camp-line, #e5e7eb);
  border-radius: var(--camp-radius, 12px);
  padding: 14px 16px;
  margin-bottom: 16px;
}

.city-label {
  font-size: 13px;
  color: var(--camp-muted, #64748b);
}

.city-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.city-chip {
  border: 1px solid var(--camp-line, #e5e7eb);
  background: #fff;
  color: var(--camp-ink-2, #334155);
  font-size: 13px;
  line-height: 20px;
  padding: 4px 14px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.18s ease-out;
}

.city-chip:hover {
  border-color: #bfdbfe;
  color: var(--camp-primary, #1e40af);
  background: var(--camp-primary-soft, #eff6ff);
}

.city-chip.active {
  background: var(--camp-primary, #1e40af);
  border-color: var(--camp-primary, #1e40af);
  color: #fff;
}

/* 定位来源徽标 */
.loc-badge {
  display: inline-block;
  margin-left: 8px;
  padding: 1px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
  line-height: 20px;
  border: 1px solid #dbeafe;
  background: var(--camp-primary-soft, #eff6ff);
  color: var(--camp-primary, #1e40af);
  vertical-align: 1px;
}

.loc-badge.ip {
  border-color: var(--camp-line, #e5e7eb);
  background: #f8fafc;
  color: var(--camp-muted, #64748b);
}
</style>
