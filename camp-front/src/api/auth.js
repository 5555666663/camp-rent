import request from '@/utils/request'

// ============================================================
// 认证相关接口
// ============================================================

// 用户注册
export function register(data) {
  return request.post('/auth/register', data)
}

// 用户登录
export function login(data) {
  return request.post('/auth/login', data)
}

// 退出登录
export function logout() {
  return request.post('/auth/logout')
}

// 获取当前登录用户信息
export function getInfo() {
  return request.get('/auth/info')
}
