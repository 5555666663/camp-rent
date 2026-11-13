<template>
  <div class="layout">
    <header class="header">
      <div class="header-inner">
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
import { useRouter } from 'vue-router'
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
  Sunny
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

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

/* ---------- 顶部导航（毛玻璃） ---------- */
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: saturate(180%) blur(18px);
  -webkit-backdrop-filter: saturate(180%) blur(18px);
  border-bottom: 1px solid var(--camp-line);
}

.header-inner {
  max-width: 1240px;
  margin: 0 auto;
  height: 70px;
  display: flex;
  align-items: center;
  gap: 36px;
  padding: 0 24px;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
}

.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 13px;
  display: grid;
  place-items: center;
  font-size: 21px;
  background: linear-gradient(135deg, var(--camp-primary), var(--camp-primary-2));
  box-shadow: 0 8px 18px rgba(22, 184, 118, 0.32);
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}

.logo-title {
  font-size: 17px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--camp-ink);
}

.logo-sub {
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.16em;
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
  gap: 7px;
  padding: 10px 16px;
  border-radius: 12px;
  color: var(--camp-ink-2);
  font-size: 14.5px;
  font-weight: 600;
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.nav-item .el-icon {
  font-size: 16px;
  opacity: 0.75;
}

.nav-item:hover {
  background: rgba(22, 184, 118, 0.08);
  color: var(--camp-primary-dark);
}

.nav-item.router-link-active {
  background: var(--camp-primary-soft);
  color: var(--camp-primary-dark);
}

.nav-item.router-link-active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -16px;
  transform: translateX(-50%);
  width: 22px;
  height: 3px;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--camp-primary), var(--camp-primary-2));
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
  box-shadow: 0 1px 2px rgba(16, 42, 33, 0.04);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  outline: none;
}

.user-chip:hover {
  box-shadow: 0 6px 18px rgba(16, 42, 33, 0.1);
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
  background: linear-gradient(135deg, #2bbd7e, #7ee2b8);
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
