<template>
  <div>
    <div class="toolbar">
      <el-select v-model="query.storeId" placeholder="全部网点" clearable style="width: 160px" @change="search">
        <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="search">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户" width="100" />
      <el-table-column prop="storeName" label="网点" width="150" />
      <el-table-column label="评分" width="160">
        <template #default="{ row }">
          <el-rate :model-value="row.score" disabled />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="240" show-overflow-tooltip />
      <el-table-column prop="createTime" label="评价时间" width="170" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCommentPage, deleteComment, getAllStores } from '@/api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const stores = ref([])
const query = reactive({ current: 1, size: 10, storeId: null })

onMounted(async () => {
  stores.value = (await getAllStores()) || []
  load()
})

async function load() {
  loading.value = true
  try {
    const res = await getCommentPage(query)
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

function remove(row) {
  ElMessageBox.confirm('确认删除该评价？', '提示', { type: 'warning' }).then(async () => {
    await deleteComment(row.id)
    ElMessage.success('删除成功')
    load()
  })
}
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
