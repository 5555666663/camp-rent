<template>
  <div class="page-container fade-up">
    <div class="page-head">
      <div>
        <div class="page-title">我的订单</div>
        <p class="page-subtitle">查看租赁进度、支付、归还与评价</p>
      </div>
      <div class="head-stat">
        <span class="stat-num">{{ total }}</span>
        <span class="stat-label">笔订单</span>
      </div>
    </div>

    <el-tabs v-model="activeStatus" class="status-tabs" @tab-change="load">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待支付" name="0" />
      <el-tab-pane label="租赁中" name="1" />
      <el-tab-pane label="待归还" name="2" />
      <el-tab-pane label="已完成" name="3" />
      <el-tab-pane label="逾期" name="4" />
    </el-tabs>

    <div v-loading="loading" class="order-list">
      <el-empty v-if="!loading && !list.length" description="还没有订单，去挑一件装备吧" />

      <div v-for="o in list" :key="o.id" class="order-item camp-card" :class="'st-' + o.orderStatus">
        <span class="accent"></span>

        <div class="order-head">
          <span class="order-no">订单 #{{ o.id }}</span>
          <span class="order-time">{{ o.createTime }}</span>
          <el-tag :type="statusTag(o.orderStatus).type" effect="light" round>
            {{ statusTag(o.orderStatus).text }}
          </el-tag>
        </div>

        <div class="order-body">
          <div class="order-equipment">
            <img v-if="o.eqImg" :src="o.eqImg" alt="" class="eq-thumb" />
            <div v-else class="eq-thumb placeholder">⛺</div>

            <div class="eq-info">
              <div class="eq-name">{{ o.equipmentName }}</div>
              <div class="eq-line">
                <span class="route">
                  <span class="dot from"></span>{{ o.pickStoreName }}
                  <span class="arrow">→</span>
                  <span class="dot to"></span>{{ o.returnStoreName }}
                </span>
              </div>
              <div class="eq-time">🕐 {{ o.rentStartTime }} ~ {{ o.rentEndTime }}</div>
            </div>
          </div>

          <div class="order-amount">
            <div class="amount"><span class="cur">¥</span>{{ o.totalPrice }}</div>
            <div class="deposit">押金 ¥{{ o.depositMoney }}</div>
          </div>

          <div class="order-actions">
            <el-button size="small" round @click="showDetail(o.id)">详情</el-button>
            <el-button v-if="o.orderStatus === 0" size="small" type="primary" round @click="doPay(o.id)">支付</el-button>
            <el-button v-if="o.orderStatus === 1" size="small" type="warning" round @click="doApplyReturn(o.id)">发起归还</el-button>
            <el-button v-if="o.orderStatus === 2" size="small" type="success" round @click="doConfirmReturn(o.id)">归还确认</el-button>
            <el-button v-if="o.orderStatus === 3" size="small" type="primary" round plain @click="openComment(o)">评价</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        background
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="540px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="订单号">#{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="装备">{{ detail.equipmentName }}</el-descriptions-item>
        <el-descriptions-item label="取件网点">{{ detail.pickStoreName }}</el-descriptions-item>
        <el-descriptions-item label="归还网点">{{ detail.returnStoreName }}</el-descriptions-item>
        <el-descriptions-item label="租赁时间">{{ detail.rentStartTime }} ~ {{ detail.rentEndTime }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ detail.totalPrice }}</el-descriptions-item>
        <el-descriptions-item label="押金">¥{{ detail.depositMoney }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(detail.orderStatus).type" effect="light">{{ statusTag(detail.orderStatus).text }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="commentVisible" title="评价反馈" width="440px">
      <el-form label-position="top">
        <el-form-item label="评分">
          <el-rate v-model="commentForm.score" size="large" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="commentForm.content" type="textarea" :rows="4" placeholder="说说你的使用体验..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="commentVisible = false">取消</el-button>
        <el-button type="primary" round :loading="submitting" @click="submitComment">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyOrderPage, payOrder, applyReturn, confirmReturn, addComment } from '@/api/order'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const activeStatus = ref('')
const detailVisible = ref(false)
const detail = ref(null)
const commentVisible = ref(false)
const commentForm = reactive({ orderId: null, score: 5, content: '' })
const query = reactive({ current: 1, size: 10 })

const statusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '租赁中', type: 'primary' },
  2: { text: '待归还', type: 'info' },
  3: { text: '已完成', type: 'success' },
  4: { text: '逾期', type: 'danger' }
}
function statusTag(s) {
  return statusMap[s] || { text: '未知', type: 'info' }
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await getMyOrderPage({
      current: query.current,
      size: query.size,
      orderStatus: activeStatus.value || null
    })
    list.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function showDetail(id) {
  detail.value = list.value.find((o) => o.id === id)
  detailVisible.value = true
}

function doPay(id) {
  ElMessageBox.confirm('确认支付该订单？（模拟支付，无真实扣款）', '提示', { type: 'info' }).then(async () => {
    await payOrder(id)
    ElMessage.success('支付成功，租赁已开始')
    load()
  })
}

function doApplyReturn(id) {
  ElMessageBox.confirm('确认发起归还？', '提示', { type: 'warning' }).then(async () => {
    await applyReturn(id)
    ElMessage.success('已发起归还，待归还确认')
    load()
  })
}

function doConfirmReturn(id) {
  ElMessageBox.confirm('确认归还？归还后将释放网点库存并退还押金', '归还确认', { type: 'warning' }).then(async () => {
    await confirmReturn(id)
    ElMessage.success('归还成功，订单已完成')
    load()
  })
}

function openComment(o) {
  commentForm.orderId = o.id
  commentForm.score = 5
  commentForm.content = ''
  commentVisible.value = true
}

async function submitComment() {
  if (!commentForm.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  submitting.value = true
  try {
    await addComment({ orderId: commentForm.orderId, score: commentForm.score, content: commentForm.content })
    ElMessage.success('评价成功')
    commentVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 8px;
}

.head-stat {
  display: flex;
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

.status-tabs :deep(.el-tabs__header) {
  margin-bottom: 22px;
}

.status-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: var(--camp-line);
}

.status-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 600;
  color: var(--camp-muted);
  height: 44px;
}

.status-tabs :deep(.el-tabs__item.is-active) {
  color: var(--camp-primary-dark);
}

.status-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--camp-primary), var(--camp-primary-2));
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 200px;
}

.order-item {
  position: relative;
  padding: 18px 22px 18px 26px;
  overflow: hidden;
}

.order-item:hover {
  transform: translateY(-2px);
}

.accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 5px;
  border-radius: 0 4px 4px 0;
  background: #cfd8d4;
}

.st-0 .accent {
  background: linear-gradient(180deg, #ffb547, #ff8a3d);
}

.st-1 .accent {
  background: linear-gradient(180deg, #3ddc97, #16b876);
}

.st-2 .accent {
  background: linear-gradient(180deg, #7aa7ff, #4b7bec);
}

.st-3 .accent {
  background: linear-gradient(180deg, #7ee2b8, #2bbd7e);
}

.st-4 .accent {
  background: linear-gradient(180deg, #ff8a8f, #f2555a);
}

.order-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--camp-line);
}

.order-no {
  font-weight: 800;
  font-size: 13.5px;
  color: var(--camp-ink);
  letter-spacing: 0.01em;
}

.order-time {
  color: var(--camp-muted);
  font-size: 12.5px;
  flex: 1;
}

.order-body {
  display: flex;
  align-items: center;
  padding-top: 14px;
  gap: 20px;
}

.order-equipment {
  display: flex;
  gap: 14px;
  flex: 1;
  min-width: 0;
}

.eq-thumb {
  width: 62px;
  height: 62px;
  border-radius: 14px;
  background: linear-gradient(145deg, #e9fbf2, #d4f5e6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  object-fit: cover;
  flex-shrink: 0;
  box-shadow: inset 0 0 0 1px rgba(22, 184, 118, 0.1);
}

.eq-thumb.placeholder {
  font-size: 26px;
}

.eq-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.eq-info .eq-name {
  font-weight: 700;
  font-size: 15px;
  color: var(--camp-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.eq-line {
  font-size: 12.5px;
  color: var(--camp-ink-2);
}

.route {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.route .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  display: inline-block;
}

.route .dot.from {
  background: var(--camp-primary);
}

.route .dot.to {
  background: #4b7bec;
}

.route .arrow {
  color: var(--camp-muted);
  font-size: 12px;
}

.eq-time {
  font-size: 12px;
  color: var(--camp-muted);
}

.order-amount {
  text-align: right;
  flex-shrink: 0;
}

.order-amount .amount {
  font-size: 21px;
  font-weight: 800;
  color: #ff7043;
  letter-spacing: -0.03em;
}

.order-amount .amount .cur {
  font-size: 13px;
  font-weight: 700;
}

.order-amount .deposit {
  font-size: 12px;
  color: var(--camp-muted);
  margin-top: 3px;
}

.order-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 900px) {
  .order-body {
    flex-wrap: wrap;
  }

  .order-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
