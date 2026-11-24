<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="query.storeId" placeholder="全部网点" clearable style="width: 200px">
        <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="success" plain @click="openAdd">新增维保记录</el-button>
    </div>

    <el-table :data="rows" border stripe empty-text="暂无维保记录">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="equipmentName" label="装备" min-width="170" show-overflow-tooltip />
      <el-table-column prop="storeName" label="所属网点" min-width="160" show-overflow-tooltip />
      <el-table-column prop="maintainType" label="维保类型" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.maintainType)" effect="plain" size="small">{{ row.maintainType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="维保内容" min-width="220" show-overflow-tooltip />
      <el-table-column prop="maintainTime" label="维保时间" width="170" />
      <el-table-column prop="operator" label="操作人" width="110" />
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

    <el-dialog v-model="addVisible" title="新增维保记录" width="520px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="选择网点" required>
          <el-select v-model="form.storeId" placeholder="请选择网点" style="width: 100%">
            <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="装备 ID" required>
          <el-input-number v-model="form.equipmentId" :min="1" controls-position="right" style="width: 180px" />
        </el-form-item>
        <el-form-item label="维保类型" required>
          <el-radio-group v-model="form.maintainType">
            <el-radio label="维修" />
            <el-radio label="保养" />
            <el-radio label="报废" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="维保时间" required>
          <el-date-picker v-model="form.maintainTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择维保时间" />
        </el-form-item>
        <el-form-item label="操作人" required>
          <el-input v-model="form.operator" placeholder="填写操作人姓名" />
        </el-form-item>
        <el-form-item label="维保内容">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="记录故障、更换配件、保养情况等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getStorageMaintainPage, addMaintainByStorage } from '@/api/opt'
import { getStoreList } from '@/api/public'

const rows = ref([])
const stores = ref([])
const page = reactive({ current: 1, size: 8, total: 0 })
const query = reactive({ storeId: null })
const addVisible = ref(false)
const form = reactive({
  storeId: null,
  equipmentId: null,
  maintainType: '维修',
  maintainTime: '',
  operator: '',
  content: ''
})

function typeTag(t) {
  return t === '报废' ? 'danger' : t === '维修' ? 'warning' : 'success'
}

async function load() {
  const res = await getStorageMaintainPage({ current: page.current, size: page.size, storeId: query.storeId })
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

function openAdd() {
  form.storeId = null
  form.equipmentId = null
  form.maintainType = '维修'
  form.maintainTime = ''
  form.operator = ''
  form.content = ''
  addVisible.value = true
}

async function submit() {
  if (!form.storeId || !form.equipmentId || !form.maintainTime || !form.operator) {
    ElMessage.warning('请填写网点、装备、维保时间与操作人')
    return
  }
  await addMaintainByStorage({ ...form })
  ElMessage.success('维保记录已保存')
  addVisible.value = false
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
