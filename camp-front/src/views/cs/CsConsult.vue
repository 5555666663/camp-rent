<template>
  <!-- 客服工作台·咨询消息列表页（ROLE_CS / ROLE_ADMIN 可见） -->
  <div class="cs-consult">
    <!-- ① 顶部筛选：全部 / 待处理 / 已完成 -->
    <div class="toolbar">
      <el-radio-group v-model="query.status" @change="handleFilterChange">
        <el-radio-button :label="null">全部</el-radio-button>
        <el-radio-button :label="0">待处理</el-radio-button>
        <el-radio-button :label="1">已完成</el-radio-button>
      </el-radio-group>
      <el-button :icon="Refresh" @click="loadList">刷新</el-button>
    </div>

    <!-- ② 咨询列表表格：点击行打开回复对话框 -->
    <div class="table-card">
      <el-empty v-if="!loading && list.length === 0" description="暂无咨询记录" />

      <template v-else>
        <el-table
          v-loading="loading"
          :data="list"
          border
          stripe
          size="default"
          highlight-current-row
          @row-click="handleRowClick"
        >
          <el-table-column prop="id" label="咨询ID" width="90" align="center" />
          <el-table-column prop="username" label="用户名" width="130" />
          <el-table-column prop="title" label="咨询标题" min-width="220" show-overflow-tooltip />
          <el-table-column label="提交时间" width="180">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110" align="center">
            <template #default="{ row }">
              <!-- 状态：待处理橙色 / 已完成绿色 -->
              <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small" effect="light">
                {{ row.status === 1 ? '已回复' : '待处理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click.stop="openDialog(row)">
                {{ row.status === 1 ? '查看' : '回复' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pager">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :total="total"
            :page-size="query.size"
            :current-page="query.current"
            :page-sizes="[10, 20, 50]"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </template>
    </div>

    <!-- ③ 回复对话框 -->
    <el-dialog v-model="dialogVisible" title="处理咨询" width="620px" destroy-on-close>
      <div v-if="current" class="detail">
        <div class="detail-row">
          <span class="label">咨询用户</span>
          <span>{{ current.username }}</span>
        </div>
        <div class="detail-row">
          <span class="label">咨询标题</span>
          <span class="strong">{{ current.title || '（无标题）' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">提交时间</span>
          <span>{{ formatTime(current.createTime) }}</span>
        </div>

        <div class="divider" />

        <div class="block-label">用户提问</div>
        <div class="question-box">{{ current.content }}</div>

        <div class="block-label">客服回复</div>
        <!-- 已回复则回显，未回复可填写；客服只能回复，不能删除记录 -->
        <el-input
          v-model="replyText"
          type="textarea"
          :rows="5"
          maxlength="500"
          show-word-limit
          :disabled="current.status === 1"
          placeholder="请输入回复内容，回复后用户可在「我的咨询」中查看"
        />
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button
          v-if="current && current.status !== 1"
          type="primary"
          :loading="replying"
          @click="handleReply"
        >
          提交回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// Vue3 setup 语法糖 + Element Plus
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getCsConsultPage, replyConsult } from '@/api/consult'

// ------- 列表查询条件 -------
const query = reactive({
  current: 1,
  size: 10,
  status: null // null=全部，0=待处理，1=已完成
})

const loading = ref(false)
const list = ref([])
const total = ref(0)

// 加载咨询列表（客服可见全部用户的咨询）
async function loadList() {
  loading.value = true
  try {
    // 注意：status 为 null 时不传该参数，后端即返回全部
    const params = { current: query.current, size: query.size }
    if (query.status !== null) params.status = query.status
    const res = await getCsConsultPage(params)
    list.value = res.records || []
    total.value = Number(res.total || 0)
  } finally {
    loading.value = false
  }
}

// 切换筛选：重置到第一页后查询
function handleFilterChange() {
  query.current = 1
  loadList()
}

function handlePageChange(page) {
  query.current = page
  loadList()
}

function handleSizeChange(size) {
  query.size = size
  query.current = 1
  loadList()
}

// ------- 回复对话框 -------
const dialogVisible = ref(false)
const current = ref(null)
const replyText = ref('')
const replying = ref(false)

// 点击表格行 / 操作列都可以打开详情
function handleRowClick(row) {
  openDialog(row)
}

function openDialog(row) {
  current.value = row
  replyText.value = row.reply || ''
  dialogVisible.value = true
}

// 提交回复：成功后状态变为「已回复」
async function handleReply() {
  if (!current.value) return
  const text = replyText.value.trim()
  if (!text) {
    ElMessage.warning('请填写回复内容')
    return
  }
  replying.value = true
  try {
    await replyConsult(current.value.id, text)
    ElMessage.success('回复成功')
    dialogVisible.value = false
    loadList()
  } finally {
    replying.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 19)
}

onMounted(loadList)
</script>

<style scoped>
/* B 端简洁样式：白底卡片 + 1px 描边 + 8px 圆角，无渐变、无毛玻璃、无动画 */
.cs-consult {
  padding: 4px 0;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.table-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}

/* 对话框内容 */
.detail-row {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #334155;
  line-height: 26px;
}
.detail-row .label {
  flex: none;
  width: 68px;
  color: #64748b;
}
.detail-row .strong {
  font-weight: 600;
  color: #0f172a;
}
.divider {
  height: 1px;
  background: #eef2f7;
  margin: 12px 0;
}
.block-label {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  margin: 4px 0 8px;
}
.question-box {
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 6px;
  padding: 12px;
  font-size: 13px;
  line-height: 1.7;
  color: #334155;
  white-space: pre-wrap;
  margin-bottom: 14px;
}
</style>
