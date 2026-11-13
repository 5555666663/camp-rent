<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openAdd">新增分类</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="categoryName" label="分类名称" min-width="200" />
      <el-table-column prop="sort" label="排序" width="120" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryPage, addCategory, updateCategory, deleteCategory } from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ current: 1, size: 10 })
const form = reactive({ id: null, categoryName: '', sort: 0 })

const rules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'change' }]
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await getCategoryPage(query)
    list.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function openAdd() {
  Object.assign(form, { id: null, categoryName: '', sort: 0 })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

function remove(row) {
  ElMessageBox.confirm('确认删除该分类？', '提示', { type: 'warning' }).then(async () => {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    load()
  })
}

function submit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (form.id) {
        await updateCategory(form)
      } else {
        await addCategory(form)
      }
      ElMessage.success('保存成功')
      dialogVisible.value = false
      load()
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
