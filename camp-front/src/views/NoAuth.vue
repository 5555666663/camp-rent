<template>
  <div class="noauth">
    <div class="card">
      <div class="code">403</div>
      <h2>无访问权限</h2>
      <p>当前账号（{{ roleText }}）没有该页面的访问权限，请使用具备权限的账号登录，或返回可访问的页面。</p>
      <div class="actions">
        <el-button type="primary" @click="goHome">返回我的工作台</el-button>
        <el-button @click="relogin">重新登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { primaryRole, homePathForRole, ROLE_LABELS } from '@/config/consoleMenu'

const router = useRouter()
const userStore = useUserStore()

const role = computed(() => primaryRole(userStore.roles || [], userStore.isAdmin))
const roleText = computed(() => ROLE_LABELS[role.value] || '未登录')

function goHome() {
  if (!userStore.isLogin) {
    router.replace('/login')
    return
  }
  router.replace(homePathForRole(role.value))
}

function relogin() {
  userStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.noauth {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  padding: 20px;
}

.card {
  width: 100%;
  max-width: 460px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 32px 28px;
  text-align: center;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
}

.code {
  font-size: 40px;
  font-weight: 700;
  color: #1e40af;
  line-height: 1.1;
  margin-bottom: 6px;
}

h2 {
  margin: 0 0 10px;
  font-size: 18px;
  color: #0f172a;
}

p {
  margin: 0 0 22px;
  font-size: 13px;
  line-height: 1.8;
  color: #64748b;
}

.actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}
</style>
