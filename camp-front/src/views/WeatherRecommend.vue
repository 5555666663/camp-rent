<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-head">
      <h2 class="page-title">露营装备智能推荐</h2>
      <p class="page-subtitle">选择出行日期与目的地，系统结合当日天气为你智能推荐合适的露营装备组合</p>
    </div>

    <!-- 查询表单 -->
    <div class="query-card">
      <el-form :inline="true" :model="form" class="query-form">
        <el-form-item label="出行日期">
          <el-date-picker
            v-model="form.date"
            type="date"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
            placeholder="仅可选未来 7 天"
            style="width: 190px"
          />
        </el-form-item>
        <el-form-item label="目的地">
          <el-input
            v-model="form.location"
            placeholder="如：北京 怀柔 / 杭州 千岛湖"
            clearable
            style="width: 260px"
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">智能推荐</el-button>
        </el-form-item>
      </el-form>
      <p class="query-tip">提示：仅支持查询未来 7 天内的天气，超范围会直接提示且不调用 AI。</p>
    </div>

    <!-- 推荐结果 -->
    <template v-if="result">
      <!-- 风险提示条 -->
      <div class="risk-banner" :class="'risk-' + riskLevelKey">
        <div class="risk-icon">{{ riskIcon }}</div>
        <div class="risk-body">
          <div class="risk-title">
            风险等级：{{ result.riskLevel }}
            <span class="risk-scene">推荐场景：{{ result.sceneType }}</span>
          </div>
          <div class="risk-tip">{{ result.riskTip }}</div>
          <div v-if="result.remindText" class="risk-remind">📌 {{ result.remindText }}</div>
        </div>
      </div>

      <!-- 天气要素 -->
      <div class="weather-row" v-if="result.weather">
        <div class="weather-chip">
          <span class="chip-label">天气</span>
          <span class="chip-value">{{ result.weather.weatherText }}</span>
        </div>
        <div class="weather-chip">
          <span class="chip-label">温度</span>
          <span class="chip-value">{{ result.weather.tempMin }}~{{ result.weather.tempMax }}℃</span>
        </div>
        <div class="weather-chip">
          <span class="chip-label">降水概率</span>
          <span class="chip-value">{{ result.weather.precipitationProbability }}%</span>
        </div>
        <div class="weather-chip">
          <span class="chip-label">风力</span>
          <span class="chip-value">{{ result.weather.windSpeed }} km/h</span>
        </div>
        <div class="weather-chip">
          <span class="chip-label">紫外线</span>
          <span class="chip-value">{{ result.weather.uvIndex }}</span>
        </div>
        <div class="weather-chip" :class="{ danger: result.weather.thunderAlert }">
          <span class="chip-label">雷电预警</span>
          <span class="chip-value">{{ result.weather.thunderAlert ? '有' : '无' }}</span>
        </div>
      </div>

      <!-- 推荐装备组合 -->
      <h3 class="section-title">推荐装备组合</h3>
      <div class="pkg-grid">
        <div
          v-for="pkg in result.recommendPackage"
          :key="pkg.id"
          class="pkg-card"
          @click="goPackage(pkg.id)"
        >
          <div class="pkg-cover" :style="coverStyle(pkg.coverImg)">
            <span class="pkg-scene">{{ pkg.sceneType }}</span>
          </div>
          <div class="pkg-body">
            <div class="pkg-name">{{ pkg.packageName }}</div>
            <div class="pkg-desc">{{ pkg.description }}</div>
            <div class="pkg-items">
              <span v-for="it in pkg.items" :key="it.equipmentId" class="pkg-item">{{ it.eqName }}×{{ it.quantity }}</span>
            </div>
            <div class="pkg-foot">
              <div class="pkg-price">
                <span class="price-num">¥{{ pkg.price }}</span><span class="price-unit">/天</span>
                <span v-if="pkg.originalPrice" class="price-origin">¥{{ pkg.originalPrice }}</span>
              </div>
              <span class="pkg-link">查看组合 →</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 附加装备 -->
      <template v-if="result.optionalAdd && result.optionalAdd.length">
        <h3 class="section-title">建议补充装备</h3>
        <div class="add-grid">
          <div
            v-for="eq in result.optionalAdd"
            :key="eq.id"
            class="add-card"
            @click="goOrder(eq.id)"
          >
            <div class="add-img" :style="coverStyle(eq.eqImg)"></div>
            <div class="add-info">
              <div class="add-name">{{ eq.eqName }}</div>
              <div class="add-price">¥{{ eq.pricePerDay }}/天 · 押金 ¥{{ eq.deposit }}</div>
            </div>
            <span class="add-link">单独租 →</span>
          </div>
        </div>
      </template>

      <p class="result-foot">
        推荐来源：{{ result.source === 'ai' ? '大模型' : '本地规则引擎（未配置大模型 Key 时自动兜底）' }}
        ｜ 查询日期：{{ result.date }} ｜ 地点：{{ result.location }}
      </p>
    </template>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRecommend } from '@/api/weather'
import { useUserStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  date: '',
  location: ''
})
const loading = ref(false)
const result = ref(null)

/** 限制日期只能选未来 7 天内（含今天） */
const disabledDate = (time) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const max = new Date(today)
  max.setDate(max.getDate() + 7)
  return time.getTime() < today.getTime() || time.getTime() > max.getTime()
}

/** 提交查询 */
const onSubmit = async () => {
  if (!form.date) {
    ElMessage.warning('请选择出行日期')
    return
  }
  if (!form.location || !form.location.trim()) {
    ElMessage.warning('请输入露营或徒步地点')
    return
  }
  loading.value = true
  try {
    const data = await getRecommend({ date: form.date, location: form.location.trim() })
    result.value = data
  } catch (e) {
    result.value = null
  } finally {
    loading.value = false
  }
}

/** 风险等级 -> 样式后缀 */
const riskLevelKey = computed(() => {
  const level = result.value?.riskLevel || ''
  if (level.includes('高危')) return 'danger'
  if (level.includes('注意')) return 'warn'
  return 'safe'
})

const riskIcon = computed(() => {
  const level = result.value?.riskLevel || ''
  if (level.includes('高危')) return '⛈'
  if (level.includes('注意')) return '🌤'
  return '☀️'
})

/** 图片背景样式 */
const coverStyle = (img) => {
  return img
    ? { backgroundImage: `url(${img})` }
    : { background: '#dbeafe' }
}

/** 跳转套餐详情页 */
const goPackage = (id) => {
  router.push(`/package/${id}`)
}

/** 单独租赁某件装备 */
const goOrder = (equipmentId) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再下单')
    router.push({ path: '/login', query: { redirect: `/order?equipmentId=${equipmentId}` } })
    return
  }
  router.push({ path: '/order', query: { equipmentId } })
}
</script>

<style scoped>
.page-head {
  margin-bottom: 18px;
}

.query-card {
  background: #fff;
  border-radius: var(--camp-radius, 16px);
  padding: 18px 20px 8px;
  box-shadow: 0 10px 30px rgba(15, 31, 26, 0.06);
  border: 1px solid var(--camp-line, rgba(15, 31, 26, 0.07));
  margin-bottom: 22px;
}

.query-tip {
  margin: 0 0 10px;
  font-size: 12px;
  color: var(--camp-muted, #8b9a94);
}

/* 风险提示 */
.risk-banner {
  display: flex;
  gap: 16px;
  padding: 20px 22px;
  border-radius: 18px;
  margin-bottom: 18px;
  border: 1px solid transparent;
}

.risk-safe {
  background: #eff6ff;
  border-color: rgba(30, 64, 175, 0.25);
}

.risk-warn {
  background: #fff7e8;
  border-color: rgba(230, 162, 60, 0.3);
}

.risk-danger {
  background: #fdecec;
  border-color: rgba(220, 53, 69, 0.3);
}

.risk-icon {
  font-size: 34px;
  line-height: 1;
}

.risk-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--camp-ink, #0f1f1a);
  margin-bottom: 8px;
}

