<template>
  <div class="page-container">
    <div class="page-head">
      <h2 class="page-title">用户管理</h2>
      <p class="page-subtitle">管理普通用户与客服账号，可新增、启用或禁用客服账号</p>
    </div>

    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索用户名 / 姓名 / 手机号" clearable style="width: 240px" @keyup.enter="load" />
      <el-button type="primary" @click="load">搜索</el-button>
      <el-button @click="openCreate">新增客服账号</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="realName" label="姓名" width="130" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="角色" min-width="180">
        <template #default="{ row }">
          <el-tag v-for="r in row.roles" :key="r" :type="roleType(r)" round style="margin-right: 6px">
            {{ roleName(r) }}
          </el-tag>
          <span v-if="!row.roles || !row.roles.length" class="muted">无角色</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" round>{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" type="danger" plain @click="toggleStatus(row, 0)">禁用</el-button>
          <el-button v-else size="small" type="success" plain @click="toggleStatus(row, 1)">启用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="load"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="新增客服账号" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" placeholder="初始密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="客服姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUserPage, addCsAccount, updateUserStatus } from '@/api/cs'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ current: 1, size: 10, keyword: '' })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ username: '', password: '', realName: '', phone: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

function roleName(code) {
  return { ROLE_USER: '普通用户', ROLE_CS: '客服', ROLE_ADMIN: '超级管理员' }[code] || code
}

function roleType(code) {
  return { ROLE_USER: 'info', ROLE_CS: 'warning', ROLE_ADMIN: 'danger' }[code] || 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await getAdminUserPage(query)
    list.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.username = ''
  form.password = ''
  form.realName = ''
  form.phone = ''
  dialogVisible.value = true
}

async function submit() {
  await formRef.value.validate()
  await addCsAccount(form)
  ElMessage.success('客服账号创建成功')
  dialogVisible.value = false
  load()
}

async function toggleStatus(row, status) {
  await ElMessageBox.confirm(`确认${status === 1 ? '启用' : '禁用'}账号「${row.username}」？`, '提示', { type: 'warning' })
  await updateUserStatus(row.id, status)
  ElMessage.success('操作成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.muted {
  color: var(--camp-muted, #8b9a94);
  font-size: 12px;
}
</style>
