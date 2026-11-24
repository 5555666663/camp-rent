<template>
  <div class="console">
    <!-- 左侧菜单：可折叠，按角色动态渲染 -->
    <aside class="aside" :class="{ collapsed }">
      <div class="brand">
        <span class="brand-logo">⛺</span>
        <div v-show="!collapsed" class="brand-text">
          <strong>露营装备租赁</strong>
          <small>{{ groupTitle }}</small>
        </div>
      </div>

      <el-menu
        :default-active="activePath"
        :collapse="collapsed"
        :collapse-transition="false"
        class="aside-menu"
        router
      >
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="iconOf(m.icon)" /></el-icon>
          <template #title>{{ m.title }}</template>
        </el-menu-item>
      </el-menu>

      <div class="aside-foot">
        <button class="fold-btn" @click="toggleCollapse">
          <el-icon><DArrowLeft v-if="!collapsed" /><DArrowRight v-else /></el-icon>
          <span v-show="!collapsed">收起菜单</span>
        </button>
      </div>
    </aside>

    <!-- 右侧主内容 -->
    <section class="main">
      <header class="topbar">
        <div class="topbar-left">
          <button class="icon-btn" title="收起/展开菜单" @click="toggleCollapse">
            <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
          </button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>控制台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ pageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="topbar-right">
          <el-tag size="small" type="info" effect="plain">{{ roleLabel }}</el-tag>
          <el-dropdown @command="onCommand">
            <span class="user-chip">
              <span class="avatar">{{ (userStore.username || 'U').charAt(0).toUpperCase() }}</span>
              <span class="uname">{{ userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">返回前台</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import * as ElIcons from '@element-plus/icons-vue'
import { Fold, Expand, DArrowLeft, DArrowRight, ArrowDown } from '@element-plus/icons-vue'
import { menusForRole, primaryRole, ROLE_LABELS, GROUP_TITLES } from '@/config/consoleMenu'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const collapsed = ref(localStorage.getItem('console_collapsed') === '1')

const role = computed(() => primaryRole(userStore.roles || [], userStore.isAdmin))
const menus = computed(() => menusForRole(role.value))
const roleLabel = computed(() => ROLE_LABELS[role.value] || '用户')
const groupTitle = computed(() => GROUP_TITLES[role.value] || '控制台')
const activePath = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || '控制台')

function iconOf(name) {
  return ElIcons[name] || ElIcons.Menu
}

function toggleCollapse() {
  collapsed.value = !collapsed.value
  localStorage.setItem('console_collapsed', collapsed.value ? '1' : '0')
}

function onCommand(cmd) {
  if (cmd === 'home') {
    router.push('/equipment')
  } else if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.console {
  display: flex;
  min-height: 100vh;
  background: #f8fafc;
}

.aside {
  width: 224px;
  flex: 0 0 224px;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  transition: width 0.16s ease-out, flex-basis 0.16s ease-out;
}

.aside.collapsed {
  width: 64px;
  flex-basis: 64px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 64px;
  padding: 0 16px;
  border-bottom: 1px solid #e5e7eb;
  overflow: hidden;
}

.brand-logo {
  width: 30px;
  height: 30px;
  flex: 0 0 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #eff6ff;
  font-size: 16px;
}

.brand-text strong {
  display: block;
  font-size: 14px;
  color: #0f172a;
  line-height: 1.2;
  white-space: nowrap;
}

.brand-text small {
  display: block;
  font-size: 11px;
  color: #64748b;
  white-space: nowrap;
}

.aside-menu {
  flex: 1;
  border-right: none;
  padding: 8px;
  overflow-y: auto;
  overflow-x: hidden;
}

.aside-menu :deep(.el-menu-item) {
  height: 42px;
  line-height: 42px;
  margin-bottom: 2px;
  border-radius: 8px;
  font-size: 14px;
  color: #334155;
}

.aside-menu :deep(.el-menu-item:hover) {
  background: #f8fafc;
  color: #1e40af;
}

.aside-menu :deep(.el-menu-item.is-active) {
  background: #eff6ff;
  color: #1e40af;
  font-weight: 600;
}

.aside-foot {
  border-top: 1px solid #e5e7eb;
  padding: 8px;
}

.fold-btn {
  width: 100%;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  color: #475569;
  font-size: 13px;
  cursor: pointer;
}

.fold-btn:hover {
  background: #f8fafc;
  color: #1e40af;
  border-color: #bfdbfe;
}

.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: 64px;
  flex: 0 0 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.icon-btn {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  color: #475569;
  cursor: pointer;
}

.icon-btn:hover {
  background: #f8fafc;
  color: #1e40af;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 34px;
  padding: 0 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  color: #334155;
  font-size: 13px;
}

.user-chip:hover {
  background: #f8fafc;
  border-color: #bfdbfe;
}

.avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #1e40af;
  color: #fff;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.uname {
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content {
  flex: 1;
  padding: 20px;
  overflow: auto;
}
</style>
