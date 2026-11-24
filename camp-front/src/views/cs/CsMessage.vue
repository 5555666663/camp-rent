<template>
  <div class="page">
    <div class="toolbar">
      <el-button type="primary" @click="load">刷新</el-button>
      <span class="tip">共 {{ page.total }} 条用户咨询</span>
    </div>

    <el-table :data="rows" border stripe empty-text="暂无用户咨询">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column prop="content" label="咨询内容" min-width="260" show-overflow-tooltip />
      <el-table-column prop="reply" label="回复内容" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.reply">{{ row.reply }}</span>
          <el-tag v-else size="small" type="warning" effect="plain">待回复</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="170" />
      <el-table-column label="操作" width="110" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openReply(row)">
            {{ row.reply ? '修改回复' : '回复' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :page-sizes="[8, 10, 20, 50]"
        :total="page.total"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="load"
        @size-change="load"
      />
    </div>

    <el-dialog v-model="replyVisible" title="回复用户咨询" width="480px">
      <div class="quote">用户：{{ current?.content }}</div>
      <el-input v-model="replyText" type="textarea" :rows="4" maxlength="200" show-word-limit placeholder="请输入回复内容" />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="saveReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getCsMessagePage, csReplyMessage } from '@/api/cs'

const rows = ref([])
const page = reactive({ current: 1, size: 8, total: 0 })
const replyVisible = ref(false)
const replyText = ref('')
const current = ref(null)

async function load() {
  const res = await getCsMessagePage({ current: page.current, size: page.size })
  const data = res || {}
  rows.value = data.records || []
  page.total = data.total || 0
}

function openReply(row) {
  current.value = row
  replyText.value = row.reply || ''
  replyVisible.value = true
}

async function saveReply() {
  if (!replyText.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  await csReplyMessage(current.value.id, replyText.value.trim())
  ElMessage.success('回复已提交')
  replyVisible.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.page {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.tip {
  font-size: 13px;
  color: #64748b;
}

.quote {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 13px;
  color: #475569;
  margin-bottom: 12px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
