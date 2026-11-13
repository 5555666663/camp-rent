<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索网点名称/地址" clearable style="width: 220px" @keyup.enter="search" @clear="search" />
      <el-select v-model="query.city" placeholder="城市筛选" clearable style="width: 140px" @change="search">
        <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="search">
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="search">查询</el-button>
      <div style="flex: 1"></div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增网点</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="storeName" label="网点名称" min-width="150" />
      <el-table-column prop="city" label="城市" width="90" />
      <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="businessHours" label="营业时间" width="150" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '停用' : '启用' }}
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑网点' : '新增网点'" width="640px" @opened="initMap" @closed="destroyMap">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="网点名称" prop="storeName">
              <el-input v-model="form.storeName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="城市" prop="city">
              <el-input v-model="form.city" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="营业时间" prop="businessHours">
              <el-input v-model="form.businessHours" placeholder="如 09:00-21:00" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="网点位置" required>
          <div class="map-tip">点击地图选点，自动回填经纬度</div>
          <div id="picker-map" class="picker-map"></div>
          <div class="coord">
            <el-input v-model="form.longitude" placeholder="经度" style="width: 160px" />
            <el-input v-model="form.latitude" placeholder="纬度" style="width: 160px" />
          </div>
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
import { getStorePage, addStore, updateStore, deleteStore, updateStoreStatus } from '@/api/admin'
import { getStoreCities } from '@/api/public'
import { loadAMap } from '@/utils/amap'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const cities = ref([])
const dialogVisible = ref(false)
const formRef = ref()
let pickerMap = null
let pickerMarker = null
const query = reactive({ current: 1, size: 10, keyword: '', city: '', status: null })
const form = reactive({
  id: null, storeName: '', city: '', address: '', phone: '', businessHours: '', longitude: '', latitude: ''
})

const rules = {
  storeName: [{ required: true, message: '请输入网点名称', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  businessHours: [{ required: true, message: '请输入营业时间', trigger: 'blur' }]
}

onMounted(async () => {
  cities.value = (await getStoreCities()) || []
  load()
})

async function load() {
  loading.value = true
  try {
    const res = await getStorePage(query)
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
    id: null, storeName: '', city: '', address: '', phone: '', businessHours: '', longitude: '', latitude: ''
  })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  ElMessageBox.confirm(`确认${next === 1 ? '启用' : '停用'}该网点？`, '提示', { type: 'warning' }).then(async () => {
    await updateStoreStatus(row.id, next)
    ElMessage.success('操作成功')
    load()
  })
}

function remove(row) {
  ElMessageBox.confirm('确认删除该网点？', '提示', { type: 'warning' }).then(async () => {
    await deleteStore(row.id)
    ElMessage.success('删除成功')
    load()
  })
}

function submit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!form.longitude || !form.latitude) {
      ElMessage.warning('请在地图上点击选择网点位置')
      return
    }
    submitting.value = true
    try {
      if (form.id) {
        await updateStore(form)
      } else {
        await addStore(form)
      }
      ElMessage.success('保存成功')
      dialogVisible.value = false
      load()
    } finally {
      submitting.value = false
    }
  })
}

async function initMap() {
  try {
    const AMap = await loadAMap()
    const center = form.longitude ? [Number(form.longitude), Number(form.latitude)] : [116.397, 39.909]
    pickerMap = new AMap.Map('picker-map', { zoom: 12, center })
    if (form.longitude) {
      pickerMarker = new AMap.Marker({ position: center })
      pickerMarker.setMap(pickerMap)
    }
    pickerMap.on('click', (e) => {
      form.longitude = e.lnglat.getLng().toFixed(6)
      form.latitude = e.lnglat.getLat().toFixed(6)
      if (pickerMarker) pickerMarker.setMap(null)
      pickerMarker = new AMap.Marker({ position: [e.lnglat.getLng(), e.lnglat.getLat()] })
      pickerMarker.setMap(pickerMap)
    })
  } catch (e) {
    ElMessage.warning(e.message || '地图加载失败')
  }
}

function destroyMap() {
  if (pickerMap) {
    pickerMap.destroy()
    pickerMap = null
    pickerMarker = null
  }
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
.map-tip {
  font-size: 12px;
  color: #ff9800;
  margin-bottom: 6px;
}
.picker-map {
  width: 100%;
  height: 260px;
  border-radius: 8px;
  margin-bottom: 8px;
}
.coord {
  display: flex;
  gap: 12px;
}
</style>
