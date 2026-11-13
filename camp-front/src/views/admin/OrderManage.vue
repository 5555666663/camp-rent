<template>
  <div>
    <div class="toolbar">
      <el-select v-model="query.storeId" placeholder="全部网点" clearable style="width: 160px" @change="search">
        <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
      </el-select>
      <el-select v-model="query.orderStatus" placeholder="全部状态" clearable style="width: 140px" @change="search">
        <el-option v-for="(v, k) in statusMap" :key="k" :label="v.text" :value="Number(k)" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="search">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="订单号" width="80" />
      <el-table-column prop="username" label="用户" width="100" />
      <el-table-column prop="equipmentName" label="装备" min-width="120" />
      <el-table-column prop="pickStoreName" label="取件网点" width="120" />
      <el-table-column prop="returnStoreName" label="归还网点" width="120" />
      <el-table-column label="租赁时间" width="200">
        <template #default="{ row }">
          <div class="time-cell">{{ row.rentStartTime }}</div>
          <div class="time-cell">{{ row.rentEndTime }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="totalPrice" label="金额" width="90">
        <template #default="{ row }">¥{{ row.totalPrice }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.orderStatus]?.type">{{ statusMap[row.orderStatus]?.text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.orderStatus !== 4 && row.orderStatus !== 3" size="small" type="danger" @click="markOverdue(row)">标记逾期</el-button>
          <el-button v-if="row.orderStatus === 2" size="small" type="success" @click="verify(row)">核验归还</el-button>
          <el-button size="small" @click="openRemark(row)">备注</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>

    <el-dialog v-model="remarkVisible" title="修改订单备注" width="420px">
      <el-input v-model="remarkText" type="textarea" :rows="3" placeholder="填写备注" />
      <template #footer>
        <el-button @click="remarkVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRemark">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminOrderPage, getAllStores, markOrderOverdue, verifyOrderReturn, updateOrderRemark } from '@/api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const stores = ref([])
const remarkVisible = ref(false)
const remarkText = ref('')
const remarkOrderId = ref(null)
const query = reactive({ current: 1, size: 10, storeId: null, orderStatus: null })

const statusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '租赁中', type: 'primary' },
  2: { text: '待归还', type: 'info' },
  3: { text: '已完成', type: 'success' },
  4: { text: '逾期', type: 'danger' }
}

onMounted(async () => {
  stores.value = (await getAllStores()) || []
  load()
})

async function load() {
  loading.value = true
  try {
    const res = await getAdminOrderPage(query)
    list.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.current = 1
  load()
}

function markOverdue(row) {
  ElMessageBox.confirm(`确认将订单 #${row.id} 标记为逾期？`, '提示', { type: 'warning' }).then(async () => {
    await markOrderOverdue(row.id)
    ElMessage.success('已标记逾期')
    load()
  })
}

function verify(row) {
  ElMessageBox.confirm('确认核验归还？将恢复网点库存并退还押金', '核验归还', { type: 'warning' }).then(async () => {
    await verifyOrderReturn(row.id)
    ElMessage.success('核验归还完成')
    load()
  })
}

function openRemark(row) {
  remarkOrderId.value = row.id
  remarkText.value = row.remark || ''
  remarkVisible.value = true
}

async function saveRemark() {
  await updateOrderRemark(remarkOrderId.value, remarkText.value)
  ElMessage.success('备注已保存')
  remarkVisible.value = false
  load()
}
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
.time-cell {
  font-size: 12px;
  color: #666;
  line-height: 1.5;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
