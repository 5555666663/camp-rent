import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// ============================================================
// Axios 封装：统一请求拦截、响应拦截
// ============================================================
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动携带 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理业务码与异常
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端统一返回 { code, msg, data }，成功时直接返回 data 业务数据
    if (res.code === 200) {
      return res.data
    }
    if (res.code === 401) {
      // 未登录或登录过期
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      ElMessage.error(res.msg || '登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(res)
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(res)
  },
  (error) => {
    ElMessage.error('网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
