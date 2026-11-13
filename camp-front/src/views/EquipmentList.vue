<template>
  <div class="page-container fade-up">
    <div class="page-head">
      <div>
        <div class="page-title">装备浏览</div>
        <p class="page-subtitle">挑一件趁手的装备，把周末交给山野与晚风</p>
      </div>
      <div class="head-stat">
        <span class="stat-num">{{ total }}</span>
        <span class="stat-label">件商品在架</span>
      </div>
    </div>

    <!-- 商品大类切换：全部 / 露营装备 / 露营食品 -->
    <div class="type-tabs">
      <span
        v-for="t in typeTabs"
        :key="String(t.value)"
        class="type-tab"
        :class="{ active: query.productType === t.value }"
        @click="changeType(t.value)"
      >
        {{ t.label }}
      </span>
    </div>

    <div class="filter-bar float-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索装备名称"
        clearable
        style="width: 240px"
        :prefix-icon="Search"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 160px" @change="search">
        <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
      </el-select>
      <el-select v-model="query.storeId" placeholder="全部网点" clearable style="width: 190px" @change="search">
        <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
      </el-select>
      <el-button type="primary" round :icon="Search" @click="search">搜索</el-button>
      <el-button round :icon="RefreshLeft" @click="reset">重置</el-button>
    </div>

    <div v-loading="loading" class="card-grid">
      <el-empty v-if="!loading && !list.length" description="暂无符合条件的装备" />

      <div v-for="eq in list" :key="eq.id" class="eq-card camp-card" @click="goDetail(eq)">
        <div class="eq-img">
          <img v-if="eq.eqImg" :src="eq.eqImg" alt="" />
          <div v-else class="eq-img-placeholder">
            <span class="ph-icon">⛺</span>
          </div>
          <span class="cat-chip">{{ eq.categoryName || '其他' }}</span>
          <el-tag v-if="eq.status === 0" type="info" class="status-tag">已下架</el-tag>
        </div>

        <div class="eq-body">
          <div class="brand-row">
            <span v-if="eq.brand" class="brand-chip">{{ eq.brand }}</span>
            <span v-if="eq.spec" class="spec-text">{{ eq.spec }}</span>
          </div>
          <h3 class="eq-name">{{ eq.eqName }}</h3>
          <div class="eq-store">
            <el-icon><LocationFilled /></el-icon>
            <span>{{ eq.storeName }}</span>
          </div>

          <div class="eq-price-row">
            <div class="price">
              <span class="cur">¥</span>{{ isFood(eq) ? eq.salePrice : eq.pricePerDay }}<span class="unit">/{{ isFood(eq) ? '件' : '天' }}</span>
            </div>
            <span class="stock" :class="{ low: eq.stockNum <= 3 }">
              库存 {{ eq.stockNum }}
            </span>
          </div>

          <div class="eq-foot">
            <span v-if="isFood(eq)" class="deposit">一次性购买</span>
            <span v-else class="deposit">押金 ¥{{ eq.deposit }}</span>
            <span class="go">{{ isFood(eq) ? '去网点购买' : '查看详情' }} <span class="arrow">→</span></span>
          </div>
        </div>
      </div>
    </div>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[8, 12, 20]"
        background
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="search"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, RefreshLeft, LocationFilled } from '@element-plus/icons-vue'
import { getEquipmentPage, getCategoryList, getStoreList } from '@/api/public'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const categories = ref([])
const stores = ref([])
const query = reactive({
  current: 1,
  size: 8,
  keyword: '',
  categoryId: null,
  storeId: null,
  productType: null
})

/** 商品大类：全部 / 露营装备 / 露营食品 */
const typeTabs = [
  { label: '全部商品', value: null },
  { label: '露营装备', value: 1 },
  { label: '露营食品', value: 2 }
]

/** 是否为食品（食品为一次性售卖，不展示押金） */
function isFood(eq) {
  return eq.productType === 2
}

function changeType(v) {
  query.productType = v
  query.categoryId = null
  search()
}

onMounted(async () => {
  if (route.query.storeId) query.storeId = Number(route.query.storeId)
  const [catRes, storeRes] = await Promise.all([getCategoryList(), getStoreList()])
  categories.value = catRes || []
  stores.value = storeRes || []
  load()
})

function search() {
  query.current = 1
  load()
}

