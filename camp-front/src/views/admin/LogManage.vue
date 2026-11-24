<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.username" placeholder="按操作人搜索" clearable style="width: 180px" />
      <el-select v-model="query.module" placeholder="模块" clearable style="width: 150px; margin-left: 10px">
        <el-option label="订单" value="订单" />
        <el-option label="装备" value="装备" />
        <el-option label="网点" value="网点" />
        <el-option label="仓储" value="仓储" />
        <el-option label="用户" value="用户" />
      </el-select>
      <el-button type="primary" style="margin-left: 10px" @click="reload">查询</el-button>
      <div style="flex: 1"></div>
      <span class="tip">记录所有后台写操作，用于责任追溯</span>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="操作人" width="120" />
      <el-table-column prop="roleCode" label="角色" width="130" />
      <el-table-column prop="module" label="模块" width="110" />
      <el-table-column prop="action" label="操作" min-width="140" />
      <el-table-column prop="uri" label="请求地址" min-width="200" show-overflow-tooltip />
      <el-table-column label="结果" width="90">
        <template #default="{ row }">
          <el-tag :type="row.resultCode === 200 ? 'success' : 'danger'" size="small">
            {{ row.resultCode }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="costMs" label="耗时(ms)" width="100" />
      <el-table-column prop="ip" label="IP" width="130" />
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column label="详情" width="90">
        <template #default="{ row }">
          <el-button text type="primary" @click="openDetail(row)">查看</el-button>
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
      @current-change="(p) => { query.current = p; load() }"
    />

    <el-dialog v-model="dialog" title="操作日志详情" width="620px">
      <el-descriptions :column="1" border v-if="detail">
        <el-descriptions-item label="操作人">{{ detail.username }}（{{ detail.roleCode }}）</el-descriptions-item>
        <el-descriptions-item label="模块 / 操作">{{ detail.module }} / {{ detail.action }}</el-descriptions-item>
        <el-descriptions-item label="请求">{{ detail.method }} {{ detail.uri }}</el-descriptions-item>
        <el-descriptions-item label="参数">
          <pre class="params">{{ detail.params }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="结果">
          {{ detail.resultCode }} {{ detail.errorMsg || '' }}
        </el-descriptions-item>
        <el-descriptions-item label="耗时 / IP">{{ detail.costMs }} ms / {{ detail.ip }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getLogPage } from '@/api/opt'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const dialog = ref(false)
const detail = ref(null)
const query = reactive({ current: 1, size: 10, username: '', module: '' })

async function load() {
  loading.value = true
  try {
    const res = await getLogPage({ ...query })
    list.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function reload() {
  query.current = 1
  load()
}

function openDetail(row) {
  detail.value = row
  dialog.value = true
}

onMounted(load)
</script>

<style scoped>
.tip {
  font-size: 13px;
  color: var(--camp-muted, #64748b);
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

.params {
  margin: 0;
  max-height: 220px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
}
</style>
