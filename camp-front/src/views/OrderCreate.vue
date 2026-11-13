<template>
  <div class="page-container fade-up">
    <div class="page-head">
      <div class="page-title">租赁下单</div>
      <p class="page-subtitle">确认取件与归还网点、租赁时间，提交后即可在订单页支付</p>
    </div>

    <div class="order-grid" v-loading="loading">
      <!-- 左：表单 -->
      <div class="form-card camp-card">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
          <div class="eq-strip">
            <div class="eq-thumb">⛺</div>
            <div class="eq-meta">
              <div class="eq-name">{{ equipment ? equipment.eqName : '加载中...' }}</div>
              <div v-if="equipment" class="eq-sub">
                {{ equipment.storeName }} · 日租 ¥{{ equipment.pricePerDay }} · 押金 ¥{{ equipment.deposit }}
              </div>
            </div>
            <span v-if="equipment" class="eq-stock">库存 {{ equipment.stockNum }}</span>
          </div>

          <el-form-item label="取件网点" prop="pickStoreId">
            <el-select v-model="form.pickStoreId" placeholder="请选择取件网点" style="width: 100%" @change="onPickStoreChange">
              <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
            </el-select>
            <div v-if="equipment" class="tip">
              该装备归属网点：<b>{{ equipment.storeName }}</b>（下单仅扣减取件网点库存）
            </div>
          </el-form-item>

          <el-form-item label="归还网点" prop="returnStoreId">
            <el-select v-model="form.returnStoreId" placeholder="请选择归还网点（可与取件不同）" style="width: 100%">
              <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
            </el-select>
          </el-form-item>

          <div class="row-2">
            <el-form-item label="租赁数量" prop="quantity">
              <el-input-number
                v-model="form.quantity"
                :min="1"
                :max="equipment ? equipment.stockNum : 1"
                style="width: 100%"
              />
            </el-form-item>
          </div>

          <el-form-item label="租赁时间" prop="range">
            <el-date-picker
              v-model="form.range"
              type="datetimerange"
              range-separator="至"
              start-placeholder="租赁开始时间"
              end-placeholder="租赁结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 右：费用汇总 -->
      <aside class="summary camp-card">
        <h3 class="sum-title">费用明细</h3>

        <div class="sum-row">
          <span>日租金</span>
          <span class="v">¥{{ equipment ? equipment.pricePerDay : 0 }} / 天</span>
        </div>
        <div class="sum-row">
          <span>租赁天数</span>
          <span class="v">{{ days }} 天</span>
        </div>
        <div class="sum-row">
          <span>租赁数量</span>
          <span class="v">{{ form.quantity }} 件</span>
        </div>

        <div class="sum-divider"></div>

        <div class="sum-row">
          <span>租金小计</span>
          <span class="v">¥{{ totalPrice }}</span>
        </div>
        <div class="sum-row">
          <span>押金（可退）</span>
          <span class="v">¥{{ totalDeposit }}</span>
        </div>

        <div class="sum-total">
          <span class="label">应付合计</span>
          <span class="amount"><span class="cur">¥</span>{{ grandTotal }}</span>
        </div>

        <el-button type="primary" size="large" round class="submit-btn" :loading="submitting" @click="submit">
          提交订单
        </el-button>
        <el-button text class="back-btn" @click="$router.back()">返回</el-button>

        <p class="sum-hint">押金仅做记录，归还确认后自动退回</p>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getEquipmentDetail, getStoreList } from '@/api/public'
import { createOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const equipment = ref(null)
const stores = ref([])
const form = reactive({
  equipmentId: null,
  pickStoreId: null,
  returnStoreId: null,
  quantity: 1,
  range: []
})

const rules = {
  pickStoreId: [{ required: true, message: '请选择取件网点', trigger: 'change' }],
  returnStoreId: [{ required: true, message: '请选择归还网点', trigger: 'change' }],
  quantity: [{ required: true, message: '请选择数量', trigger: 'change' }],
  range: [{ required: true, message: '请选择租赁起止时间', trigger: 'change' }]
}

const days = computed(() => {
  if (!form.range || form.range.length !== 2) return 0
  const start = new Date(form.range[0].replace(' ', 'T'))
  const end = new Date(form.range[1].replace(' ', 'T'))
  const d = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
  return d > 0 ? d : 1
})

const totalPrice = computed(() => {
  if (!equipment.value) return '0.00'
  return (Number(equipment.value.pricePerDay) * days.value * form.quantity).toFixed(2)
})

const totalDeposit = computed(() => {
  if (!equipment.value) return '0.00'
  return (Number(equipment.value.deposit) * form.quantity).toFixed(2)
})

const grandTotal = computed(() => {
  return (Number(totalPrice.value) + Number(totalDeposit.value)).toFixed(2)
})

onMounted(async () => {
  loading.value = true
  try {
    const eqId = route.query.equipmentId
    const [eq, storeList] = await Promise.all([getEquipmentDetail(eqId), getStoreList()])
    equipment.value = eq
    stores.value = storeList || []
    form.equipmentId = eq.id
    // 默认取件网点 = 装备归属网点
    form.pickStoreId = eq.storeId
    form.returnStoreId = eq.storeId
  } finally {
    loading.value = false
  }
})

function onPickStoreChange(id) {
  if (equipment.value && Number(id) !== Number(equipment.value.storeId)) {
    ElMessage.warning('取件网点需与装备归属网点一致')
  }
}

function submit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    if (equipment.value && Number(form.pickStoreId) !== Number(equipment.value.storeId)) {
      ElMessage.warning('取件网点必须为装备归属网点')
      return
    }
    submitting.value = true
    try {
      await createOrder({
        equipmentId: form.equipmentId,
        pickStoreId: form.pickStoreId,
        returnStoreId: form.returnStoreId,
        quantity: form.quantity,
        rentStartTime: form.range[0],
        rentEndTime: form.range[1]
      })
      ElMessage.success('下单成功，请前往订单支付')
      router.push('/my-orders')
    } catch (e) {
      // 错误已由拦截器提示
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.page-head {
  margin-bottom: 24px;
}

.order-grid {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
  align-items: start;
}

@media (max-width: 980px) {
  .order-grid {
    grid-template-columns: 1fr;
  }
}

/* 表单卡片 */
.form-card {
  padding: 26px 28px 10px;
}

.form-card:hover,
.summary:hover {
  transform: none;
}

.eq-strip {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid rgba(30, 64, 175, 0.14);
  margin-bottom: 24px;
}

.eq-thumb {
  width: 46px;
  height: 46px;
  border-radius: 13px;
  display: grid;
  place-items: center;
  font-size: 22px;
  background: #fff;
  box-shadow: 0 6px 14px rgba(30, 64, 175, 0.14);
}

.eq-meta {
  flex: 1;
  min-width: 0;
}

.eq-name {
  font-size: 16px;
  font-weight: 800;
  color: var(--camp-ink);
}

.eq-sub {
  font-size: 12.5px;
  color: var(--camp-muted);
  margin-top: 3px;
}

.eq-stock {
  font-size: 12px;
  font-weight: 700;
  color: var(--camp-primary-dark);
  background: #fff;
  padding: 4px 11px;
  border-radius: 999px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
}

.row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.tip {
  font-size: 12px;
  color: var(--camp-muted);
  margin-top: 6px;
  line-height: 1.6;
}

.tip b {
  color: var(--camp-primary-dark);
}

/* 汇总卡片 */
.summary {
  position: sticky;
  top: 94px;
  padding: 24px;
}

.sum-title {
  font-size: 16px;
  font-weight: 800;
  letter-spacing: -0.01em;
  margin: 0 0 18px;
  color: var(--camp-ink);
}

.sum-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13.5px;
  color: var(--camp-ink-2);
  padding: 7px 0;
}

.sum-row .v {
  font-weight: 700;
  color: var(--camp-ink);
}

.sum-divider {
  height: 1px;
  background: var(--camp-line);
  margin: 10px 0;
}

.sum-total {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed var(--camp-line);
}

.sum-total .label {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--camp-ink-2);
}

.sum-total .amount {
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: #ff7043;
}

.sum-total .cur {
  font-size: 17px;
  font-weight: 700;
}

.submit-btn {
  width: 100%;
  height: 48px;
  margin-top: 20px;
  font-size: 15.5px;
  font-weight: 700;
}

.back-btn {
  width: 100%;
  margin-top: 8px;
  margin-left: 0;
}

.sum-hint {
  margin: 14px 0 0;
  text-align: center;
  font-size: 11.5px;
  color: var(--camp-muted);
}
</style>