function reset() {
  query.keyword = ''
  query.categoryId = null
  query.storeId = null
  query.current = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const res = await getEquipmentPage(query)
    list.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function goDetail(eq) {
  // 食品为一次性售卖商品，直接去所属网点购买
  if (isFood(eq)) {
    router.push(`/store/${eq.storeId}`)
  } else {
    router.push(`/equipment/${eq.id}`)
  }
}
</script>

<style scoped>
.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.type-tabs {
  display: inline-flex;
  gap: 6px;
  padding: 5px;
  margin-bottom: 16px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid var(--camp-line);
  box-shadow: var(--camp-shadow-sm);
}

.type-tab {
  padding: 8px 20px;
  border-radius: 999px;
  font-size: 13.5px;
  font-weight: 600;
  color: var(--camp-ink-2);
  cursor: pointer;
  transition: all 0.22s ease;
}

.type-tab:hover {
  color: var(--camp-primary-dark);
}

.type-tab.active {
  color: #fff;
  background: var(--camp-primary);
  box-shadow: 0 8px 18px rgba(30, 64, 175, 0.28);
}

.head-stat {  display: flex;
  align-items: baseline;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid var(--camp-line);
  box-shadow: var(--camp-shadow-sm);
}

.stat-num {
  font-size: 22px;
  font-weight: 800;
  color: var(--camp-primary-dark);
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 12.5px;
  color: var(--camp-muted);
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 26px;
  flex-wrap: wrap;
}

/* 卡片网格 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(258px, 1fr));
  gap: 22px;
  min-height: 240px;
}

/* 装备卡片 */
.eq-card {
  cursor: pointer;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.eq-card:hover .eq-img-placeholder {
  transform: scale(1.08) rotate(-3deg);
}

.eq-card:hover .eq-img img {
  transform: scale(1.06);
}

.eq-img {
  position: relative;
  height: 168px;
  overflow: hidden;
  background: #eff6ff;
  display: grid;
  place-items: center;
}

.eq-img::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(120% 90% at 50% 0%, rgba(255, 255, 255, 0.65), transparent 55%);
  pointer-events: none;
}

.eq-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.22, 1, 0.36, 1);
}

.eq-img-placeholder {
  font-size: 52px;
  filter: drop-shadow(0 8px 16px rgba(30, 64, 175, 0.25));
  transition: transform 0.5s cubic-bezier(0.22, 1, 0.36, 1);
}

.cat-chip {
  position: absolute;
  left: 12px;
  top: 12px;
  padding: 4px 11px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 700;
  color: var(--camp-primary-dark);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(6px);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.08);
  z-index: 2;
}

.status-tag {
  position: absolute;
  right: 12px;
  top: 12px;
  z-index: 2;
}

.eq-body {
  padding: 16px 16px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.eq-name {
  font-size: 16.5px;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--camp-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.brand-chip {
  display: inline-flex;
  align-items: center;
  height: 21px;
  padding: 0 9px;
  border-radius: 6px;
  background: #1e40af;
  color: #fff;
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.02em;
  box-shadow: 0 4px 10px rgba(30, 64, 175, 0.18);
}

.spec-text {
  font-size: 11.5px;
  color: var(--camp-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px;
}

.eq-store {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12.5px;
  color: var(--camp-muted);
}

.eq-store .el-icon {
  color: var(--camp-primary);
}

.eq-price-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 2px;
}

.price {
  font-size: 23px;
  font-weight: 800;
  color: #ff7043;
  letter-spacing: -0.03em;
  line-height: 1;
}

.price .cur {
  font-size: 14px;
  font-weight: 700;
  margin-right: 1px;
}

.price .unit {
  font-size: 12px;
  font-weight: 500;
  color: var(--camp-muted);
  margin-left: 2px;
  letter-spacing: 0;
}

.stock {
  font-size: 12px;
  color: var(--camp-ink-2);
  padding: 3px 9px;
  border-radius: 999px;
  background: #f1f7f4;
  font-weight: 600;
}

.stock.low {
  color: #e2484e;
  background: #fdeced;
}

.eq-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px dashed var(--camp-line);
  font-size: 12.5px;
}

.deposit {
  color: var(--camp-muted);
}

.go {
  color: var(--camp-primary-dark);
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.eq-card:hover .go .arrow {
  transform: translateX(3px);
}

.go .arrow {
  transition: transform 0.25s ease;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 36px;
}
</style>
