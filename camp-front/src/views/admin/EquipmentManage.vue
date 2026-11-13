<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索装备名称" clearable style="width: 200px" @keyup.enter="search" @clear="search" />
      <el-select v-model="query.storeId" placeholder="全部网点" clearable style="width: 160px" @change="search">
        <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
      </el-select>
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 140px" @change="search">
        <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="search">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="search">查询</el-button>
      <div style="flex: 1"></div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增装备</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <img v-if="row.eqImg" :src="row.eqImg" class="eq-img" />
          <div v-else class="eq-img placeholder">⛺</div>
        </template>
      </el-table-column>
      <el-table-column prop="eqName" label="装备名称" min-width="140" />
      <el-table-column prop="brand" label="品牌" width="100" />
      <el-table-column prop="storeName" label="归属网点" width="130" />
      <el-table-column prop="categoryName" label="分类" width="90" />
      <el-table-column prop="pricePerDay" label="日租金" width="90">
        <template #default="{ row }">¥{{ row.pricePerDay }}</template>
      </el-table-column>
      <el-table-column prop="deposit" label="押金" width="90">
        <template #default="{ row }">¥{{ row.deposit }}</template>
      </el-table-column>
      <el-table-column prop="stockNum" label="库存" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑装备' : '新增装备'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="装备名称" prop="eqName">
              <el-input v-model="form.eqName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="form.brand" placeholder="如 牧高笛 / 挪客 / 自嗨锅" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="规格">
              <el-input v-model="form.spec" placeholder="如 2 人 / 210×150cm / 320g" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归属网点" prop="storeId">
              <el-select v-model="form.storeId" placeholder="选择网点" style="width: 100%">
                <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存" prop="stockNum">
              <el-input-number v-model="form.stockNum" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="日租金" prop="pricePerDay">
              <el-input-number v-model="form.pricePerDay" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="押金" prop="deposit">
              <el-input-number v-model="form.deposit" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="装备图片">
          <el-input v-model="form.eqImg" placeholder="图片URL地址（http/https）" />
        </el-form-item>
        <el-form-item label="商品介绍">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="介绍商品的特点、适用场景、材质参数等" />
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
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getEquipmentAdminPage, getAllEquipments, getAllStores,
  addEquipment, updateEquipment, deleteEquipment, updateEquipmentStatus
} from '@/api/admin'
import { getCategoryList } from '@/api/public'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const stores = ref([])
const categories = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ current: 1, size: 10, keyword: '', storeId: null, categoryId: null, status: null })
const form = reactive({
  id: null, eqName: '', brand: '', spec: '', storeId: null, categoryId: null, stockNum: 1,
  pricePerDay: 0, deposit: 0, eqImg: '', description: ''
})

const rules = {
  eqName: [{ required: true, message: '请输入装备名称', trigger: 'blur' }],
  storeId: [{ required: true, message: '请选择归属网点', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  stockNum: [{ required: true, message: '请输入库存', trigger: 'change' }],
  pricePerDay: [{ required: true, message: '请输入日租金', trigger: 'change' }],
  deposit: [{ required: true, message: '请输入押金', trigger: 'change' }]
}

onMounted(async () => {
  const [storeRes, catRes] = await Promise.all([getAllStores(), getCategoryList()])
  stores.value = storeRes || []
  categories.value = catRes || []
  load()
})

async function load() {
  loading.value = true
  try {
    const res = await getEquipmentAdminPage(query)
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

function openAdd() {
  Object.assign(form, {
    id: null, eqName: '', brand: '', spec: '', storeId: null, categoryId: null, stockNum: 1,
    pricePerDay: 0, deposit: 0, eqImg: '', description: ''
  })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  ElMessageBox.confirm(`确认${next === 1 ? '上架' : '下架'}该装备？`, '提示', { type: 'warning' }).then(async () => {
    await updateEquipmentStatus(row.id, next)
    ElMessage.success('操作成功')
    load()
  })
}

function remove(row) {
  ElMessageBox.confirm('确认删除该装备？', '提示', { type: 'warning' }).then(async () => {
    await deleteEquipment(row.id)
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
        await updateEquipment(form)
      } else {
        await addEquipment(form)
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
.eq-img {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  object-fit: cover;
  background: #eef7ef;
}
.eq-img.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #a5d6a7;
}
</style>
