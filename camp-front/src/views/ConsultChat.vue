<template>
  <div class="chat-page">
    <div class="chat-card">
      <!-- 聊天窗口头部 -->
      <div class="chat-head">
        <div class="head-left">
          <el-avatar :size="36" class="cs-avatar">客服</el-avatar>
          <div class="head-text">
            <div class="cs-name">在线客服</div>
            <div class="cs-status"><i class="dot"></i>在线，通常 1 分钟内回复</div>
          </div>
        </div>
        <div class="head-right">
          <el-button text @click="loadMessages(true)">刷新</el-button>
        </div>
      </div>

      <!-- 消息气泡区 -->
      <div ref="scrollRef" class="msg-area">
        <div v-if="messages.length === 0" class="empty-tip">还没有消息，发送第一条咨询吧～</div>
        <template v-for="(m, i) in messages" :key="m.id">
          <div v-if="showTime(i)" class="time-divider">{{ fmtTime(m.createTime) }}</div>
          <div class="msg-row" :class="m.senderType === 1 ? 'self' : 'other'">
            <el-avatar :size="32" :class="m.senderType === 1 ? 'avatar-self' : 'avatar-cs'">
              {{ m.senderType === 1 ? '我' : '客' }}
            </el-avatar>
            <div class="bubble" :class="m.senderType === 1 ? 'bubble-self' : 'bubble-cs'">
              {{ m.content }}
            </div>
          </div>
        </template>
      </div>

      <!-- 快捷问题 -->
      <div class="quick-row">
        <span class="quick-label">常见问题：</span>
        <button v-for="q in quickQuestions" :key="q" class="quick-chip" @click="fillQuick(q)">{{ q }}</button>
      </div>

      <!-- 输入区 -->
      <div class="input-area">
        <el-input
          v-model="draft"
          type="textarea"
          :rows="3"
          resize="none"
          maxlength="1000"
          show-word-limit
          placeholder="请输入咨询内容，Enter 发送，Shift + Enter 换行"
          @keydown.enter.native="onEnter"
        />
        <div class="send-row">
          <span class="hint">客服看到后会尽快回复，稍等片刻即可</span>
          <el-button type="primary" :loading="sending" @click="onSend">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getChatMessages, getMySession, sendChatMessage } from '@/api/chat'

const messages = ref([])
const draft = ref('')
const sending = ref(false)
const sessionId = ref(null)
const scrollRef = ref(null)
let timer = null

/** 常见问题快捷填充 */
const quickQuestions = [
  '押金什么时候退还？',
  '可以跨网点归还吗？',
  '下单后怎么取消？',
  '装备损坏怎么赔偿？'
]

/** 拉取消息（silent=true 时不提示错误，用于轮询） */
async function loadMessages(silent = false) {
  if (!sessionId.value) return
  try {
    const res = await getChatMessages(sessionId.value)
    const list = Array.isArray(res) ? res : []
    // 仅在数量变化时重绘，避免轮询导致滚动跳动
    if (list.length !== messages.value.length) {
      messages.value = list
      await nextTick()
      scrollToBottom()
    } else if (list.length) {
      messages.value = list
    }
  } catch (e) {
    if (!silent) ElMessage.error('消息加载失败')
  }
}

/** 发送消息 */
async function onSend() {
  const content = draft.value.trim()
  if (!content) {
    ElMessage.warning('请输入咨询内容')
    return
  }
  sending.value = true
  try {
    await sendChatMessage(content)
    draft.value = ''
    await loadMessages()
  } catch (e) {
    ElMessage.error('发送失败，请稍后重试')
  } finally {
    sending.value = false
  }
}

/** Enter 发送，Shift+Enter 换行 */
function onEnter(e) {
  if (e.shiftKey) return
  e.preventDefault()
  onSend()
}

function fillQuick(q) {
  draft.value = q
}

function scrollToBottom() {
  const el = scrollRef.value
  if (el) el.scrollTop = el.scrollHeight
}

/** 相邻消息间隔超过 5 分钟才显示时间分隔 */
function showTime(index) {
  if (index === 0) return true
  const prev = new Date((messages.value[index - 1].createTime || '').replace(/-/g, '/')).getTime()
  const cur = new Date((messages.value[index].createTime || '').replace(/-/g, '/')).getTime()
  return cur - prev > 5 * 60 * 1000
}

function fmtTime(t) {
  if (!t) return ''
  const s = String(t).replace('T', ' ')
  return s.length > 16 ? s.slice(0, 16) : s
}

onMounted(async () => {
  try {
    const session = await getMySession()
    sessionId.value = session && session.id ? session.id : null
    await loadMessages()
  } catch (e) {
    ElMessage.error('会话初始化失败，请重新登录')
  }
  // 轮询：4 秒拉一次新消息
  timer = setInterval(() => loadMessages(true), 4000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.chat-page {
  max-width: 1120px;
  margin: 0 auto;
  padding: 24px 16px 32px;
}

.chat-card {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 190px);
  min-height: 520px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.chat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid #e5e7eb;
}

.head-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cs-avatar {
  background: #1e40af;
  color: #fff;
  font-size: 13px;
}

.cs-name {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.cs-status {
  margin-top: 2px;
  font-size: 12px;
  color: #64748b;
}

.dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-right: 5px;
  border-radius: 50%;
  background: #16a34a;
  vertical-align: middle;
}

.msg-area {
  flex: 1;
  overflow-y: auto;
  padding: 18px;
  background: #f8fafc;
}

.empty-tip {
  padding: 60px 0;
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
}

.time-divider {
  margin: 14px 0;
  text-align: center;
  font-size: 12px;
  color: #94a3b8;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 12px;
}

.msg-row.self {
  flex-direction: row-reverse;
}

.avatar-cs {
  flex: 0 0 auto;
  background: #e2e8f0;
  color: #475569;
  font-size: 12px;
}

.avatar-self {
  flex: 0 0 auto;
  background: #1e40af;
  color: #fff;
  font-size: 12px;
}

.bubble {
  max-width: 62%;
  padding: 10px 13px;
  font-size: 14px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
  border-radius: 8px;
}

.bubble-cs {
  background: #fff;
  border: 1px solid #e5e7eb;
  color: #0f172a;
}

.bubble-self {
  background: #1e40af;
  color: #fff;
}

.quick-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 18px 0;
}

.quick-label {
  font-size: 12px;
  color: #64748b;
}

.quick-chip {
  padding: 4px 12px;
  font-size: 12px;
  color: #334155;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.16s ease-out;
}

.quick-chip:hover {
  color: #1e40af;
  border-color: #bfdbfe;
  background: #eff6ff;
}

.input-area {
  padding: 10px 18px 14px;
  border-top: 1px solid #e5e7eb;
  background: #fff;
}

.send-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}

.hint {
  font-size: 12px;
  color: #94a3b8;
}
</style>
