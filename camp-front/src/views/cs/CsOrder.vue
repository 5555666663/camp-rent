<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="订单号 / 用户名" clearable style="width: 200px" @keyup.enter="search" />
      <el-select v-model="query.orderStatus" placeholder="全部状态" clearable style="width: 150px">
        <el-option v-for="s in statusList" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-select v-model="query.storeId" placeholder="全部网点" clearable style="width: 200px">
        <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table :data="rows" border stripe empty-text="暂无订单数据">
      <el-table-column prop="id" label="订单号" width="80" align="center" />
      <el-table-column prop="username" label="用户" width="110" />
      <el-table-column prop="equipmentName" label="商品" min-width="150" show-overflow-tooltip />
      <el-table-column prop="pickStoreName" label="取件网点" min-width="140" show-overflow-tooltip />
      <el-table-column prop="returnStoreName" label="归还网点" min-width="140" show-overflow-tooltip />
      <el-table-column label="租赁时间" min-width="200">
        <template #default="{ row }">
          <span class="time">{{ row.rentStartTime }} ~ {{ row.rentEndTime }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="totalPrice" label="租金" width="90" align="right" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.orderStatus)" effect="plain" size="small">{{ statusText(row.orderStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" align="center">
        <template #default="{ row }">
          <el-button v-if="row.orderStatus === 2" size="small" type="primary" @click="verify(row)">核验归还</el-button>
          <el-button v-if="row.orderStatus !== 3 && row.orderStatus !== 4" size="small" @click="overdue(row)">标记逾期</el-button>
          <el-button size="small" text type="primary" @click="openRemark(row)">备注</el-button>
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

    <el-dialog v-model="remarkVisible" title="修改订单备注" width="420px">
      <el-input v-model="remarkText" type="textarea" :rows="3" placeholder="请输入备注内容" />
      <template #footer>
        <el-button @click="remarkVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRemark">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCsOrderPage, csVerifyReturn, csMarkOverdue } from '@/api/cs'
import { getStoreList } from '@/api/public'
import request from '@/utils/request'

const rows = ref([])
const stores = ref([])
const page = reactive({ current: 1, size: 8, total: 0 })
const query = reactive({ keyword: '', orderStatus: null, storeId: null })

const statusList = [
  { value: 0, label: '待支付' },
  { value: 1, label: '租赁中' },
  { value: 2, label: '待归还' },
  { value: 3, label: '已完成' },
  { value: 4, label: '逾期' }
]

const remarkVisible = ref(false)
const remarkText = ref('')
const remarkRow = ref(null)

function statusText(s) {
  return (statusList.find((i) => i.value === s) || {}).label || '-'
}

function statusType(s) {
  return { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger' }[s] || 'info'
}

async function load() {
  const res = await getCsOrderPage({
    current: page.current,
    size: page.size,
    orderStatus: query.orderStatus,
    storeId: query.storeId
  })
  const data = res || {}
  // 客服接口暂不支持关键字，前端做一次本地过滤
  let records = data.records || []
  if (query.keyword) {
    const k = query.keyword.trim()
    records = records.filter(
      (r) => String(r.id).includes(k) || (r.username || '').includes(k)
    )
  }
  rows.value = records
  page.total = data.total || 0
}

function search() {
  page.current = 1
  load()
}

function reset() {
  query.keyword = ''
  query.orderStatus = null
  query.storeId = null
  page.current = 1
  load()
}

async function verify(row) {
  await ElMessageBox.confirm(`确认核验订单 #${row.id} 的归还？核验后将恢复网点库存并转为已完成。`, '核验归还', {
    type: 'warning'
  })
  await csVerifyReturn(row.id)
  ElMessage.success('核验完成')
  load()
}

async function overdue(row) {
  await ElMessageBox.confirm(`确认将订单 #${row.id} 标记为逾期？`, '标记逾期', { type: 'warning' })
  await csMarkOverdue(row.id)
  ElMessage.success('已标记逾期')
  load()
}

function openRemark(row) {
  remarkRow.value = row
  remarkText.value = row.remark || ''
  remarkVisible.value = true
}

async function saveRemark() {
  await request.put(`/cs/order/${remarkRow.value.id}/remark`, { remark: remarkText.value })
  ElMessage.success('备注已保存')
  remarkVisible.value = false
  load()
}

onMounted(async () => {
  try {
    stores.value = (await getStoreList()) || []
  } catch (e) {
    stores.value = []
  }
  load()
})
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
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}

.time {
  font-size: 12px;
  color: #64748b;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
