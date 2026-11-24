<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="query.storeId" placeholder="全部网点" clearable style="width: 210px">
        <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="商品名称 / 品牌" clearable style="width: 200px" @keyup.enter="search" />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="success" plain @click="openInbound">入库登记</el-button>
    </div>

    <el-table :data="rows" border stripe empty-text="暂无库存数据">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="eqName" label="商品名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="brand" label="品牌" width="110" />
      <el-table-column label="类型" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.productType === 2 ? 'warning' : 'primary'" effect="plain" size="small">
            {{ row.productType === 2 ? '食品' : '装备' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="storeName" label="所属网点" min-width="160" show-overflow-tooltip />
      <el-table-column prop="stockNum" label="当前库存" width="100" align="center" />
      <el-table-column prop="minStock" label="预警阈值" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.stockNum <= (row.minStock || 0)" type="danger" effect="plain" size="small">需补货</el-tag>
          <el-tag v-else type="success" effect="plain" size="small">正常</el-tag>
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

    <el-dialog v-model="inboundVisible" title="装备/食品入库登记" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="商品 ID" required>
          <el-input-number v-model="form.equipmentId" :min="1" controls-position="right" style="width: 180px" />
          <span class="hint">请从上方列表确认商品 ID</span>
        </el-form-item>
        <el-form-item label="入库数量" required>
          <el-input-number v-model="form.quantity" :min="1" controls-position="right" style="width: 180px" />
        </el-form-item>
        <el-form-item v-if="isFood" label="生产日期">
          <el-date-picker v-model="form.productionDate" type="date" value-format="YYYY-MM-DD" placeholder="选择生产日期" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inboundVisible = false">取消</el-button>
        <el-button type="primary" @click="submitInbound">确认入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getStorageEquipmentPage, equipmentInbound } from '@/api/opt'
import { getStoreList } from '@/api/public'

const rows = ref([])
const stores = ref([])
const page = reactive({ current: 1, size: 8, total: 0 })
const query = reactive({ storeId: null, keyword: '' })
const inboundVisible = ref(false)
const form = reactive({ equipmentId: null, quantity: 1, productionDate: '', remark: '' })

const isFood = computed(() => {
  const hit = rows.value.find((r) => r.id === form.equipmentId)
  return hit ? hit.productType === 2 : false
})

async function load() {
  const res = await getStorageEquipmentPage({
    current: page.current,
    size: page.size,
    storeId: query.storeId,
    keyword: query.keyword
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
  query.keyword = ''
  page.current = 1
  load()
}

function openInbound() {
  form.equipmentId = null
  form.quantity = 1
  form.productionDate = ''
  form.remark = ''
  inboundVisible.value = true
}

async function submitInbound() {
  if (!form.equipmentId) {
    ElMessage.warning('请输入商品 ID')
    return
  }
  await equipmentInbound({ ...form })
  ElMessage.success('入库成功')
  inboundVisible.value = false
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

.hint {
  margin-left: 10px;
  font-size: 12px;
  color: #94a3b8;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
