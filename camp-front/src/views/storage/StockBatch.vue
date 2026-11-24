<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.batchNo" placeholder="批次号" clearable style="width: 220px" @keyup.enter="search" />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <span class="tip">记录食品批次的入库与库存变化（先进先出扣减）</span>
    </div>

    <el-table :data="rows" border stripe empty-text="暂无库存变更记录">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="batchNo" label="批次号" width="200" />
      <el-table-column prop="equipmentId" label="商品 ID" width="90" align="center" />
      <el-table-column prop="storeId" label="网点 ID" width="90" align="center" />
      <el-table-column prop="quantity" label="剩余数量" width="110" align="center" />
      <el-table-column prop="productionDate" label="生产日期" width="130" />
      <el-table-column prop="expireDate" label="到期日期" width="130" />
      <el-table-column label="批次状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" effect="plain" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="入库时间" width="170" />
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
import { getBatchPage } from '@/api/opt'

const rows = ref([])
const page = reactive({ current: 1, size: 8, total: 0 })
const query = reactive({ batchNo: '' })

const STATUS = { 0: '正常', 1: '临期', 2: '过期', 3: '售罄' }

function statusText(s) {
  return STATUS[s] || '-'
}

function statusType(s) {
  return { 0: 'success', 1: 'warning', 2: 'danger', 3: 'info' }[s] || 'info'
}

async function load() {
  const res = await getBatchPage({ current: page.current, size: page.size, batchNo: query.batchNo })
  const data = res || {}
  rows.value = data.records || []
  page.total = data.total || 0
}

function search() {
  page.current = 1
  load()
}

function reset() {
  query.batchNo = ''
  page.current = 1
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
  gap: 10px;
  margin-bottom: 14px;
}

.tip {
  font-size: 12px;
  color: #94a3b8;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
