<template>
  <div class="page-container" v-loading="loading">
    <div class="breadcrumb">
      <el-link :underline="false" @click="$router.push('/nearest')">就近网点</el-link>
      <span class="sep">/</span>
      <span>{{ store.storeName || '网点详情' }}</span>
    </div>

    <div class="store-hero" v-if="store.id">
      <div class="hero-left">
        <h2>{{ store.storeName }}</h2>
        <div class="hero-tags">
          <span class="tag city">{{ store.city }}</span>
          <span class="tag dist" v-if="store.distanceKm !== null && store.distanceKm !== undefined">
            距你 {{ store.distanceKm }} km
          </span>
          <span class="tag" :class="store.status === 1 ? 'on' : 'off'">
            {{ store.status === 1 ? '营业中' : '已停用' }}
          </span>
        </div>
        <div class="hero-row"><el-icon><Location /></el-icon>{{ store.address }}</div>
        <div class="hero-row"><el-icon><Phone /></el-icon>{{ store.phone }}</div>
        <div class="hero-row"><el-icon><Clock /></el-icon>{{ store.businessHours }}</div>
        <div class="hero-row remark" v-if="store.remark"><el-icon><InfoFilled /></el-icon>{{ store.remark }}</div>
      </div>
      <div class="hero-right">
        <el-button type="primary" round @click="$router.push({ path: '/equipment', query: { storeId: store.id } })">
          浏览该网点装备
        </el-button>
        <el-button round @click="$router.push('/map')">在地图查看</el-button>
      </div>
    </div>

    <el-tabs v-model="tab" class="stock-tabs">
      <el-tab-pane :label="`可租装备 (${equipments.length})`" name="equipment">
        <el-empty v-if="!equipments.length" description="该网点暂无在架装备" />
        <div v-else class="goods-grid">
          <div v-for="it in equipments" :key="it.id" class="goods-card" @click="$router.push(`/equipment/${it.id}`)">
            <div class="cover">
              <img :src="it.eqImg || fallbackImg" :alt="it.eqName" />
              <span class="cat-chip">{{ it.categoryName }}</span>
            </div>
            <div class="goods-body">
              <div class="goods-name">{{ it.eqName }}</div>
              <div class="goods-spec" v-if="it.spec">{{ it.spec }}</div>
              <div class="goods-price">
                <b>¥{{ it.pricePerDay }}</b><span>/天</span>
              </div>
              <div class="goods-foot">
                <span>押金 ¥{{ it.deposit }}</span>
                <span class="stock">库存 {{ it.stockNum }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane :label="`可购食品 (${foods.length})`" name="food">
        <el-empty v-if="!foods.length" description="该网点暂无在售食品" />
        <div v-else class="goods-grid">
          <div v-for="it in foods" :key="it.id" class="goods-card">
            <div class="cover">
              <img :src="it.eqImg || fallbackImg" :alt="it.eqName" />
              <span class="cat-chip food">食品</span>
            </div>
            <div class="goods-body">
              <div class="goods-name">{{ it.eqName }}</div>
              <div class="goods-spec" v-if="it.spec">{{ it.spec }}</div>
              <div class="goods-price"><b>¥{{ it.salePrice }}</b><span>/件</span></div>
              <div class="goods-foot">
                <span>库存 {{ it.stockNum }}</span>
                <el-input-number
                  v-model="cart[it.id]"
                  :min="0"
                  :max="it.stockNum"
                  size="small"
                  controls-position="right"
                  style="width: 96px"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="cart-bar" v-if="hasFood">
          <div class="cart-info">
            已选 <b>{{ totalCount }}</b> 件，合计 <span class="total">¥{{ totalAmount }}</span>
            <span class="tip">食品为一次性售卖，不支持归还</span>
          </div>
          <el-button type="primary" round :loading="submitting" @click="submitFood">提交购买</el-button>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Phone, Clock, InfoFilled } from '@element-plus/icons-vue'
import { getStoreDetailWithStock } from '@/api/public'
import { createFoodOrder } from '@/api/order'
import { useUserStore } from '@/store'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tab = ref('equipment')
const store = ref({})
const equipments = ref([])
const foods = ref([])
const cart = reactive({})
const fallbackImg = '/img/food/snack.svg'

const hasFood = computed(() => Object.values(cart).some((n) => n > 0))
const totalCount = computed(() => Object.values(cart).reduce((a, b) => a + (b || 0), 0))
const totalAmount = computed(() => {
  let sum = 0
  foods.value.forEach((f) => {
    const n = cart[f.id] || 0
    if (n > 0) sum += Number(f.salePrice || 0) * n
  })
  return sum.toFixed(2)
})