.risk-scene {
  margin-left: 12px;
  font-size: 13px;
  font-weight: 500;
  color: var(--camp-primary-dark, #1e40af);
  background: rgba(30, 64, 175, 0.1);
  padding: 2px 10px;
  border-radius: 999px;
}

.risk-tip {
  font-size: 14px;
  line-height: 1.7;
  color: var(--camp-ink-2, #43564f);
}

.risk-remind {
  margin-top: 8px;
  font-size: 13px;
  color: var(--camp-muted, #8b9a94);
}

/* 天气要素 */
.weather-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 26px;
}

.weather-chip {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 108px;
  padding: 10px 16px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid var(--camp-line, rgba(15, 31, 26, 0.07));
}

.weather-chip.danger {
  border-color: rgba(220, 53, 69, 0.4);
  background: #fff5f5;
}

.chip-label {
  font-size: 12px;
  color: var(--camp-muted, #8b9a94);
}

.chip-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--camp-ink, #0f1f1a);
}

/* 区块标题 */
.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--camp-ink, #0f1f1a);
  margin: 0 0 14px;
  padding-left: 12px;
  border-left: 4px solid var(--camp-primary, #1e40af);
  line-height: 1.2;
}

/* 推荐装备组合卡片 */
.pkg-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 26px;
}

.pkg-card {
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid var(--camp-line, rgba(15, 31, 26, 0.07));
  box-shadow: 0 10px 30px rgba(15, 31, 26, 0.06);
  cursor: pointer;
  transition: transform 0.25s, box-shadow 0.25s;
}

.pkg-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 38px rgba(15, 31, 26, 0.12);
}

.pkg-cover {
  height: 150px;
  background-size: cover;
  background-position: center;
  position: relative;
}

.pkg-scene {
  position: absolute;
  left: 12px;
  top: 12px;
  font-size: 12px;
  color: #fff;
  background: rgba(10, 60, 40, 0.55);
  backdrop-filter: blur(4px);
  padding: 3px 10px;
  border-radius: 999px;
}

.pkg-body {
  padding: 14px 16px 16px;
}

.pkg-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--camp-ink, #0f1f1a);
  margin-bottom: 6px;
}

.pkg-desc {
  font-size: 13px;
  line-height: 1.6;
  color: var(--camp-muted, #8b9a94);
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pkg-items {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.pkg-item {
  font-size: 12px;
  color: var(--camp-primary-dark, #1e40af);
  background: var(--camp-primary-soft, #eff6ff);
  padding: 2px 8px;
  border-radius: 6px;
}

.pkg-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price-num {
  font-size: 20px;
  font-weight: 800;
  color: #ff6a3d;
}

.price-unit {
  font-size: 12px;
  color: var(--camp-muted, #8b9a94);
}

.price-origin {
  margin-left: 8px;
  font-size: 12px;
  color: #b9c4bf;
  text-decoration: line-through;
}

.pkg-link {
  font-size: 13px;
  font-weight: 600;
  color: var(--camp-primary, #1e40af);
}

/* 附加装备 */
.add-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 14px;
}

.add-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border-radius: 14px;
  padding: 10px 14px;
  border: 1px solid var(--camp-line, rgba(15, 31, 26, 0.07));
  cursor: pointer;
  transition: box-shadow 0.25s;
}

.add-card:hover {
  box-shadow: 0 10px 24px rgba(15, 31, 26, 0.1);
}

.add-img {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  background-size: cover;
  background-position: center;
  flex-shrink: 0;
}

.add-info {
  flex: 1;
}

.add-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--camp-ink, #0f1f1a);
}

.add-price {
  margin-top: 4px;
  font-size: 12px;
  color: var(--camp-muted, #8b9a94);
}

.add-link {
  font-size: 13px;
  color: var(--camp-primary, #1e40af);
  font-weight: 600;
}

.result-foot {
  margin-top: 24px;
  font-size: 12px;
  color: var(--camp-muted, #8b9a94);
  text-align: center;
}
</style>
