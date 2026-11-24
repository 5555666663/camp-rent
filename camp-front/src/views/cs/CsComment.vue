<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="query.storeId" placeholder="全部网点" clearable style="width: 220px">
        <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table :data="rows" border stripe empty-text="暂无评价数据">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="orderId" label="订单号" width="90" align="center" />
      <el-table-column prop="username" label="用户" width="110" />
      <el-table-column prop="storeName" label="关联网点" min-width="160" show-overflow-tooltip />
      <el-table-column label="评分" width="160" align="center">
        <template #default="{ row }">
          <el-rate :model-value="row.score" disabled size="small" />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="240" show-overflow-tooltip />
      <el-table-column prop="createTime" label="评价时间" width="170" />
      <el-table-column label="操作" width="110" align="center">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCsCommentPage, csDeleteComment } from '@/api/cs'
import { getStoreList } from '@/api/public'

const rows = ref([])
const stores = ref([])
const page = reactive({ current: 1, size: 8, total: 0 })
const query = reactive({ storeId: null })

async function load() {
  const res = await getCsCommentPage({
    current: page.current,
    size: page.size,
    storeId: query.storeId
  })
  const data = res || {}
  rows.value = data.records || []
  page.total = data.total || 0
}

function search() {
  page.current = 1
  load()
}

function reset() {
  query.storeId = null
  page.current = 1
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除该条评价（订单 #${row.orderId}）？删除后不可恢复。`, '删除评价', {
    type: 'warning'
  })
  await csDeleteComment(row.id)
  ElMessage.success('已删除')
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

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
