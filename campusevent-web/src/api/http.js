import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 12000,
  withCredentials: true,
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.code === 0) {
      return body.data
    }
    const message = body?.message || '请求失败'
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常'
    if (status === 401) {
      const isSessionProbe = String(error.config?.url || '').includes('/auth/me')
      if (!isSessionProbe) {
        ElMessage.warning('登录已失效，请重新登录')
        if (!window.location.pathname.startsWith('/login')) {
          window.location.href = '/login'
        }
      }
    } else if (status === 403) {
      ElMessage.error(message || '没有权限')
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  },
)

export default http