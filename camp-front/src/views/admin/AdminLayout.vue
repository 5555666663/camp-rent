<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside width="238px" class="aside">
      <div class="aside-logo">
        <span class="logo-icon">⛺</span>
        <span class="logo-text">
          <span class="logo-title">露营装备后台</span>
          <span class="logo-sub">ADMIN CONSOLE</span>
        </span>
      </div>

      <div class="menu-label">功能导航</div>

      <el-menu
        :default-active="activeMenu"
        router
        background-color="transparent"
        text-color="#475569"
        active-text-color="#1e40af"
        class="side-menu"
      >
        <el-menu-item index="/admin/stats"><el-icon><DataAnalysis /></el-icon>数据统计</el-menu-item>
        <el-menu-item index="/admin/store"><el-icon><OfficeBuilding /></el-icon>网点管理</el-menu-item>
        <el-menu-item index="/admin/category"><el-icon><Menu /></el-icon>分类管理</el-menu-item>
        <el-menu-item index="/admin/equipment"><el-icon><Box /></el-icon>装备管理</el-menu-item>
        <el-menu-item index="/admin/maintain"><el-icon><Tools /></el-icon>维保管理</el-menu-item>
        <el-menu-item index="/admin/order"><el-icon><Tickets /></el-icon>订单管理</el-menu-item>
        <el-menu-item index="/admin/comment"><el-icon><ChatDotRound /></el-icon>评价管理</el-menu-item>
        <el-menu-item index="/admin/user"><el-icon><UserFilled /></el-icon>用户管理</el-menu-item>
      </el-menu>

      <div class="aside-foot">
        <button class="back-front" @click="$router.push('/')">
          <el-icon><Back /></el-icon>
          <span>返回前台</span>
        </button>
      </div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="header-title">{{ route.meta.title || '管理后台' }}</span>
          <span class="header-crumb">露营装备租赁管理端</span>
        </div>
        <div class="header-user">
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-chip">
              <span class="avatar">{{ avatarText }}</span>
              <span class="user-meta">
                <span class="user-name">{{ userStore.username }}</span>
                <span class="user-role">管理员</span>
              </span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="front">
                  <el-icon><HomeFilled /></el-icon>返回前台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  DataAnalysis,
  OfficeBuilding,
  Menu,
  Box,
  Tools,
  Tickets,
  ChatDotRound,
  Back,
  HomeFilled,
  SwitchButton,
  ArrowDown,
  UserFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const avatarText = computed(() => (userStore.username || 'A').charAt(0).toUpperCase())

function handleCommand(cmd) {
  if (cmd === 'front') {
    router.push('/')
  } else if (cmd === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

/* ---------- 侧边栏 ---------- */
.aside {
  display: flex;
  flex-direction: column;
  background: var(--camp-surface);
  border-right: 1px solid var(--camp-line);
}

.aside-logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid var(--camp-line);
}

.logo-icon {
  width: 34px;
  height: 34px;
  border-radius: var(--camp-radius-sm);
  display: grid;
  place-items: center;
  font-size: 17px;
  background: var(--camp-primary-soft);
  color: var(--camp-primary);
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}

.logo-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--camp-ink);
  letter-spacing: -0.01em;
}

.logo-sub {
  font-size: 9px;
  font-weight: 500;
  letter-spacing: 0.14em;
  color: var(--camp-muted);
}

.menu-label {
  padding: 20px 24px 8px;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.06em;
  color: var(--camp-muted);
}

.side-menu {
  border-right: none;
  padding: 0 4px;
  flex: 1;
  overflow-y: auto;
}

.side-menu :deep(.el-menu-item) {
  border-radius: 11px;
  margin: 3px 8px;
}

.aside-foot {
  padding: 14px 16px 20px;
  border-top: 1px solid var(--camp-line);
}

.back-front {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  border-radius: var(--camp-radius-sm);
  border: 1px solid var(--camp-line);
  background: #ffffff;
  color: var(--camp-ink-2);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 150ms ease-out;
  font-family: inherit;
}

.back-front:hover {
  background: var(--camp-primary-soft);
  border-color: #dbeafe;
  color: var(--camp-primary);
}

/* ---------- 顶栏 ---------- */
.header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid var(--camp-line);
  padding: 0 28px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.header-title {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--camp-ink);
}

.header-crumb {
  font-size: 12px;
  color: var(--camp-muted);
}

.header-user {
  display: flex;
  align-items: center;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 6px 14px 6px 6px;
  border-radius: 999px;
  cursor: pointer;
  background: #fff;
  border: 1px solid var(--camp-line);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  outline: none;
}

.user-chip:hover {
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.1);
  transform: translateY(-1px);
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 13px;
  font-weight: 800;
  color: #fff;
  background: #1e40af;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-name {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--camp-ink);
}

.user-role {
  font-size: 10.5px;
  color: var(--camp-primary-dark);
  font-weight: 600;
}

.arrow {
  font-size: 12px;
  color: var(--camp-muted);
}

/* ---------- 内容区 ---------- */
.main {
  background: var(--camp-bg);
  padding: 24px 28px 40px;
}
</style>
