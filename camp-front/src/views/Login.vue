<template>
  <div class="login-page">
    <div class="login-shell">
      <!-- 左侧品牌区 -->
      <aside class="brand">
        <div class="brand-glow glow-1"></div>
        <div class="brand-glow glow-2"></div>

        <div class="brand-top">
          <span class="brand-logo">⛺</span>
          <span class="brand-name">露营装备租赁</span>
        </div>

        <div class="brand-body">
          <h1>把周末<br />交给山野与晚风</h1>
          <p>多网点独立库存 · 在线预约 · 归还即完成</p>

          <ul class="features">
            <li><span class="ico">🗺️</span>地图选点，就近取还</li>
            <li><span class="ico">🏕️</span>帐篷睡袋炊具，一站租齐</li>
            <li><span class="ico">📦</span>网点库存隔离，下单即锁定</li>
          </ul>
        </div>

        <div class="brand-foot">SpringBoot · Vue3 · MyBatis-Plus</div>
      </aside>

      <!-- 右侧表单区 -->
      <section class="form-panel">
        <div class="form-inner">
          <h2>{{ mode === 'login' ? '欢迎回来' : '创建账号' }}</h2>
          <p class="form-desc">
            {{ mode === 'login' ? '登录后即可预约装备、管理订单' : '注册一个账号，开始你的露营之旅' }}
          </p>

          <el-tabs v-model="mode" stretch class="login-tabs">
            <el-tab-pane label="登录" name="login">
              <el-form ref="loginRef" :model="loginForm" :rules="loginRules" size="large">
                <el-form-item prop="username">
                  <el-input v-model="loginForm.username" placeholder="用户名" :prefix-icon="User" />
                </el-form-item>
                <el-form-item prop="password">
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="密码"
                    :prefix-icon="Lock"
                    show-password
                    @keyup.enter="handleLogin"
                  />
                </el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  round
                  style="width: 100%; margin-top: 6px"
                  :loading="loading"
                  @click="handleLogin"
                >
                  登 录
                </el-button>
              </el-form>

              <div class="demo-tip">
                <span class="demo-label">测试账号</span>
                <div class="demo-chips">
                  <button class="chip" @click="fillDemo('admin', '123456')">管理员 admin / 123456</button>
                  <button class="chip" @click="fillDemo('zhangsan', '123456')">用户 zhangsan / 123456</button>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="注册" name="register">
              <el-form ref="registerRef" :model="registerForm" :rules="registerRules" size="large">
                <el-form-item prop="username">
                  <el-input v-model="registerForm.username" placeholder="用户名" :prefix-icon="User" />
                </el-form-item>
                <el-form-item prop="realName">
                  <el-input v-model="registerForm.realName" placeholder="真实姓名" :prefix-icon="Avatar" />
                </el-form-item>
                <el-form-item prop="phone">
                  <el-input v-model="registerForm.phone" placeholder="手机号" :prefix-icon="Iphone" />
                </el-form-item>
                <el-form-item prop="password">
                  <el-input
                    v-model="registerForm.password"
                    type="password"
                    placeholder="密码（不少于6位）"
                    :prefix-icon="Lock"
                    show-password
                  />
                </el-form-item>
                <el-form-item prop="confirmPassword">
                  <el-input
                    v-model="registerForm.confirmPassword"
                    type="password"
                    placeholder="确认密码"
                    :prefix-icon="Lock"
                    show-password
                  />
                </el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  round
                  style="width: 100%; margin-top: 6px"
                  :loading="loading"
                  @click="handleRegister"
                >
                  注 册
                </el-button>
              </el-form>
            </el-tab-pane>
          </el-tabs>

          <router-link to="/" class="back-home">← 先随便逛逛</router-link>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock, Avatar, Iphone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api/auth'
import { useUserStore } from '@/store'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const mode = ref(route.query.mode === 'register' ? 'register' : 'login')
const loading = ref(false)

const loginRef = ref()
const registerRef = ref()
const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', realName: '', phone: '', password: '', confirmPassword: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        value === registerForm.password ? callback() : callback(new Error('两次密码不一致'))
      },
      trigger: 'blur'
    }
  ]
}

function fillDemo(username, password) {
  loginForm.username = username
  loginForm.password = password
}

function handleLogin() {
  loginRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(loginForm)
      userStore.setLogin(res.token, res)
      ElMessage.success('登录成功')
      // RBAC：按角色决定登录后的落地页（超级管理员→后台，客服→客服工作台，普通用户→前台首页）
      const redirect = route.query.redirect
      const roles = res.roles || []
      let home = '/'
      if (roles.includes('ROLE_ADMIN')) {
        home = '/admin'
      } else if (roles.includes('ROLE_CS')) {
        home = '/cs'
      }
      router.push(redirect || home)
    } finally {
      loading.value = false
    }
  })
}

