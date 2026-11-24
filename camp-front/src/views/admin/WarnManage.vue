<template>
  <div>
    <div class="toolbar">
      <el-radio-group v-model="tab">
        <el-radio-button label="stock">库存预警</el-radio-button>
        <el-radio-button label="expire">保质期预警</el-radio-button>
      </el-radio-group>
      <div style="flex: 1"></div>
      <el-button :loading="loading" @click="load">刷新</el-button>
      <el-button type="primary" @click="handleExport('stats')">导出统计 Excel</el-button>
    </div>

    <el-table v-if="tab === 'stock'" :data="stockList" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="eqName" label="商品名称" min-width="180" />
      <el-table-column prop="storeName" label="所属网点" min-width="160" />
      <el-table-column prop="stockNum" label="当前库存" width="110" />
      <el-table-column prop="minStock" label="预警阈值" width="110" />
      <el-table-column label="缺口" width="90">
        <template #default="{ row }">{{ Math.max(0, (row.minStock || 0) - (row.stockNum || 0)) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default>
          <el-tag type="danger" size="small">需补货</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-else :data="expireList" v-loading="loading" border stripe>
      <el-table-column prop="id" label="批次ID" width="90" />
      <el-table-column prop="equipmentId" label="商品ID" width="100" />
      <el-table-column prop="storeId" label="网点ID" width="90" />
      <el-table-column prop="batchNo" label="批次号" min-width="180" />
      <el-table-column prop="productionDate" label="生产日期" width="130" />
      <el-table-column prop="expireDate" label="到期日期" width="130" />
      <el-table-column prop="quantity" label="剩余数量" width="110" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 2 ? 'danger' : 'warning'" size="small">
            {{ row.status === 2 ? '已过期' : '临期' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !currentList.length" description="暂无预警数据" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getStockWarn, getExpireWarn, downloadExport } from '@/api/opt'

const tab = ref('stock')
const loading = ref(false)
const stockList = ref([])
const expireList = ref([])

const currentList = computed(() => (tab.value === 'stock' ? stockList.value : expireList.value))

async function load() {
  loading.value = true
  try {
    if (tab.value === 'stock') {
      stockList.value = (await getStockWarn()) || []
    } else {
      expireList.value = (await getExpireWarn()) || []
    }
  } finally {
    loading.value = false
  }
}

function saveBlob(res, name) {
  const blob = res.data instanceof Blob ? res.data : new Blob([res.data])
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = name
  a.click()
  window.URL.revokeObjectURL(url)
}

async function handleExport(type) {
  try {
    const res = await downloadExport(type)
    saveBlob(res, '业务统计_' + Date.now() + '.xlsx')
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

onMounted(load)
</script>
