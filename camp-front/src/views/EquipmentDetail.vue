<template>
  <div class="page-container fade-up" v-loading="loading">
    <template v-if="eq">
      <el-breadcrumb separator="/" class="crumb">
        <el-breadcrumb-item :to="{ path: '/equipment' }">装备浏览</el-breadcrumb-item>
        <el-breadcrumb-item>{{ eq.eqName }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="detail-grid">
        <!-- 左：图片 -->
        <div class="detail-img camp-card">
          <img v-if="eq.eqImg" :src="eq.eqImg" alt="" />
          <div v-else class="detail-img-placeholder">
            <span>⛺</span>
          </div>
          <span class="img-chip">{{ eq.categoryName || '露营装备' }}</span>
        </div>

        <!-- 右：信息 -->
        <div class="detail-info camp-card">
          <div class="info-head">
            <div v-if="eq.brand" class="brand-line">
              <span class="brand-chip">{{ eq.brand }}</span>
              <span v-if="eq.spec" class="spec-text">{{ eq.spec }}</span>
            </div>
            <h1 class="eq-name">{{ eq.eqName }}</h1>
            <div class="eq-tags">
              <el-tag type="success" effect="light">{{ eq.categoryName }}</el-tag>
              <el-tag type="info" effect="light">{{ eq.storeName }}</el-tag>
              <el-tag v-if="eq.status === 1" type="success" effect="dark">上架中</el-tag>
              <el-tag v-else type="danger" effect="dark">已下架</el-tag>
            </div>
          </div>

          <div class="price-block">
            <div class="price-main">
              <span class="cur">¥</span>
              <span class="num">{{ eq.pricePerDay }}</span>
              <span class="unit">/ 天</span>
            </div>
            <div class="price-side">
              <span class="side-label">押金</span>
              <span class="side-value">¥{{ eq.deposit }}</span>
            </div>
          </div>

          <div class="meta-list">
            <div class="meta-row">
              <span class="meta-label">归属网点</span>
              <span class="meta-value">{{ eq.storeName }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">当前库存</span>
              <span class="meta-value" :class="{ 'stock-low': eq.stockNum <= 3 }">
                {{ eq.stockNum }} 件
                <span v-if="eq.stockNum <= 3" class="stock-hint">（库存紧张）</span>
              </span>
            </div>
            <div class="meta-row">
              <span class="meta-label">品牌 / 规格</span>
              <span class="meta-value">{{ eq.brand || '—' }}<template v-if="eq.spec"> · {{ eq.spec }}</template></span>
            </div>
            <div class="meta-row">
              <span class="meta-label">计费方式</span>
              <span class="meta-value">按天计费，不足 1 天按 1 天计算</span>
            </div>
          </div>

          <div class="desc">
            <h4>商品介绍</h4>
            <p>{{ eq.description || '这件装备还没有填写介绍～' }}</p>
          </div>

          <div class="cta">
            <el-button
              type="primary"
              size="large"
              round
              :disabled="eq.status !== 1 || eq.stockNum <= 0"
              class="cta-btn"
              @click="goOrder"
            >
              {{ eq.status !== 1 ? '装备已下架' : eq.stockNum <= 0 ? '暂时无库存' : '立即租赁 →' }}
            </el-button>
            <span class="cta-hint">支持跨网点归还，取件网点库存实时锁定</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEquipmentDetail } from '@/api/public'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const eq = ref(null)

onMounted(async () => {
  loading.value = true
  try {
    eq.value = await getEquipmentDetail(route.params.id)
  } finally {
    loading.value = false
  }
})

function goOrder() {
  router.push(`/order?equipmentId=${eq.value.id}`)
}
</script>

<style scoped>
.crumb {
  margin-bottom: 22px;
  font-weight: 600;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 26px;
  align-items: start;
}

@media (max-width: 900px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

/* 图片 */
.detail-img {
  position: relative;
  height: 460px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #eff6ff;
}

.detail-img:hover {
  transform: none;
}

.detail-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-img-placeholder span {
  font-size: 110px;
  filter: drop-shadow(0 16px 28px rgba(30, 64, 175, 0.28));
}

.img-chip {
  position: absolute;
  left: 18px;
  top: 18px;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: var(--camp-primary-dark);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.1);
}

/* 信息 */
.detail-info {
  padding: 30px;
}

.detail-info:hover {
  transform: none;
}

.brand-line {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.brand-chip {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 11px;
  border-radius: 7px;
  background: #1e40af;
  color: #fff;
  font-size: 12.5px;
  font-weight: 700;
  letter-spacing: 0.02em;
  box-shadow: 0 5px 14px rgba(30, 64, 175, 0.2);
}

.spec-text {
  font-size: 12.5px;
  color: var(--camp-muted);
  padding: 3px 10px;
  border-radius: 7px;
  background: rgba(15, 31, 26, 0.045);
}

.eq-name {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--camp-ink);
  margin: 0 0 14px;
}

.eq-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.price-block {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 24px 0 20px;
  padding: 18px 20px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid rgba(30, 64, 175, 0.14);
}

.price-main {
  display: flex;
  align-items: baseline;
  color: #ff7043;
}

.price-main .cur {
  font-size: 20px;
  font-weight: 700;
}

.price-main .num {
  font-size: 40px;
  font-weight: 800;
  letter-spacing: -0.04em;
  line-height: 1;
}

.price-main .unit {
  font-size: 14px;
  color: var(--camp-muted);
  margin-left: 4px;
  font-weight: 600;
}

.price-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.side-label {
  font-size: 11.5px;
  color: var(--camp-muted);
  font-weight: 600;
}

.side-value {
  font-size: 18px;
  font-weight: 800;
  color: var(--camp-ink);
}

.meta-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  border-top: 1px dashed var(--camp-line);
  padding-top: 16px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 0;
  font-size: 13.5px;
}

.meta-label {
  color: var(--camp-muted);
  font-weight: 600;
  flex-shrink: 0;
}

.meta-value {
  color: var(--camp-ink-2);
  font-weight: 600;
  text-align: right;
}

.stock-low {
  color: #e2484e;
}

.stock-hint {
  font-size: 12px;
  font-weight: 600;
}

.desc {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px dashed var(--camp-line);
}

.desc h4 {
  margin: 0 0 8px;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--camp-ink);
}

.desc p {
  color: var(--camp-ink-2);
  font-size: 13.5px;
  line-height: 1.85;
  margin: 0;
}

.cta {
  margin-top: 26px;
}

.cta-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 700;
}

.cta-hint {
  display: block;
  text-align: center;
  margin-top: 10px;
  font-size: 12px;
  color: var(--camp-muted);
}
</style>