function handleRegister() {
  registerRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await register({
        username: registerForm.username,
        realName: registerForm.realName,
        phone: registerForm.phone,
        password: registerForm.password
      })
      ElMessage.success('注册成功，请登录')
      mode.value = 'login'
      loginForm.username = registerForm.username
      loginForm.password = ''
    } finally {
      loading.value = false
    }
  })
}

watch(mode, () => {
  if (mode.value === 'register') registerForm.confirmPassword = ''
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 24px;
  background:
    radial-gradient(900px 500px at 12% 0%, rgba(61, 220, 151, 0.2), transparent 60%),
    radial-gradient(800px 500px at 100% 100%, rgba(22, 184, 118, 0.14), transparent 55%),
    #f5faf7;
}

.login-shell {
  width: min(1040px, 100%);
  min-height: 620px;
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  background: #fff;
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 24px 80px rgba(16, 42, 33, 0.16), 0 2px 8px rgba(16, 42, 33, 0.05);
  animation: shellIn 0.7s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes shellIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.985);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ---------- 左侧品牌 ---------- */
.brand {
  position: relative;
  padding: 46px 44px;
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(155deg, #0c3d2b 0%, #0f6b48 45%, #16b876 100%);
  overflow: hidden;
}

.brand-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.55;
  pointer-events: none;
}

.glow-1 {
  width: 340px;
  height: 340px;
  right: -110px;
  top: -90px;
  background: radial-gradient(circle, rgba(126, 240, 190, 0.9), transparent 65%);
}

.glow-2 {
  width: 280px;
  height: 280px;
  left: -100px;
  bottom: -110px;
  background: radial-gradient(circle, rgba(59, 220, 151, 0.75), transparent 65%);
}

.brand-top {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.brand-logo {
  width: 46px;
  height: 46px;
  border-radius: 15px;
  display: grid;
  place-items: center;
  font-size: 24px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.28);
  backdrop-filter: blur(6px);
}

.brand-name {
  font-size: 17px;
  font-weight: 800;
  letter-spacing: 0.01em;
}

.brand-body {
  position: relative;
  z-index: 1;
}

.brand-body h1 {
  font-size: 40px;
  line-height: 1.18;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin-bottom: 16px;
}

.brand-body > p {
  font-size: 14.5px;
  color: rgba(255, 255, 255, 0.78);
  letter-spacing: 0.02em;
}

.features {
  list-style: none;
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  gap: 13px;
}

.features li {
  display: flex;
  align-items: center;
  gap: 11px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.92);
}

.features .ico {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-size: 15px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.brand-foot {
  position: relative;
  z-index: 1;
  font-size: 11.5px;
  letter-spacing: 0.14em;
  color: rgba(255, 255, 255, 0.5);
  text-transform: uppercase;
}

/* ---------- 右侧表单 ---------- */
.form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 46px 44px;
}

.form-inner {
  width: 100%;
  max-width: 380px;
}

.form-inner h2 {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--camp-ink);
}

.form-desc {
  font-size: 13.5px;
  color: var(--camp-muted);
  margin: 6px 0 22px;
}

.login-tabs :deep(.el-tabs__header) {
  margin-bottom: 22px;
}

.login-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: var(--camp-line);
}

.login-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 700;
  color: var(--camp-muted);
}

.login-tabs :deep(.el-tabs__item.is-active) {
  color: var(--camp-primary-dark);
}

.login-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--camp-primary), var(--camp-primary-2));
}

.demo-tip {
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px dashed var(--camp-line);
}

.demo-label {
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.1em;
  color: var(--camp-muted);
  text-transform: uppercase;
}

.demo-chips {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
}

.chip {
  text-align: left;
  padding: 9px 13px;
  border-radius: 11px;
  border: 1px solid var(--camp-line);
  background: #f8fcfa;
  color: var(--camp-ink-2);
  font-size: 12.5px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}

.chip:hover {
  border-color: rgba(22, 184, 118, 0.45);
  background: var(--camp-primary-soft);
  color: var(--camp-primary-dark);
  transform: translateX(2px);
}

.back-home {
  display: inline-block;
  margin-top: 24px;
  font-size: 13px;
  color: var(--camp-muted);
  font-weight: 600;
}

.back-home:hover {
  color: var(--camp-primary-dark);
}

/* 响应式 */
@media (max-width: 880px) {
  .login-shell {
    grid-template-columns: 1fr;
    min-height: 0;
  }

  .brand {
    padding: 32px 28px;
  }

  .brand-body h1 {
    font-size: 28px;
  }

  .features {
    display: none;
  }

  .form-panel {
    padding: 34px 28px 40px;
  }
}
</style>
