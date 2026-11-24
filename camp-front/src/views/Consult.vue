<template>
  <!-- 用户端·咨询服务页面 -->
  <div class="page-container">
    <div class="page-head">
      <h2 class="page-title">在线咨询</h2>
      <p class="page-subtitle">对装备、订单或网点有疑问，提交后客服会尽快回复</p>
    </div>

    <!-- ① 提交表单 -->
    <div class="card form-card">
      <div class="card-title">我要咨询</div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="咨询标题" prop="title">
          <el-input
            v-model="form.title"
            maxlength="100"
            show-word-limit
            placeholder="一句话概括你的问题，例如：帐篷押金什么时候退"
          />
        </el-form-item>

        <el-form-item label="咨询内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            maxlength="1000"
            show-word-limit
            placeholder="请描述具体问题，写清订单号、装备名称或网点，方便客服快速处理"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交咨询</el-button>
          <el-button @click="handleReset">清空</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- ② 历史咨询记录 -->
    <div class="card list-card">
      <div class="card-title">
        我的咨询记录
        <span class="total-text">共 {{ total }} 条</span>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && list.length === 0" description="您还没有提交过咨询" />

      <template v-else>
        <div v-loading="loading" class="consult-list">
          <div v-for="item in list" :key="item.id" class="consult-item">
            <div class="item-head">
              <span class="item-title">{{ item.title || '（无标题）' }}</span>
              <!-- ③ 状态标签：待回复橙色 / 已回复绿色 -->
              <el-tag :type="item.status === 1 ? 'success' : 'warning'" size="small" effect="light">
                {{ item.status === 1 ? '已回复' : '待回复' }}
              </el-tag>
              <span class="item-time">{{ formatTime(item.createTime) }}</span>
            </div>

            <div class="item-body">
              <span class="label">我的提问</span>
              <span class="text">{{ item.content }}</span>
            </div>

            <div v-if="item.status === 1" class="item-body reply">
              <span class="label">客服回复</span>
              <span class="text">{{ item.reply || '—' }}</span>
            </div>
            <div v-else class="item-pending">客服正在处理中，请稍候…</div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pager">
          <el-pagination
            background
            layout="total, prev, pager, next"
            :total="total"
            :page-size="query.size"
            :current-page="query.current"
            @current-change="handlePageChange"
          />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
// Vue3 setup 语法糖 + Element Plus
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { submitConsult, getMyConsults } from '@/api/consult'

// ------- 表单相关 -------
const formRef = ref(null)
const submitting = ref(false)
const form = reactive({ title: '', content: '' })

// 前端表单校验：标题、内容必填
const rules = {
  title: [
    { required: true, message: '请填写咨询标题', trigger: 'blur' },
    { max: 100, message: '标题不能超过 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请填写咨询内容', trigger: 'blur' },
    { min: 5, message: '请至少填写 5 个字符，方便客服理解', trigger: 'blur' },
    { max: 1000, message: '内容不能超过 1000 个字符', trigger: 'blur' }
  ]
}

// ------- 列表相关 -------
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 5 })

// 提交咨询
async function handleSubmit() {
  if (!formRef.value) return
  // 表单校验通过后再提交
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await submitConsult({ title: form.title.trim(), content: form.content.trim() })
    ElMessage.success('提交成功，客服会尽快回复')
    handleReset()
    query.current = 1
    loadList()
  } finally {
    submitting.value = false
  }
}

// 清空表单
function handleReset() {
  form.title = ''
  form.content = ''
  formRef.value && formRef.value.clearValidate()
}

// 加载「我的咨询」列表（后端按当前登录用户过滤，天然看不到他人数据）
async function loadList() {
  loading.value = true
  try {
    const res = await getMyConsults({ current: query.current, size: query.size })
    list.value = res.records || []
    total.value = Number(res.total || 0)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  query.current = page
  loadList()
}

// 时间格式化：后端返回 yyyy-MM-dd HH:mm:ss
function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 19)
}

onMounted(loadList)
</script>

<style scoped>
.page-container {
  max-width: 1120px;
  margin: 0 auto;
  padding: 24px 20px 48px;
}
.page-head {
  margin-bottom: 16px;
}
.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #0f172a;
}
.page-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #64748b;
}

/* 卡片：白底 + 1px 描边，B 端简洁风，无渐变/毛玻璃/动画 */
.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.total-text {
  font-size: 12px;
  font-weight: 400;
  color: #94a3b8;
}

/* 记录项 */
.consult-list {
  display: flex;
  flex-direction: column;
}
.consult-item {
  border-top: 1px solid #f1f5f9;
  padding: 14px 0;
}
.consult-item:first-child {
  border-top: none;
  padding-top: 0;
}
.item-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.item-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}
.item-time {
  margin-left: auto;
  font-size: 12px;
  color: #94a3b8;
}
.item-body {
  display: flex;
  gap: 10px;
  font-size: 13px;
  line-height: 1.7;
  color: #334155;
}
.item-body .label {
  flex: none;
  width: 60px;
  color: #64748b;
}
.item-body.reply {
  margin-top: 6px;
  padding: 10px 12px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 6px;
}
.item-body.reply .text {
  color: #0f172a;
}
.item-pending {
  margin-top: 6px;
  font-size: 12px;
  color: #b45309;
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}
</style>
