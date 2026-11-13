<template>
  <div class="cs-page">
    <!-- 顶部栏 -->
    <header class="cs-header">
      <div class="brand">
        <div class="logo">🎧</div>
        <div>
          <div class="brand-name">客服工作台</div>
          <div class="brand-sub">CUSTOMER SERVICE</div>
        </div>
      </div>
      <div class="right">
        <el-tag type="success" effect="light" round>在线</el-tag>
        <span class="who">{{ userStore.username }}</span>
        <el-button text @click="goHome">返回前台</el-button>
        <el-button text type="danger" @click="handleLogout">退出</el-button>
      </div>
    </header>

    <main class="cs-main">
      <div class="page-container">
        <div class="page-head">
          <h2 class="page-title">客服工作台</h2>
          <p class="page-subtitle">处理订单状态、售后评价与用户咨询，装备与价格相关操作请联系超级管理员</p>
        </div>

        <el-tabs v-model="tab" class="cs-tabs">
          <!-- 订单 -->
          <el-tab-pane label="订单处理" name="order">
            <div class="float-bar">
              <el-select v-model="orderQuery.orderStatus" placeholder="全部状态" clearable style="width: 150px" @change="loadOrders">
                <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
              </el-select>
              <el-button @click="loadOrders">刷新</el-button>
            </div>
            <el-table :data="orders" border stripe v-loading="loadingOrder">
              <el-table-column prop="id" label="订单号" width="80" />
              <el-table-column prop="username" label="用户" width="110" />
              <el-table-column prop="equipmentName" label="装备" min-width="140" />
              <el-table-column prop="pickStoreName" label="取件网点" min-width="150" />
              <el-table-column prop="returnStoreName" label="归还网点" min-width="150" />
              <el-table-column label="租赁时间" min-width="180">
                <template #default="{ row }">
                  <div class="small">{{ row.rentStartTime }}</div>
                  <div class="small">至 {{ row.rentEndTime }}</div>
                </template>
              </el-table-column>
              <el-table-column prop="totalPrice" label="租金" width="90" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="statusType(row.orderStatus)" round>{{ statusText(row.orderStatus) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button v-if="row.orderStatus === 2" size="small" type="primary" @click="verify(row)">核验归还</el-button>
                  <el-button v-if="row.orderStatus !== 3 && row.orderStatus !== 4" size="small" type="warning" @click="overdue(row)">标记逾期</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="pager">
              <el-pagination
                v-model:current-page="orderQuery.current"
                v-model:page-size="orderQuery.size"
                :total="orderTotal"
                :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next"
                @current-change="loadOrders"
                @size-change="loadOrders"
              />
            </div>
          </el-tab-pane>

          <!-- 售后 -->
          <el-tab-pane label="售后处理" name="comment">
            <el-table :data="comments" border stripe v-loading="loadingComment">
              <el-table-column prop="id" label="ID" width="70" />
              <el-table-column prop="username" label="用户" width="120" />
              <el-table-column prop="storeName" label="网点" min-width="160" />
              <el-table-column prop="score" label="评分" width="90">
                <template #default="{ row }"><el-rate :model-value="row.score" disabled /></template>
              </el-table-column>
              <el-table-column prop="content" label="评价内容" min-width="220" />
              <el-table-column prop="createTime" label="时间" width="170" />
              <el-table-column label="操作" width="110" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" type="danger" plain @click="removeComment(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="pager">
              <el-pagination
                v-model:current-page="commentQuery.current"
                v-model:page-size="commentQuery.size"
                :total="commentTotal"
                layout="total, prev, pager, next"
                @current-change="loadComments"
              />
            </div>
          </el-tab-pane>

          <!-- 消息 -->
          <el-tab-pane label="咨询消息" name="message">
            <el-table :data="messages" border stripe v-loading="loadingMessage">
              <el-table-column prop="id" label="ID" width="70" />
              <el-table-column prop="username" label="用户" width="120" />
              <el-table-column prop="content" label="咨询内容" min-width="220" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'warning'" round>{{ row.status === 1 ? '已回复' : '待处理' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="reply" label="客服回复" min-width="200" />
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" type="primary" plain @click="openReply(row)">回复</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="empty-tip" v-if="!messages.length">
              暂无咨询消息（用户可在前台「我的订单」页发起咨询）
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </main>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyVisible" title="回复咨询" width="480px">
      <p class="quote">{{ currentMessage.content }}</p>
      <el-input v-model="replyText" type="textarea" :rows="4" placeholder="请输入回复内容" />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store'
import {
  getCsOrderPage,
  csVerifyReturn,
  csMarkOverdue,
  getCsCommentPage,
  csDeleteComment,
  getCsMessagePage,
  csReplyMessage
} from '@/api/cs'

const router = useRouter()
const userStore = useUserStore()

const tab = ref('order')

const statusOptions = [
  { value: 0, label: '待支付' },
  { value: 1, label: '租赁中' },
  { value: 2, label: '待归还' },
  { value: 3, label: '已完成' },
  { value: 4, label: '逾期' }
]

function statusText(s) {
  return (statusOptions.find((i) => i.value === s) || {}).label || '未知'
}

function statusType(s) {
  return ['warning', 'success', 'primary', 'info', 'danger'][s] || 'info'
}

/* ---------------- 订单 ---------------- */
const orders = ref([])
const orderTotal = ref(0)
const loadingOrder = ref(false)
const orderQuery = reactive({ current: 1, size: 10, orderStatus: null })

async function loadOrders() {
  loadingOrder.value = true
  try {
    const res = await getCsOrderPage(orderQuery)
    orders.value = res.records || []
    orderTotal.value = res.total || 0
  } finally {
    loadingOrder.value = false
  }
}

async function verify(row) {
  await ElMessageBox.confirm(`确认订单 #${row.id} 已归还？确认后订单将流转为已完成并恢复库存。`, '核验归还', { type: 'warning' })
  await csVerifyReturn(row.id)
  ElMessage.success('核验成功')
  loadOrders()
}

async function overdue(row) {
  await ElMessageBox.confirm(`确认将订单 #${row.id} 标记为逾期？`, '标记逾期', { type: 'warning' })
  await csMarkOverdue(row.id)
  ElMessage.success('已标记逾期')
  loadOrders()
}

/* ---------------- 售后 ---------------- */
const comments = ref([])
const commentTotal = ref(0)
const loadingComment = ref(false)
const commentQuery = reactive({ current: 1, size: 10 })

async function loadComments() {
  loadingComment.value = true
  try {
    const res = await getCsCommentPage(commentQuery)
    comments.value = res.records || []
    commentTotal.value = res.total || 0
  } finally {
    loadingComment.value = false
  }
}

async function removeComment(row) {
  await ElMessageBox.confirm('确认删除该条评价？', '删除评价', { type: 'warning' })
  await csDeleteComment(row.id)
  ElMessage.success('已删除')
  loadComments()
}

/* ---------------- 消息 ---------------- */
const messages = ref([])
const loadingMessage = ref(false)
const replyVisible = ref(false)
const replyText = ref('')
const currentMessage = ref({})

async function loadMessages() {
  loadingMessage.value = true
  try {
    const res = await getCsMessagePage({ current: 1, size: 50 })
    messages.value = res.records || []
  } finally {
    loadingMessage.value = false
  }
}

function openReply(row) {
  currentMessage.value = row
  replyText.value = row.reply || ''
  replyVisible.value = true
}

async function submitReply() {
  if (!replyText.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  await csReplyMessage(currentMessage.value.id, replyText.value)
  ElMessage.success('回复成功')
  replyVisible.value = false
  loadMessages()
}

function goHome() {
  router.push('/')
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadOrders()
  loadComments()
  loadMessages()
})
</script>

<style scoped>
.cs-page {
  min-height: 100vh;
  background: var(--camp-bg, #f8fafc);
}

.cs-header {
  height: 68px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(18px);
  border-bottom: 1px solid var(--camp-line, rgba(15, 31, 26, 0.08));
  position: sticky;
  top: 0;
  z-index: 10;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-size: 20px;
  background: #1e40af;
  box-shadow: 0 8px 18px rgba(30, 64, 175, 0.28);
}

.brand-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--camp-ink, #0f1f1a);
}

.brand-sub {
  font-size: 10px;
  letter-spacing: 2px;
  color: var(--camp-muted, #8b9a94);
}

.right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.who {
  font-size: 14px;
  font-weight: 600;
  color: var(--camp-ink-2, #43564f);
}

.cs-main {
  padding: 24px 0 60px;
}

.page-head {
  margin-bottom: 16px;
}

.cs-tabs {
  background: #fff;
  border-radius: 18px;
  padding: 8px 20px 20px;
  box-shadow: 0 10px 28px rgba(15, 31, 26, 0.06);
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.small {
  font-size: 12px;
  color: var(--camp-muted, #8b9a94);
}

.quote {
  background: #f4fbf7;
  border-radius: 10px;
  padding: 10px 14px;
  color: var(--camp-ink-2, #43564f);
  margin-bottom: 14px;
}

.empty-tip {
  margin-top: 16px;
  font-size: 13px;
  color: var(--camp-muted, #8b9a94);
}
</style>
