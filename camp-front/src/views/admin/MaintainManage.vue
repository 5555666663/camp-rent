<template>
  <div>
    <div class="toolbar">
      <el-select v-model="query.storeId" placeholder="全部网点" clearable style="width: 160px" @change="search">
        <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
      </el-select>
      <el-select v-model="query.equipmentId" placeholder="全部装备" clearable filterable style="width: 200px" @change="search">
        <el-option v-for="e in equipments" :key="e.id" :label="e.eqName" :value="e.id" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="search">查询</el-button>
      <div style="flex: 1"></div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增维保记录</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="equipmentName" label="装备" min-width="130" />
      <el-table-column prop="storeName" label="所属网点" min-width="140" />
      <el-table-column label="维保类型" width="100">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.maintainType)">{{ row.maintainType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="维保内容" min-width="180" show-overflow-tooltip />
      <el-table-column prop="maintainTime" label="维保时间" width="170" />
      <el-table-column prop="operator" label="操作人" width="100" />
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
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

    <el-dialog v-model="dialogVisible" title="新增维保记录" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="选择装备" prop="equipmentId">
          <el-select v-model="form.equipmentId" placeholder="选择装备（自动带出网点）" filterable style="width: 100%" @change="onEquipChange">
            <el-option v-for="e in equipments" :key="e.id" :label="e.eqName" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属网点">
          <el-input :model-value="storeNameOf(form.storeId)" disabled />
        </el-form-item>
        <el-form-item label="维保类型" prop="maintainType">
          <el-select v-model="form.maintainType" placeholder="选择类型" style="width: 100%">
            <el-option label="维修" value="维修" />
            <el-option label="保养" value="保养" />
            <el-option label="报废" value="报废" />
          </el-select>
        </el-form-item>
        <el-form-item label="维保内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="维保时间" prop="maintainTime">
          <el-date-picker v-model="form.maintainTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="form.operator" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" />
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
import { ElMessage } from 'element-plus'
import { getMaintainPage, addMaintain, getAllStores, getAllEquipments } from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const stores = ref([])
const equipments = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ current: 1, size: 10, storeId: null, equipmentId: null })
const form = reactive({
  equipmentId: null, storeId: null, maintainType: '', content: '', maintainTime: '', operator: '', remark: ''
})

const rules = {
  equipmentId: [{ required: true, message: '请选择装备', trigger: 'change' }],
  maintainType: [{ required: true, message: '请选择维保类型', trigger: 'change' }],
  content: [{ required: true, message: '请填写维保内容', trigger: 'blur' }],
  maintainTime: [{ required: true, message: '请选择维保时间', trigger: 'change' }],
  operator: [{ required: true, message: '请填写操作人', trigger: 'blur' }]
}

function typeTag(t) {
  if (t === '维修') return 'warning'
  if (t === '保养') return 'primary'
  return 'danger'
}

onMounted(async () => {
  const [storeRes, equipRes] = await Promise.all([getAllStores(), getAllEquipments()])
  stores.value = storeRes || []
  equipments.value = equipRes || []
  load()
})

async function load() {
  loading.value = true
  try {
    const res = await getMaintainPage(query)
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
    equipmentId: null, storeId: null, maintainType: '', content: '',
    maintainTime: '', operator: '', remark: ''
  })
  dialogVisible.value = true
}

function onEquipChange(id) {
  const eq = equipments.value.find((e) => e.id === id)
  form.storeId = eq ? eq.storeId : null
}

function storeNameOf(id) {
  const s = stores.value.find((x) => x.id === id)
  return s ? s.storeName : ''
}

function submit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await addMaintain(form)
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
</style>