async function load() {
  loading.value = true
  try {
    // 复用「就近网点」页记录的用户位置，用于展示距离
    let params = {}
    const cached = localStorage.getItem('camp_location')
    if (cached) {
      const loc = JSON.parse(cached)
      params = { longitude: loc.longitude, latitude: loc.latitude }
    }
    const data = await getStoreDetailWithStock(route.params.id, params)
    store.value = data.store || {}
    equipments.value = data.equipments || []
    foods.value = data.foods || []
    foods.value.forEach((f) => {
      if (cart[f.id] === undefined) cart[f.id] = 0
    })
  } catch (e) {
    ElMessage.error('网点详情加载失败')
  } finally {
    loading.value = false
  }
}

async function submitFood() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再购买')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  const items = Object.keys(cart)
    .map((id) => ({ equipmentId: Number(id), quantity: cart[id] }))
    .filter((i) => i.quantity > 0)
  if (!items.length) {
    ElMessage.warning('请先选择食品')
    return
  }
  submitting.value = true
  try {
    await createFoodOrder({ storeId: store.value.id, items })
    ElMessage.success('购买成功，请到该网点自提')
    Object.keys(cart).forEach((k) => (cart[k] = 0))
    router.push('/my-orders')
  } catch (e) {
    // 错误信息已由 axios 拦截器统一提示
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--camp-muted, #8b9a94);
  margin-bottom: 14px;
}

.breadcrumb .sep {
  opacity: 0.6;
}

.store-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  background: #fff;
  border-radius: var(--camp-radius, 16px);
  padding: 24px 26px;
  box-shadow: 0 10px 30px rgba(15, 31, 26, 0.06);
  margin-bottom: 18px;
}

.store-hero h2 {
  margin: 0 0 10px;
  font-size: 22px;
  color: var(--camp-ink, #0f1f1a);
}

.hero-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.tag {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 999px;
  background: #f1f6f3;
  color: var(--camp-ink-2, #43564f);
}

.tag.city {
  background: #e9fbf2;
  color: #0e9b62;
}

.tag.dist {
  background: #fff4e5;
  color: #c07a1a;
}

.tag.on {
  background: #e9fbf2;
  color: #0e9b62;
}

.tag.off {
  background: #fdeaea;
  color: #c0392b;
}

.hero-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  color: var(--camp-ink-2, #43564f);
  margin-top: 6px;
}

.hero-row.remark {
  color: var(--camp-muted, #8b9a94);
}

.hero-right {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
}

.stock-tabs {
  background: #fff;
  border-radius: var(--camp-radius, 16px);
  padding: 8px 22px 22px;
  box-shadow: 0 10px 30px rgba(15, 31, 26, 0.05);
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 16px;
}

.goods-card {
  border: 1px solid var(--camp-line, rgba(15, 31, 26, 0.075));
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.goods-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 16px 32px rgba(15, 31, 26, 0.1);
}

.cover {
  position: relative;
  height: 158px;
  background: linear-gradient(135deg, #eefaf3, #dff3e9);
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cat-chip {
  position: absolute;
  left: 10px;
  top: 10px;
  font-size: 11px;
  padding: 3px 9px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  color: #16b876;
  font-weight: 600;
}

.cat-chip.food {
  color: #c07a1a;
}

.goods-body {
  padding: 12px 14px 14px;
}

.goods-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--camp-ink, #0f1f1a);
  margin-bottom: 4px;
}

.goods-spec {
  font-size: 12px;
  color: var(--camp-muted, #8b9a94);
  margin-bottom: 8px;
}

.goods-price b {
  font-size: 19px;
  color: #ef6c3e;
}

.goods-price span {
  font-size: 12px;
  color: var(--camp-muted, #8b9a94);
  margin-left: 2px;
}

.goods-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
  color: var(--camp-muted, #8b9a94);
}

.stock {
  color: #16b876;
}

.cart-bar {
  position: sticky;
  bottom: 12px;
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(10px);
  box-shadow: 0 12px 30px rgba(15, 31, 26, 0.14);
}

.cart-info {
  font-size: 14px;
  color: var(--camp-ink-2, #43564f);
}

.cart-info b {
  color: #16b876;
}

.cart-info .total {
  font-size: 18px;
  font-weight: 700;
  color: #ef6c3e;
}

.cart-info .tip {
  margin-left: 12px;
  font-size: 12px;
  color: var(--camp-muted, #8b9a94);
}
</style>
