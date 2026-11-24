<template>
  <div class="cs-chat">
    <!-- 左侧会话列表 -->
    <aside class="session-panel">
      <div class="panel-head">
        <div class="panel-title">咨询会话</div>
        <el-input v-model="keyword" size="small" placeholder="搜索用户名/内容" clearable @keyup.enter.native="loadSessions" @clear="loadSessions" />
      </div>
      <div class="session-list">
        <div v-if="sessions.length === 0" class="empty-tip">暂无会话</div>
        <div
          v-for="s in sessions"
          :key="s.id"
          class="session-item"
          :class="{ active: current && current.id === s.id }"
          @click="selectSession(s)"
        >
          <div class="session-top">
            <span class="session-name">{{ s.username }}</span>
            <span class="session-time">{{ fmtTime(s.lastTime) }}</span>
          </div>
          <div class="session-bottom">
            <span class="session-last">{{ s.lastContent || '暂无消息' }}</span>
            <el-badge v-if="s.csUnread > 0" :value="s.csUnread" class="unread-badge" />
          </div>
        </div>
      </div>
    </aside>

    <!-- 右侧聊天窗口 -->
    <section class="chat-panel">
      <div class="chat-head">
        <div class="chat-title">
          {{ current ? current.username : '请选择会话' }}
          <span v-if="current" class="chat-sub">最近活跃 {{ fmtTime(current.lastTime) }}</span>
        </div>
        <el-button text :disabled="!current" @click="loadMessages(true)">刷新</el-button>
      </div>

      <div ref="scrollRef" class="msg-area">
        <div v-if="!current" class="empty-tip">从左侧选择一位用户的咨询开始回复</div>
        <div v-else-if="messages.length === 0" class="empty-tip">该会话还没有消息</div>
        <template v-for="(m, i) in messages" :key="m.id">
          <div v-if="showTime(i)" class="time-divider">{{ fmtTime(m.createTime) }}</div>
          <div class="msg-row" :class="m.senderType === 2 ? 'self' : 'other'">
            <el-avatar :size="30" :class="m.senderType === 2 ? 'avatar-self' : 'avatar-cs'">
              {{ m.senderType === 2 ? '客' : '用' }}
            </el-avatar>
            <div class="bubble" :class="m.senderType === 2 ? 'bubble-self' : 'bubble-user'">
              {{ m.content }}
            </div>
          </div>
        </template>
      </div>

      <div class="input-area">
        <el-input
          v-model="draft"
          type="textarea"
          :rows="3"
          resize="none"
          maxlength="1000"
          show-word-limit
          :disabled="!current"
          placeholder="回复内容，Enter 发送，Shift + Enter 换行"
          @keydown.enter.native="onEnter"
        />
        <div class="send-row">
          <span class="hint">客服仅可回复，不可删除会话</span>
          <el-button type="primary" :disabled="!current" :loading="sending" @click="onSend">回复</el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getCsMessages, getCsSessionPage, replyCsMessage } from '@/api/chat'

const sessions = ref([])
const current = ref(null)
const messages = ref([])
const draft = ref('')
const keyword = ref('')
const sending = ref(false)
const scrollRef = ref(null)
let listTimer = null
let msgTimer = null

/** 会话分页加载（固定取前 50 条，够工作台使用） */
async function loadSessions() {
  try {
    const res = await getCsSessionPage({ current: 1, size: 50, keyword: keyword.value || undefined })
    sessions.value = (res && res.records) || []
    if (!current.value && sessions.value.length) {
      selectSession(sessions.value[0])
    } else if (current.value) {
      const fresh = sessions.value.find((s) => s.id === current.value.id)
      if (fresh) current.value = fresh
    }
  } catch (e) {
    ElMessage.error('会话列表加载失败')
  }
}

/** 选择会话并拉取消息 */
async function selectSession(s) {
  current.value = s
  messages.value = []
  await loadMessages()
}

/** 拉取当前会话消息 */
async function loadMessages(silent = false) {
  if (!current.value) return
  try {
    const res = await getCsMessages(current.value.id)
    const list = Array.isArray(res) ? res : []
    if (list.length !== messages.value.length) {
      messages.value = list
      await nextTick()
      scrollToBottom()
    } else if (list.length) {
      messages.value = list
    }
    // 读取即已读，刷新一次未读数
    if (current.value.csUnread > 0) {
      const s = sessions.value.find((x) => x.id === current.value.id)
      if (s) s.csUnread = 0
      current.value.csUnread = 0
    }
  } catch (e) {
    if (!silent) ElMessage.error('消息加载失败')
  }
}

/** 发送回复 */
async function onSend() {
  if (!current.value) return
  const content = draft.value.trim()
  if (!content) {
    ElMessage.warning('请输入回复内容')
    return
  }
  sending.value = true
  try {
    await replyCsMessage(current.value.id, content)
    draft.value = ''
    await loadMessages()
    await loadSessions()
  } catch (e) {
    ElMessage.error('回复失败，请稍后重试')
  } finally {
    sending.value = false
  }
}

function onEnter(e) {
  if (e.shiftKey) return
  e.preventDefault()
  onSend()
}

function scrollToBottom() {
  const el = scrollRef.value
  if (el) el.scrollTop = el.scrollHeight
}

function showTime(index) {
  if (index === 0) return true
  const prev = new Date((messages.value[index - 1].createTime || '').replace(/-/g, '/')).getTime()
  const cur = new Date((messages.value[index].createTime || '').replace(/-/g, '/')).getTime()
  return cur - prev > 5 * 60 * 1000
}

function fmtTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

onMounted(() => {
  loadSessions()
  // 会话列表 5 秒轮询，当前会话消息 4 秒轮询
  listTimer = setInterval(loadSessions, 5000)
  msgTimer = setInterval(() => loadMessages(true), 4000)
})

onBeforeUnmount(() => {
  if (listTimer) clearInterval(listTimer)
  if (msgTimer) clearInterval(msgTimer)
})
</script>

<style scoped>
.cs-chat {
  display: flex;
  gap: 12px;
  height: calc(100vh - 150px);
  min-height: 520px;
}

.session-panel {
  display: flex;
  flex-direction: column;
  width: 288px;
  flex: 0 0 288px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.panel-head {
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.panel-title {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  padding: 10px 12px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: background 0.16s ease-out;
}

.session-item:hover {
  background: #f8fafc;
}

.session-item.active {
  background: #eff6ff;
  border-left: 3px solid #1e40af;
}

.session-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.session-name {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
}

.session-time {
  font-size: 11px;
  color: #94a3b8;
}

.session-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
}

.session-last {
  flex: 1;
  overflow: hidden;
  font-size: 12px;
  color: #64748b;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  margin-left: 6px;
}

.chat-panel {
  display: flex;
  flex: 1;
  flex-direction: column;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.chat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
}

.chat-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.chat-sub {
  margin-left: 8px;
  font-size: 12px;
  font-weight: 400;
  color: #94a3b8;
}

.msg-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
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
  max-width: 60%;
  padding: 9px 12px;
  font-size: 13px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
  border-radius: 8px;
}

.bubble-user {
  background: #fff;
  border: 1px solid #e5e7eb;
  color: #0f172a;
}

.bubble-self {
  background: #1e40af;
  color: #fff;
}

.input-area {
  padding: 10px 16px 14px;
  border-top: 1px solid #e5e7eb;
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
