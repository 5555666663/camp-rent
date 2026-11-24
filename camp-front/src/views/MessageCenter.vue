<template>
  <div class="page-container">
    <div class="page-head">
      <h2 class="page-title">消息中心</h2>
      <p class="page-subtitle">租期到期提醒、逾期通知与售后消息都在这里</p>
    </div>

    <div class="toolbar">
      <el-radio-group v-model="query.readFlag" @change="reload">
        <el-radio-button :label="null">全部</el-radio-button>
        <el-radio-button :label="0">未读</el-radio-button>
        <el-radio-button :label="1">已读</el-radio-button>
      </el-radio-group>
      <div style="flex: 1"></div>
      <el-button :disabled="!unread" @click="handleReadAll">全部标为已读</el-button>
    </div>

    <div class="msg-list" v-loading="loading">
      <div v-for="m in list" :key="m.id" class="msg-card" :class="{ unread: m.readFlag === 0 }">
        <div class="msg-main">
          <div class="msg-head">
            <el-tag size="small" :type="tagType(m.type)">{{ typeText(m.type) }}</el-tag>
            <span class="msg-title">{{ m.title }}</span>
            <span class="msg-time">{{ m.createTime }}</span>
          </div>
          <p class="msg-content">{{ m.content }}</p>
        </div>
        <el-button v-if="m.readFlag === 0" text type="primary" @click="handleRead(m)">标为已读</el-button>
      </div>
      <el-empty v-if="!loading && !list.length" description="暂无消息" />
    </div>

    <el-pagination
      v-if="total > query.size"
      class="pager"
      background
      layout="prev, pager, next, total"
      :total="total"
      :page-size="query.size"
      :current-page="query.current"
      @current-change="(p) => { query.current = p; load() }"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getMessagePage, getUnreadCount, readMessage, readAllMessages } from '@/api/opt'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const unread = ref(0)
const query = reactive({ current: 1, size: 10, readFlag: null })

const typeText = (t) => ({ 1: '到期提醒', 2: '逾期通知', 3: '系统通知', 4: '售后' }[t] || '通知')
const tagType = (t) => ({ 1: 'warning', 2: 'danger', 3: 'info', 4: 'success' }[t] || 'info')

async function load() {
  loading.value = true
  try {
    const res = await getMessagePage({ current: query.current, size: query.size, readFlag: query.readFlag })
    list.value = res.records || []
    total.value = res.total || 0
    const c = await getUnreadCount()
    unread.value = c || 0
  } finally {
    loading.value = false
  }
}

function reload() {
  query.current = 1
  load()
}

async function handleRead(m) {
  await readMessage(m.id)
  m.readFlag = 1
  await load()
}

async function handleReadAll() {
  await readAllMessages()
  await load()
}

onMounted(load)
</script>

<style scoped>
.msg-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.msg-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border: 1px solid var(--camp-line, #e5e7eb);
  border-radius: var(--camp-radius, 12px);
  padding: 14px 18px;
}

.msg-card.unread {
  border-left: 3px solid var(--camp-primary, #1e40af);
}

.msg-main {
  flex: 1;
  min-width: 0;
}

.msg-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.msg-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--camp-ink, #0f172a);
}

.msg-time {
  margin-left: auto;
  font-size: 12px;
  color: var(--camp-muted, #64748b);
}

.msg-content {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--camp-ink-2, #334155);
  line-height: 1.6;
}

.pager {
  margin-top: 18px;
  justify-content: flex-end;
}

.page-head {
  margin-bottom: 16px;
}
</style>
