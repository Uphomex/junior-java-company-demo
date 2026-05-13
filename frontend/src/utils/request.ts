// HTTP 请求统一封装
//
// 教学要点：
//   1. 用 axios.create 创建一个独立实例，方便单独配置 baseURL / 超时 / 拦截器，
//      不污染全局 axios。
//   2. baseURL 走环境变量 VITE_API_BASE_URL，默认 "/api"，开发时由 Vite proxy
//      转发到 http://localhost:8080，避免跨域。生产部署时改成真实域名即可。
//   3. 响应拦截器统一解开后端返回的 ApiResponse 包装：
//        { code, message, data, timestamp }
//      只把 data 交给上层业务代码使用，code 非 0 时抛出错误。
//   4. 用 ElMessage 把后端业务错误统一弹出来，避免每个调用方都自己 try/catch。

import axios, { AxiosError } from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp?: string
}

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器：可以在这里统一加 token、loading 等
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理后端业务码 + HTTP 错误
request.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0) {
        return body.data
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return response.data
  },
  (error: AxiosError<ApiResponse<unknown>>) => {
    const msg = error.response?.data?.message || error.message || '网络异常'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
