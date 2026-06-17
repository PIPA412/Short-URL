import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// Create axios instance
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

// ==================== Request Interceptor ====================

service.interceptors.request.use(
  (config) => {
    // Read token directly from localStorage to avoid Pinia
    // initialization order issues (store may not be ready yet)
    const stored = localStorage.getItem('shortlink-user')
    if (stored) {
      try {
        const parsed = JSON.parse(stored)
        if (parsed.token) {
          config.headers['Authorization'] = `Bearer ${parsed.token}`
        }
      } catch {
        // corrupted storage — ignore
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ==================== Response Interceptor ====================

// Track whether we're already redirecting to login (prevent redirect storms)
let isRedirectingToLogin = false

service.interceptors.response.use(
  (response) => {
    const res = response.data

    // Backend returns R<T>: { code, msg, data }
    // Non-200 business codes are treated as errors
    if (res && typeof res.code === 'number' && res.code !== 200) {
      const msg = res.msg || 'Request failed'
      ElMessage.error(msg)
      return Promise.reject(new Error(msg))
    }

    // Return the full R<T> object so callers can access .code, .msg, .data
    return res
  },
  (error) => {
    // No response from server (network error, CORS, etc.)
    if (!error.response) {
      ElMessage.error('网络连接失败，请检查后端服务是否启动')
      return Promise.reject(error)
    }

    const { status, data } = error.response

    switch (status) {
      case 401: {
        // Token expired or invalid — clear stored data and redirect
        if (!isRedirectingToLogin) {
          isRedirectingToLogin = true
          localStorage.removeItem('shortlink-user')
          // Only show message if it's NOT a login attempt (login failure is handled by the page)
          const isLoginRequest = error.config?.url?.includes('/auth/login')
          if (!isLoginRequest) {
            ElMessage.error('登录已过期，请重新登录')
          }
          router.push({
            path: '/login',
            query: { redirect: router.currentRoute.value.fullPath }
          }).finally(() => {
            isRedirectingToLogin = false
          })
        }
        break
      }
      case 403:
        ElMessage.error('没有权限访问此资源')
        break
      case 404:
        ElMessage.error('请求的资源不存在')
        break
      case 429:
        ElMessage.error('请求过于频繁，请稍后再试')
        break
      default:
        ElMessage.error(data?.msg || `服务器错误 (${status})`)
    }

    return Promise.reject(error)
  }
)

export default service
