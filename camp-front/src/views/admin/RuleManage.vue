<template>
  <div>
    <div class="toolbar">
      <span class="tip">规则按优先级从小到大匹配，命中即返回；修改后立即生效，无需改代码。</span>
      <div style="flex: 1"></div>
      <el-button type="primary" @click="openAdd">新增规则</el-button>
      <el-button @click="openTest">规则试算</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="ruleName" label="规则名称" min-width="150" />
      <el-table-column prop="priority" label="优先级" width="90" />
      <el-table-column prop="conditionJson" label="触发条件（JSON）" min-width="240" show-overflow-tooltip />
      <el-table-column prop="actionJson" label="输出动作（JSON）" min-width="240" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            :model-value="row.enabled === 1"
            @change="(v) => handleToggle(row, v)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑规则' : '新增规则'" width="640px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="规则名称">
          <el-input v-model="form.ruleName" placeholder="例如：雷电或强降水-高危" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="1" :max="999" />
          <span class="tip" style="margin-left: 10px">数值越小越先匹配</span>
        </el-form-item>
        <el-form-item label="触发条件">
          <el-input
            v-model="form.conditionJson"
            type="textarea"
            :rows="4"
            placeholder='{"precipitationProbability":{"gte":70}} 支持 gte/gt/lte/lt/eq'
          />
        </el-form-item>
        <el-form-item label="输出动作">
          <el-input
            v-model="form.actionJson"
            type="textarea"
            :rows="4"
            placeholder='{"riskLevel":"high","sceneType":"hiking","riskTip":"...","remindText":"..."}'
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="testDialog" title="规则试算" width="560px">
      <el-form :model="testForm" label-width="120px">
        <el-form-item label="降水概率">
          <el-input-number v-model="testForm.precipitationProbability" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="是否下雨">
          <el-switch v-model="testForm.willRain" />
        </el-form-item>
        <el-form-item label="风力 km/h">
          <el-input-number v-model="testForm.windSpeed" :min="0" :max="120" />
        </el-form-item>
        <el-form-item label="紫外线">
          <el-input-number v-model="testForm.uvIndex" :min="0" :max="15" />
        </el-form-item>
        <el-form-item label="最高温 ℃">
          <el-input-number v-model="testForm.tempMax" :min="-20" :max="50" />
        </el-form-item>
        <el-form-item label="雷电预警">
          <el-switch v-model="testForm.thunder" />
        </el-form-item>
      </el-form>
      <div v-if="testResult" class="test-result">
        命中规则：<b>{{ testResult.ruleName || '无' }}</b>
        <pre>{{ testResult.action }}</pre>
      </div>
      <template #footer>
        <el-button @click="testDialog = false">关闭</el-button>
        <el-button type="primary" @click="handleTest">开始试算</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRuleList, addRule, updateRule, deleteRule, toggleRule, testRule } from '@/api/opt'

const loading = ref(false)
const list = ref([])
const dialog = ref(false)
const testDialog = ref(false)
const testResult = ref(null)

const form = reactive({ id: null, ruleName: '', priority: 10, conditionJson: '', actionJson: '', enabled: 1 })
const testForm = reactive({ precipitationProbability: 80, willRain: true, windSpeed: 20, uvIndex: 4, tempMax: 26, thunder: false })

async function load() {
  loading.value = true
  try {
    list.value = (await getRuleList()) || []
  } finally {
    loading.value = false
  }
}

function reset(data) {
  Object.assign(form, { id: null, ruleName: '', priority: 10, conditionJson: '', actionJson: '', enabled: 1 }, data || {})
}

function openAdd() {
  reset()
  dialog.value = true
}

function openEdit(row) {
  reset({ ...row })
  dialog.value = true
}

function openTest() {
  testResult.value = null
  testDialog.value = true
}

async function handleSave() {
  if (!form.ruleName) {
    ElMessage.warning('请填写规则名称')
    return
  }
  try {
    JSON.parse(form.conditionJson || '{}')
    JSON.parse(form.actionJson || '{}')
  } catch (e) {
    ElMessage.error('条件或动作不是合法 JSON')
    return
  }
  if (form.id) {
    await updateRule(form)
  } else {
    await addRule(form)
  }
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

async function handleToggle(row, val) {
  await toggleRule(row.id, val ? 1 : 0)
  row.enabled = val ? 1 : 0
  ElMessage.success(val ? '已启用' : '已停用')
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除规则「' + row.ruleName + '」？', '提示', { type: 'warning' })
  await deleteRule(row.id)
  ElMessage.success('已删除')
  load()
}

async function handleTest() {
  testResult.value = await testRule({ ...testForm })
}

onMounted(load)
</script>

<style scoped>
.tip {
  font-size: 13px;
  color: var(--camp-muted, #64748b);
}

.test-result {
  margin-top: 10px;
  background: var(--camp-primary-soft, #eff6ff);
  border-radius: 8px;
  padding: 12px 14px;
  font-size: 13px;
  color: var(--camp-ink-2, #334155);
}

.test-result pre {
  margin: 8px 0 0;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
