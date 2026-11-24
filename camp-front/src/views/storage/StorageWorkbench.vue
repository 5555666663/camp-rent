<template>
  <div class="page-container">
    <div class="page-head">
      <h2 class="page-title">仓储工作台</h2>
      <p class="page-subtitle">装备入库、批次管理与维保登记（仅仓储人员 / 管理员）</p>
    </div>

    <el-tabs v-model="tab" class="work-tabs">
      <el-tab-pane label="装备入库" name="inbound">
        <div class="toolbar">
          <el-select v-model="query.storeId" placeholder="全部网点" clearable style="width: 180px" @change="loadEquipment">
            <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
          </el-select>
          <el-input v-model="query.keyword" placeholder="商品名称/品牌" clearable style="width: 200px; margin-left: 10px" @keyup.enter="loadEquipment" />
          <el-button type="primary" style="margin-left: 10px" @click="loadEquipment">查询</el-button>
        </div>

        <el-table :data="equipmentList" v-loading="loading" border stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="eqName" label="商品名称" min-width="180" />
          <el-table-column prop="brand" label="品牌" width="110" />
          <el-table-column prop="storeName" label="所属网点" min-width="150" />
          <el-table-column prop="stockNum" label="当前库存" width="110" />
          <el-table-column label="类型" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.productType === 2 ? 'warning' : 'primary'">
                {{ row.productType === 2 ? '食品' : '装备' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button text type="primary" @click="openInbound(row)">入库</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="pager"
          background
          layout="prev, pager, next, total"
          :total="total"
          :page-size="query.size"
          :current-page="query.current"
          @current-change="(p) => { query.current = p; loadEquipment() }"
        />
      </el-tab-pane>

      <el-tab-pane label="食品批次" name="batch">
        <div class="toolbar">
          <el-input v-model="batchQuery.batchNo" placeholder="批次号" clearable style="width: 200px" />
          <el-button type="primary" style="margin-left: 10px" @click="loadBatch">查询</el-button>
        </div>
        <el-table :data="batchList" v-loading="batchLoading" border stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="batchNo" label="批次号" min-width="190" />
          <el-table-column prop="equipmentId" label="商品ID" width="100" />
          <el-table-column prop="storeId" label="网点ID" width="100" />
          <el-table-column prop="productionDate" label="生产日期" width="130" />
          <el-table-column prop="expireDate" label="到期日期" width="130" />
          <el-table-column prop="quantity" label="数量" width="90" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="['success', 'warning', 'danger', 'info'][row.status] || 'info'">
                {{ ['正常', '临期', '过期', '售罄'][row.status] || '未知' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          class="pager"
          background
          layout="prev, pager, next, total"
          :total="batchTotal"
          :page-size="batchQuery.size"
          :current-page="batchQuery.current"
          @current-change="(p) => { batchQuery.current = p; loadBatch() }"
        />
      </el-tab-pane>

      <el-tab-pane label="维保登记" name="maintain">
        <el-form :model="maintainForm" label-width="110px" class="maintain-form">
          <el-form-item label="装备ID">
            <el-input v-model="maintainForm.equipmentId" placeholder="填写需要维保的装备ID" />
          </el-form-item>
          <el-form-item label="维保类型">
            <el-select v-model="maintainForm.maintainType" style="width: 180px">
              <el-option label="维修" value="维修" />
              <el-option label="保养" value="保养" />
              <el-option label="报废" value="报废" />
            </el-select>
          </el-form-item>
          <el-form-item label="维保内容">
            <el-input v-model="maintainForm.content" type="textarea" :rows="3" placeholder="例如：更换帐篷支撑杆、清洗睡袋" />
          </el-form-item>
          <el-form-item label="维保时间">
            <el-date-picker v-model="maintainForm.maintainTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
          <el-form-item label="操作人">
            <el-input v-model="maintainForm.operator" placeholder="填写操作人姓名" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleMaintain">提交维保记录</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="inboundDialog" title="装备入库" width="460px">
      <el-form :model="inboundForm" label-width="90px">
        <el-form-item label="商品">{{ inboundForm.eqName }}</el-form-item>
        <el-form-item label="入库数量">
          <el-input-number v-model="inboundForm.quantity" :min="1" :max="999" />
        </el-form-item>
        <el-form-item v-if="inboundForm.productType === 2" label="生产日期">
          <el-date-picker v-model="inboundForm.productionDate" type="date" value-format="YYYY-MM-DD" placeholder="选择生产日期" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inboundDialog = false">取消</el-button>
        <el-button type="primary" @click="handleInbound">确认入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getStoreList } from '@/api/public'
import {
  getStorageEquipmentPage,
  equipmentInbound,
  getBatchPage,
  addMaintainByStorage
} from '@/api/opt'

const tab = ref('inbound')
const loading = ref(false)
const batchLoading = ref(false)
const stores = ref([])
const equipmentList = ref([])
const total = ref(0)
const batchList = ref([])
const batchTotal = ref(0)
const inboundDialog = ref(false)

const query = reactive({ current: 1, size: 10, storeId: null, keyword: '' })
const batchQuery = reactive({ current: 1, size: 10, batchNo: '' })
const inboundForm = reactive({ equipmentId: null, eqName: '', productType: 1, quantity: 1, productionDate: '' })
const maintainForm = reactive({ equipmentId: '', maintainType: '维修', content: '', maintainTime: '', operator: '' })

async function loadEquipment() {
  loading.value = true
  try {
    const res = await getStorageEquipmentPage({ ...query })
    equipmentList.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function loadBatch() {
  batchLoading.value = true
  try {
    const res = await getBatchPage({ ...batchQuery })
    batchList.value = res.records || []
    batchTotal.value = res.total || 0
  } finally {
    batchLoading.value = false
  }
}

function openInbound(row) {
  Object.assign(inboundForm, {
    equipmentId: row.id,
    eqName: row.eqName,
    productType: row.productType,
    quantity: 1,
    productionDate: ''
  })
  inboundDialog.value = true
}

async function handleInbound() {
  await equipmentInbound({ ...inboundForm })
  ElMessage.success('入库成功')
  inboundDialog.value = false
  loadEquipment()
}

async function handleMaintain() {
  if (!maintainForm.equipmentId || !maintainForm.content) {
    ElMessage.warning('请填写装备ID与维保内容')
    return
  }
  await addMaintainByStorage({ ...maintainForm })
  ElMessage.success('维保记录已提交')
  Object.assign(maintainForm, { equipmentId: '', maintainType: '维修', content: '', maintainTime: '', operator: '' })
}

onMounted(async () => {
  stores.value = (await getStoreList()) || []
  loadEquipment()
  loadBatch()
})
</script>

<style scoped>
.page-head {
  margin-bottom: 14px;
}

.work-tabs {
  background: #fff;
  border: 1px solid var(--camp-line, #e5e7eb);
  border-radius: var(--camp-radius, 12px);
  padding: 8px 16px 16px;
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

.maintain-form {
  max-width: 560px;
  margin-top: 10px;
}
</style>
