<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h2 class="page-title">就近网点</h2>
        <p class="page-subtitle">开启定位，为你按距离由近到远推荐露营网点</p>
      </div>
      <el-button type="primary" :loading="locating" @click="locate">
        <el-icon><LocationFilled /></el-icon>
        {{ stores.length ? '重新定位' : '获取我的位置' }}
      </el-button>
    </div>

    <el-alert
      v-if="error"
      :title="error"
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { LocationFilled, LocationInformation, Location, Phone, Clock } from '@element-plus/icons-vue'
import { getNearestStores } from '@/api/public'

const router = useRouter()
const stores = ref([])
const locating = ref(false)
const error = ref('')

// 记录上次定位，便于网点详情页复用（避免重复授权）
const CACHE_KEY = 'camp_location'

function locate() {
  if (!navigator.geolocation) {
    error.value = '当前浏览器不支持定位，请在地图页手动浏览网点'
    return
  }
  locating.value = true
  error.value = ''
  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      const longitude = Number(pos.coords.longitude.toFixed(6))
      const latitude = Number(pos.coords.latitude.toFixed(6))
      localStorage.setItem(CACHE_KEY, JSON.stringify({ longitude, latitude }))
      try {
        stores.value = await getNearestStores({ longitude, latitude })
      } catch (e) {
        error.value = '网点查询失败，请稍后再试'
      } finally {
        locating.value = false
      }
    },
    (err) => {
      locating.value = false
      const msg = err.code === 1 ? '定位权限被拒绝，请在浏览器设置中允许定位' : '定位失败，请检查网络或改用地图页浏览'
      error.value = msg
      ElMessage.warning(msg)
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
  )
}

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
</style>
