import { defineStore } from 'pinia'

// ============================================================
// 用户状态管理（token + 用户信息，持久化到 localStorage）
// ============================================================
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    // 是否登录
    isLogin: (state) => !!state.token,
    // 当前用户的角色编码集合（RBAC）
    roles: (state) => (state.user && state.user.roles) || (state.user && state.user.role === 1 ? ['ROLE_ADMIN'] : ['ROLE_USER']),
    // 是否管理员
    isAdmin: (state) => {
      const list = (state.user && state.user.roles) || (state.user && state.user.role === 1 ? ['ROLE_ADMIN'] : ['ROLE_USER'])
      return list.includes('ROLE_ADMIN')
    },
    // 是否客服
    isCs: (state) => {
      const list = (state.user && state.user.roles) || (state.user && state.user.role === 1 ? ['ROLE_ADMIN'] : ['ROLE_USER'])
      return list.includes('ROLE_CS')
    },
    // 用户名
    username: (state) => (state.user ? state.user.username : '')
  },
  actions: {
    // 保存登录信息
    setLogin(token, user) {
      this.token = token
      this.user = user
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
    },
    // 退出登录
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
