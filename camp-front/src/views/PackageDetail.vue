<template>
  <div class="page-container" v-if="pkg">
    <!-- 面包屑 -->
    <div class="crumb">
      <router-link to="/recommend">装备推荐</router-link>
      <span class="sep">/</span>
      <span>{{ pkg.packageName }}</span>
    </div>

    <!-- 套餐头部 -->
    <div class="pkg-hero">
      <div class="hero-img" :style="pkg.coverImg ? { backgroundImage: `url(${pkg.coverImg})` } : {}"></div>
      <div class="hero-info">
        <div class="hero-tags">
          <span class="tag">{{ pkg.sceneType }}</span>
          <span class="tag light">套餐 · {{ pkg.items ? pkg.items.length : 0 }} 件装备</span>
        </div>
        <h1 class="hero-title">{{ pkg.packageName }}</h1>
        <p class="hero-desc">{{ pkg.description }}</p>
        <div class="hero-price">
          <span class="num">¥{{ pkg.price }}</span>
          <span class="unit">/天</span>
          <span v-if="pkg.originalPrice" class="origin">原价 ¥{{ pkg.originalPrice }}</span>
        </div>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="openOrderDialog(false)">一键预订整个套餐</el-button>
          <el-button size="large" @click="openOrderDialog(true)">自己组合装备</el-button>
        </div>
      </div>
    </div>

    <!-- 套餐内装备 -->
    <h3 class="section-title">套餐包含装备</h3>
    <div class="item-list">
      <div class="item" v-for="it in pkg.items" :key="it.equipmentId">
        <div class="item-img" :style="it.eqImg ? { backgroundImage: `url(${it.eqImg})` } : {}"></div>
        <div class="item-info">
          <div class="item-name">{{ it.eqName }} <span class="qty">×{{ it.quantity }}</span></div>
          <div class="item-meta">日租 ¥{{ it.pricePerDay }} · 押金 ¥{{ it.deposit }} · 库存 {{ it.stockNum }}</div>
        </div>
        <span class="single-link" @click="goSingleOrder(it.equipmentId)">单独租 →</span>
      </div>
    </div>

    <!-- 下单弹窗 -->
    <el-dialog v-model="dialogVisible" :title="customMode ? '自己组合装备下单' : '一键预订套餐'" width="620px">
      <el-form :model="orderForm" label-width="92px">
        <el-form-item label="取件网点" required>
          <el-select v-model="orderForm.storeId" placeholder="请选择取件网点" style="width: 100%" @change="onStoreChange">
            <el-option v-for="s in stores" :key="s.id" :label="`${s.storeName}（${s.city}）`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="归还网点" required>
          <el-select v-model="orderForm.returnStoreId" placeholder="请选择归还网点" style="width: 100%">
            <el-option v-for="s in stores" :key="s.id" :label="`${s.storeName}（${s.city}）`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="租赁时间" required>
          <el-date-picker
            v-model="orderForm.timeRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="归还时间"
            style="width: 100%"
          />
        </el-form-item>

        <template v-if="customMode">
          <el-divider content-position="left">选择套餐内装备</el-divider>
          <div class="custom-items">
            <div class="custom-item" v-for="it in pkg.items" :key="it.equipmentId">
              <el-checkbox v-model="customSelected[it.equipmentId]" :label="it.eqName" />
              <el-input-number v-model="customQty[it.equipmentId]" :min="1" :max="10" size="small" />
            </div>
          </div>
          <el-divider content-position="left">额外加购装备（同一网点）</el-divider>
          <el-select v-model="extraIds" multiple placeholder="可选，从该网点其他装备中选择" style="width: 100%">
            <el-option v-for="e in extraEquipments" :key="e.id" :label="`${e.eqName}（¥${e.pricePerDay}/天）`" :value="e.id" />
          </el-select>
          <p class="custom-tip">提示：套餐内装备需与取件网点同网点，跨网点库存不共享。</p>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitOrder">提交下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPackageDetail, orderPackage, orderCustomPackage } from '@/api/weather'
import { getStoreList, getEquipmentPage } from '@/api/public'

const route = useRoute()
const router = useRouter()

const pkg = ref(null)
const stores = ref([])
const dialogVisible = ref(false)
const customMode = ref(false)
const submitting = ref(false)
const extraEquipments = ref([])
const extraIds = ref([])
const customSelected = reactive({})
const customQty = reactive({})

const orderForm = reactive({
  storeId: null,
  returnStoreId: null,
  timeRange: []
})

/** 加载套餐详情 */
const load = async () => {
  pkg.value = await getPackageDetail(route.params.id)
  if (pkg.value?.items) {
    pkg.value.items.forEach((it) => {
      customSelected[it.equipmentId] = true
      customQty[it.equipmentId] = it.quantity || 1
    })
  }
}

/** 加载网点下拉 */
const loadStores = async () => {
  stores.value = await getStoreList()
}

/** 打开下单弹窗 */
const openOrderDialog = (custom) => {
  customMode.value = custom
  dialogVisible.value = true
  if (!orderForm.storeId && pkg.value?.items?.length) {
    // 默认取套餐内第一件装备的所属网点
    orderForm.storeId = pkg.value.items[0].storeId
    orderForm.returnStoreId = pkg.value.items[0].storeId
    onStoreChange(orderForm.storeId)
  }
}

