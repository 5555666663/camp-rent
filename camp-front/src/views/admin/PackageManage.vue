<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="套餐名称" clearable style="width: 200px" @keyup.enter="search" />
      <el-select v-model="query.sceneType" placeholder="全部场景" clearable style="width: 150px">
        <el-option label="露营" value="露营" />
        <el-option label="徒步" value="徒步" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="success" plain @click="openAdd">新增套餐</el-button>
    </div>

    <el-table :data="rows" border stripe empty-text="暂无套餐数据">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="packageName" label="套餐名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="sceneType" label="适用场景" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.sceneType === '徒步' ? 'success' : 'primary'" effect="plain" size="small">{{ row.sceneType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="套餐说明" min-width="240" show-overflow-tooltip />
      <el-table-column prop="price" label="套餐价" width="100" align="right" />
      <el-table-column prop="originalPrice" label="原价" width="90" align="right" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggle(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑套餐' : '新增套餐'" width="560px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="套餐名称" required>
          <el-input v-model="form.packageName" placeholder="如：精致露营双人套餐" />
        </el-form-item>
        <el-form-item label="适用场景" required>
          <el-radio-group v-model="form.sceneType">
            <el-radio label="露营" />
            <el-radio label="徒步" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="套餐价" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" controls-position="right" style="width: 160px" />
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" controls-position="right" style="width: 160px" />
        </el-form-item>
        <el-form-item label="封面图 URL">
          <el-input v-model="form.coverImg" placeholder="填写图片地址，或使用 /img/... 内置插画" />
        </el-form-item>
        <el-form-item label="套餐说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="简述套餐包含的装备与适用人群" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPackagePage,
  addPackage,
  updatePackage,
  deletePackage,
  togglePackageStatus
} from '@/api/opt'

const rows = ref([])
const page = reactive({ current: 1, size: 8, total: 0 })
const query = reactive({ keyword: '', sceneType: null })
const dialogVisible = ref(false)
const form = reactive({
  id: null,
  packageName: '',
  sceneType: '露营',
  price: 0,
  originalPrice: 0,
  coverImg: '',
  description: '',
  status: 1
})

async function load() {
  const res = await getPackagePage({
    current: page.current,
    size: page.size,
    keyword: query.keyword,
    sceneType: query.sceneType
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
  query.keyword = ''
  query.sceneType = null
  page.current = 1
  load()
}

function openAdd() {
  Object.assign(form, {
    id: null,
    packageName: '',
    sceneType: '露营',
    price: 0,
    originalPrice: 0,
    coverImg: '',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  if (!form.packageName) {
    ElMessage.warning('请填写套餐名称')
    return
  }
  if (form.id) {
    await updatePackage({ ...form })
    ElMessage.success('已保存')
  } else {
    await addPackage({ ...form })
    ElMessage.success('已新增')
  }
  dialogVisible.value = false
  load()
}

async function toggle(row) {
  await togglePackageStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('操作成功')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除套餐「${row.packageName}」？`, '删除套餐', { type: 'warning' })
  await deletePackage(row.id)
  ElMessage.success('已删除')
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
