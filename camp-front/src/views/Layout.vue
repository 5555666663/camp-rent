<template>
  <div class="layout">
    <header class="header">
      <div class="header-inner">
        <button v-if="showBack" class="back-btn" title="返回上一页" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </button>

        <router-link to="/" class="logo">
          <span class="logo-icon">⛺</span>
          <span class="logo-text">
            <span class="logo-title">露营装备租赁</span>
            <span class="logo-sub">CAMP RENTAL</span>
          </span>
        </router-link>

        <nav class="nav">
          <router-link to="/map" class="nav-item">
            <el-icon><LocationFilled /></el-icon>
            <span>网点地图</span>
          </router-link>
          <router-link to="/nearest" class="nav-item">
            <el-icon><Aim /></el-icon>
            <span>就近网点</span>
          </router-link>
          <router-link to="/equipment" class="nav-item">
            <el-icon><Box /></el-icon>
            <span>装备浏览</span>
          </router-link>
          <router-link to="/recommend" class="nav-item">
            <el-icon><Sunny /></el-icon>
            <span>装备推荐</span>
          </router-link>
          <router-link to="/my-orders" class="nav-item">
            <el-icon><Tickets /></el-icon>
            <span>我的订单</span>
          </router-link>
        </nav>

        <div class="user-area">
          <template v-if="userStore.isLogin">
            <el-dropdown @command="handleCommand" trigger="click">
              <span class="user-chip">
                <span class="avatar">{{ avatarText }}</span>
                <span class="user-name">{{ userStore.username }}</span>
                <el-icon class="arrow"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="orders">
                    <el-icon><Tickets /></el-icon>我的订单
                  </el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="admin">
                    <el-icon><Setting /></el-icon>后台管理
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text round @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" round @click="$router.push('/login?mode=register')">免费注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="main">
      <router-view />
    </main>

    <footer class="footer">
      <div class="footer-inner">
        <span>⛺ 露营装备租赁</span>
        <span class="dot">·</span>
        <span>基于 SpringBoot + Vue3 的多网点露营装备租赁系统</span>
        <span class="dot">·</span>
        <span>毕业设计项目</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store'
import { ElMessage } from 'element-plus'
import {
  LocationFilled,
  Aim,
  Box,
  Tickets,
  Setting,
  SwitchButton,
  ArrowDown,
  ArrowLeft,
  Sunny
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 首页（装备浏览）不显示返回键，其余页面显示
const showBack = computed(() => route.path !== '/equipment' && route.path !== '/')

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/equipment')
  }
}

const avatarText = computed(() => {
  const name = userStore.username || 'U'
  return name.charAt(0).toUpperCase()
})

function handleCommand(cmd) {
  if (cmd === 'orders') {
    router.push('/my-orders')
  } else if (cmd === 'admin') {
    router.push('/admin')
  } else if (cmd === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ---------- 顶部导航（64px 白底 + 1px 描边） ---------- */
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #ffffff;
  border-bottom: 1px solid var(--camp-line);
}

.header-inner {
  max-width: 1120px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 0 24px;
}

/* 返回键 */
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 32px;
  padding: 0 12px 0 9px;
  border: 1px solid var(--camp-line);
  border-radius: var(--camp-radius-sm);
  background: #ffffff;
  color: var(--camp-ink-2);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 150ms ease-out;
  white-space: nowrap;
}

.back-btn:hover {
  color: var(--camp-primary);
  border-color: #dbeafe;
  background: var(--camp-primary-soft);
}

.back-btn .el-icon {
  font-size: 15px;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.logo-icon {
  width: 34px;
  height: 34px;
  border-radius: var(--camp-radius-sm);
  display: grid;
  place-items: center;
  font-size: 18px;
  background: var(--camp-primary-soft);
  color: var(--camp-primary);
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}

.logo-title {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--camp-ink);
}

.logo-sub {
  font-size: 9px;
  font-weight: 500;
  letter-spacing: 0.12em;
  color: var(--camp-muted);
}

/* 导航项 */
.nav {
  display: flex;
  gap: 4px;
  flex: 1;
}

.nav-item {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: var(--camp-radius-sm);
  color: var(--camp-muted);
  font-size: 14px;
  font-weight: 500;
  transition: background-color 150ms ease-out, color 150ms ease-out;
}

.nav-item .el-icon {
  font-size: 15px;
}

.nav-item:hover {
  background: #f1f5f9;
  color: var(--camp-ink-2);
}

.nav-item.router-link-active {
  background: var(--camp-primary-soft);
  color: var(--camp-primary);
}

.nav-item.router-link-active::after {
  content: none;
}

.nav-item.router-link-active .el-icon {
  opacity: 1;
}

/* 用户区 */
.user-area {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  padding: 6px 12px 6px 6px;
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
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 13px;
  font-weight: 800;
  color: #fff;
  background: #1e40af;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--camp-ink);
}

.arrow {
  font-size: 12px;
  color: var(--camp-muted);
}

/* 主体 */
.main {
  flex: 1;
}

/* 页脚 */
.footer {
  border-top: 1px solid var(--camp-line);
  background: rgba(255, 255, 255, 0.6);
  padding: 26px 24px;
}

.footer-inner {
  max-width: 1240px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  color: var(--camp-muted);
  font-size: 13px;
}

.footer-inner .dot {
  opacity: 0.5;
}
</style>
