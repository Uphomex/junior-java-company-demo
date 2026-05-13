# 08 Pinia 与 Axios：状态管理与 HTTP 封装

本章解决两个问题：

1. 多个组件之间怎么**共享状态**？—— Pinia
2. 怎么**统一**和后端发请求、加 token、处理错误？—— Axios 实例 + 拦截器

## 1. Pinia：Vue 官方推荐的状态管理库

为什么不直接用 `ref` 在父组件里管理然后 props 往下传？

- 跨层级传 props（"props drilling"）写起来很烦
- 不同路由的页面间没有父子关系，没法 props 传

Pinia 是一个**全局响应式仓库**，谁都能拿、谁都能改。

### 安装与注册

```ts
// main.ts
import { createPinia } from 'pinia'
app.use(createPinia())
```

### 定义一个 Store

```ts
// stores/department.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { departmentApi } from '@/api/department'
import type { Department } from '@/types'

export const useDepartmentStore = defineStore('department', () => {
  const departments = ref<Department[]>([])
  const loading = ref(false)

  async function refresh(force = false) {
    if (!force && departments.value.length > 0) return departments.value
    loading.value = true
    try {
      departments.value = await departmentApi.list()
      return departments.value
    } finally {
      loading.value = false
    }
  }

  function findById(id: number | null | undefined) {
    if (id == null) return undefined
    return departments.value.find((d) => d.id === id)
  }

  return { departments, loading, refresh, findById }
})
```

要点：

- `defineStore('id', setupFn)` 是组合式写法，和组件一样用 ref / computed。
- 返回的对象就是 store 的"公开 API"。
- `'department'` 是 id，必须全局唯一，在 devtools 里能看到。

### 在组件里使用

```vue
<script setup lang="ts">
import { onMounted } from 'vue'
import { useDepartmentStore } from '@/stores/department'

const store = useDepartmentStore()

onMounted(() => store.refresh())
</script>

<template>
  <el-select v-model="form.departmentId">
    <el-option v-for="d in store.departments" :key="d.id" :label="d.name" :value="d.id" />
  </el-select>
</template>
```

- 直接调 `useDepartmentStore()`，每次得到的是同一个全局实例。
- store 的 `ref` 字段在 template 里自动解包，不用 `.value`。

### 持久化 store 到 localStorage

实际项目里，登录态的 user store 需要持久化，刷新页面后还在。常用插件 `pinia-plugin-persistedstate`：

```ts
import { createPinia } from 'pinia'
import piniaPersist from 'pinia-plugin-persistedstate'

const pinia = createPinia()
pinia.use(piniaPersist)
app.use(pinia)
```

```ts
defineStore('user', () => { /* ... */ }, {
  persist: true   // 整个 store 都存
})
```

## 2. Axios：浏览器 HTTP 客户端

虽然现代浏览器也支持 `fetch`，但 axios 优势：

- 自动 JSON 序列化/反序列化
- 拦截器、超时、取消请求
- 错误处理更友好

### 安装

```bash
pnpm add axios
```

### 全局唯一实例 + 拦截器

`utils/request.ts` 是核心：

```ts
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
  headers: { 'Content-Type': 'application/json;charset=UTF-8' }
})

// 请求拦截器：登录后这里可以塞 token
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // const token = localStorage.getItem('token')
    // if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  },
  (error: AxiosError) => Promise.reject(error)
)

// 响应拦截器：统一处理后端的 ApiResponse 包装 + 错误提示
request.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0) return body.data
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
```

### 拦截器的"魔法"

请求会按下面顺序经过拦截器：

```text
business code → request.interceptors.request → axios 发送 →
后端响应 → request.interceptors.response → business code
```

我们在 response 拦截器里"剥皮"：

- 后端返回 `{ code, message, data, timestamp }`
- 业务代码只关心 `data`
- 失败的 code 统一弹 ElMessage、抛错，业务代码用 try/catch 包不包都行（一般不包）

这种做法的代价：业务代码看不到 `code` 和 `message` 字段了。如果某些场景需要拿到原始返回值，可以改成"返回整个 body"，让业务代码自己解。

### 按业务模块拆分接口封装

```ts
// api/employee.ts
import request from '@/utils/request'
import type { Employee, EmployeeCreateRequest, EmployeePageQuery, PageResult } from '@/types'

export const employeeApi = {
  page(query: EmployeePageQuery): Promise<PageResult<Employee>> {
    return request.get('/employees', { params: query })
  },
  detail(id: number): Promise<Employee> {
    return request.get(`/employees/${id}`)
  },
  create(payload: EmployeeCreateRequest): Promise<Employee> {
    return request.post('/employees', payload)
  },
  update(id: number, payload: EmployeeCreateRequest): Promise<Employee> {
    return request.put(`/employees/${id}`, payload)
  },
  leave(id: number): Promise<void> {
    return request.delete(`/employees/${id}`)
  }
}
```

业务代码这样调：

```ts
const result = await employeeApi.page({ pageNum: 1, pageSize: 10 })
list.value = result.list
total.value = result.total
```

类型来自 `types/index.ts`，IDE 自动补全 / 类型检查。

## 3. 拦截器进阶用法

### 携带 token

```ts
request.interceptors.request.use((config) => {
  const userStore = useUserStore()    // 注意：这里要把 store 调用包到拦截器里
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})
```

> 在拦截器函数外**直接** `const store = useUserStore()` 会失败，因为 store 还没注册。
> 解决：在拦截器**内**调用 `useUserStore()`。

### 401 自动跳登录

```ts
request.interceptors.response.use(
  (resp) => resp,
  (err) => {
    if (err.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    return Promise.reject(err)
  }
)
```

### 多次重复请求合并 / 节流

如果某个接口 1 秒内被点 10 次，避免重复发：

```ts
const pendingMap = new Map<string, AbortController>()

request.interceptors.request.use((config) => {
  const key = `${config.method}-${config.url}-${JSON.stringify(config.params)}`
  pendingMap.get(key)?.abort()
  const controller = new AbortController()
  config.signal = controller.signal
  pendingMap.set(key, controller)
  return config
})
```

入门阶段不用上这个，等真碰到性能问题再加。

## 4. 环境变量与多套环境

Vite 用 `.env`、`.env.development`、`.env.production`：

```env
# .env
VITE_API_BASE_URL=/api

# .env.production
VITE_API_BASE_URL=https://api.example.com
```

代码里读：

```ts
const base = import.meta.env.VITE_API_BASE_URL
```

> 只有以 `VITE_` 开头的变量才会被打进前端代码。

## 5. 真实大厂前端常见组合

| 场景 | 库 |
| --- | --- |
| HTTP | axios + 自封装实例 |
| 状态管理 | Pinia（Vue3）/ Redux Toolkit（React） |
| 表单 | Element Plus / Ant Design Vue / 自封装 |
| 表格 | el-table / vxe-table / a-table |
| 富文本 | TinyMCE / wangEditor / Tiptap |
| 图表 | ECharts |
| 国际化 | vue-i18n |
| 权限 | 路由 meta + Pinia user store + 后端动态菜单 |

## 6. 课后练习

1. 给 `request.ts` 加一个"全局 loading 计数器"：发请求 +1，结束 -1，当 > 0 时显示一个全局 loading 条。
2. 给项目加一个 `useUserStore`：包含 `token` / `name`，配合 `persist: true` 让刷新不丢登录态。
3. 给 axios 拦截器加 401 自动跳 `/login` 的逻辑（即使现在没有登录页，先写好骨架）。

下一章：[09-Vue Router 与前后端联调](./09-Vue%20Router%20与前后端联调.md)