/** 取件网点变化时，加载该网点其他装备用于加购 */
const onStoreChange = async (storeId) => {
  extraIds.value = []
  if (!storeId) {
    extraEquipments.value = []
    return
  }
  const page = await getEquipmentPage({ current: 1, size: 50, storeId, status: 1 })
  extraEquipments.value = (page.records || []).filter(
    (e) => !(pkg.value?.items || []).some((it) => it.equipmentId === e.id)
  )
}

/** 提交下单 */
const submitOrder = async () => {
  if (!orderForm.storeId) {
    ElMessage.warning('请选择取件网点')
    return
  }
  if (!orderForm.returnStoreId) {
    ElMessage.warning('请选择归还网点')
    return
  }
  if (!orderForm.timeRange || orderForm.timeRange.length !== 2) {
    ElMessage.warning('请选择租赁起止时间')
    return
  }
  const [start, end] = orderForm.timeRange
  if (new Date(end).getTime() <= new Date(start).getTime()) {
    ElMessage.warning('归还时间必须晚于租赁开始时间')
    return
  }

  const base = {
    storeId: orderForm.storeId,
    returnStoreId: orderForm.returnStoreId,
    rentStartTime: start,
    rentEndTime: end
  }

  submitting.value = true
  try {
    if (customMode.value) {
      const items = []
      pkg.value.items.forEach((it) => {
        if (customSelected[it.equipmentId]) {
          items.push({ equipmentId: it.equipmentId, quantity: customQty[it.equipmentId] || 1 })
        }
      })
      extraIds.value.forEach((id) => items.push({ equipmentId: id, quantity: 1 }))
      if (!items.length) {
        ElMessage.warning('请至少选择一件装备')
        submitting.value = false
        return
      }
      await orderCustomPackage({ ...base, items })
    } else {
      await orderPackage(pkg.value.id, base)
    }
    ElMessage.success('下单成功，可在「我的订单」中查看')
    dialogVisible.value = false
    router.push('/my-orders')
  } catch (e) {
    // 统一由请求拦截器提示错误
  } finally {
    submitting.value = false
  }
}

/** 单独租赁套餐内某件装备 */
const goSingleOrder = (equipmentId) => {
  router.push({ path: '/order', query: { equipmentId } })
}

onMounted(() => {
  load()
  loadStores()
})
</script>

<style scoped>
.crumb {
  font-size: 13px;
  color: var(--camp-muted, #8b9a94);
  margin-bottom: 16px;
}

.crumb a {
  color: var(--camp-primary, #1e40af);
  text-decoration: none;
}

.sep {
  margin: 0 6px;
}

.pkg-hero {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 28px;
  background: #fff;
  border-radius: var(--camp-radius);
  padding: 20px;
  border: 1px solid var(--camp-line, #e5e7eb);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
  margin-bottom: 24px;
}

.hero-img {
  height: 300px;
  border-radius: 16px;
  background: #dbeafe;
  background-size: cover;
  background-position: center;
}

.hero-info {
  display: flex;
  flex-direction: column;
}

.hero-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  font-size: 12px;
  color: #fff;
  background: #1e40af;
  padding: 3px 12px;
  border-radius: 999px;
}

.tag.light {
  background: var(--camp-primary-soft, #eff6ff);
  color: var(--camp-primary-dark, #1e40af);
}

.hero-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--camp-ink, #0f1f1a);
  margin: 0 0 12px;
}

.hero-desc {
  font-size: 14px;
  line-height: 1.8;
  color: var(--camp-muted, #8b9a94);
  margin: 0 0 18px;
}

.hero-price {
  margin-bottom: 20px;
}

.hero-price .num {
  font-size: 34px;
  font-weight: 800;
  color: #ff6a3d;
}

.hero-price .unit {
  font-size: 14px;
  color: var(--camp-muted, #8b9a94);
}

.hero-price .origin {
  margin-left: 10px;
  font-size: 13px;
  color: #b9c4bf;
  text-decoration: line-through;
}

.hero-actions {
  margin-top: auto;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--camp-ink, #0f1f1a);
  margin: 0 0 14px;
  padding-left: 12px;
  border-left: 4px solid var(--camp-primary, #1e40af);
  line-height: 1.2;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border-radius: 14px;
  padding: 12px 16px;
  border: 1px solid var(--camp-line, rgba(15, 31, 26, 0.07));
}

.item-img {
  width: 72px;
  height: 72px;
  border-radius: 10px;
  background: #dbeafe;
  background-size: cover;
  background-position: center;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--camp-ink, #0f1f1a);
}

.qty {
  font-size: 13px;
  color: var(--camp-primary-dark, #1e40af);
}

.item-meta {
  margin-top: 6px;
  font-size: 12px;
  color: var(--camp-muted, #8b9a94);
}

.single-link {
  font-size: 13px;
  font-weight: 600;
  color: var(--camp-primary, #1e40af);
  cursor: pointer;
  white-space: nowrap;
}

.single-link:hover {
  color: var(--camp-primary-dark, #1e40af);
}

.custom-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.custom-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0;
}

.custom-tip {
  margin: 10px 0 0;
  font-size: 12px;
  color: var(--camp-muted, #8b9a94);
}
</style>
