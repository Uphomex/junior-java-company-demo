# 09 Vue Router 与前后端联调

本章讲两件事：

1. SPA 的前端路由怎么工作的 —— Vue Router
2. 前端跑在 5173、后端跑在 8080，怎么让它俩通信 —— 跨域 / 代理 / CORS

## 1. SPA 为什么需要"前端路由"

传统多页应用（MPA）：

- 浏览器访问 `/employees` → 后端返回 `employees.html`
- 浏览器访问 `/departments` → 后端返回 `departments.html`
- 每次切页都是**整页刷新**

SPA 只有一个 `index.html`，所有"页面切换"都靠 JS 把当前组件换掉：

- URL 变成 `/employees` 时，渲染 `EmployeeListView`
- URL 变成 `/departments` 时，渲染 `DepartmentListView`
- **不刷新页面**，体验像桌面应用

Vue Router 负责"监听 URL 变化 → 切换组件"。

## 2. 项目里的路由配置

```ts
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/views/layout/MainLayout.vue'),
    redirect: '/employees',
    children: [
      {
        path: '/employees',
        name: 'employees',
        component: () => import('@/views/employee/EmployeeListView.vue'),
        meta: { title: '员工管理' }
      },
      {
        path: '/departments',
        name: 'departments',
        component: () => import('@/views/department/DepartmentListView.vue'),
        meta: { title: '部门管理' }
      },
      {
        path: '/attendance',
        name: 'attendance',
        component: () => import('@/views/attendance/AttendanceView.vue'),
        meta: { title: '考勤打卡' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.afterEach((to) => {
  document.title = `${(to.meta?.title as string) ?? '系统'} - 教学版`
})

export default router
```

要点：

- **嵌套路由**：父级 `/` 用 `MainLayout` 提供"左侧菜单 + 顶部 + 内容区"框架，子级 `/employees`、`/departments` 等被填充到 `<RouterView />` 占位符里。
- **懒加载**：`() => import('...')` 让对应代码只在访问时下载，减小首屏 chunk。
- **meta**：自定义元信息，常见于 `title`、`requiresAuth`、`roles`。
- **`createWebHistory()`**：HTML5 history 模式，URL 没有 `#`；备选 `createWebHashHistory()` 带 `#/employees`。

## 3. `<router-link>` 与 `<RouterView />`

```vue
<RouterLink to="/employees">员工管理</RouterLink>
<router-link :to="{ name: 'employees', query: { keyword: '张' } }">员工管理</router-link>

<RouterView />
```

- `<RouterLink>` 渲染成 `<a>`，但点击**不会**触发整页刷新，而是由 Router 拦截后切换组件。
- 也可以用对象形式 `{ name, params, query }`。

编程式跳转：

```ts
import { useRouter } from 'vue-router'

const router = useRouter()
router.push('/departments')
router.push({ name: 'employees', query: { id: 1 } })
router.back()
router.replace('/login')   // 替换历史，无法后退
```

## 4. 取参数

```ts
import { useRoute } from 'vue-router'

const route = useRoute()

console.log(route.path)             // /employees
console.log(route.query.keyword)    // ?keyword=张
console.log(route.params.id)        // 路径 /employees/:id 的 id
console.log(route.meta.title)
```

> `useRoute`（响应式当前路由）和 `useRouter`（操作路由的对象）别搞混。

## 5. 路由守卫：登录鉴权常用

```ts
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.token) {
    next({ name: 'login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})
```

钩子：

- `beforeEach`：所有路由跳转前
- `beforeResolve`：组件解析后、跳转前
- `afterEach`：跳转后（常用于改 title、上报埋点）

## 6. 前后端联调：跨域问题

浏览器的"同源策略"：协议 + 域名 + 端口三者完全相同才算同源。我们的环境：

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`

端口不同 → 跨域。直接 fetch 会被浏览器拦截，看到 console 报：

```text
Access to XMLHttpRequest at 'http://localhost:8080/api/employees' from origin
'http://localhost:5173' has been blocked by CORS policy.
```

解决方案两种：

### 方案 A：Vite 开发代理（推荐用于开发）

```ts
// vite.config.ts
export default defineConfig({
  // ...
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

原理：

- 前端代码 `fetch('/api/employees')` 由 Vite **拦截**
- Vite 自己作为 HTTP 客户端去访问 `http://localhost:8080/api/employees`
- 因为这是服务器到服务器的请求，**没有浏览器同源限制**
- 把结果原样返回给前端

请求路径变化：

```text
浏览器 → http://localhost:5173/api/employees    (Vite dev server)
Vite  → http://localhost:8080/api/employees    (Spring Boot)
```

### 方案 B：后端开 CORS（生产/真实部署用）

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

后端响应头会带 `Access-Control-Allow-Origin`，浏览器看到就放行。

**生产环境一定要收紧 `allowedOriginPatterns`，不能 `"*"`，否则任意网站都能调你的接口。**

### 两种方案怎么选

| 环境 | 推荐 |
| --- | --- |
| 开发 | Vite 代理（前端简单一些，不依赖后端配置） |
| 测试 / 生产 | 后端 CORS / Nginx 反向代理（部署灵活） |

本项目两种都开，开发体验最顺滑。

## 7. CORS 预检请求 OPTIONS

浏览器对**复杂请求**（带自定义头、`Content-Type: application/json` 等）会先发一个 OPTIONS 预检：

```text
OPTIONS /api/employees
Origin: http://localhost:5173
Access-Control-Request-Method: POST
Access-Control-Request-Headers: Content-Type
```

后端要返回：

```text
HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Content-Type
```

Spring Boot 的 `WebMvcConfigurer#addCorsMappings` 自动处理预检，无需额外操作。

## 8. 真实联调步骤

假设你拿到一个新需求 "**新增员工**"，怎么从前端跑到数据库？

1. **看接口文档（或直接看后端代码）**
   - URL: `POST /api/employees`
   - Body: `{ name, phone, departmentId, ... }`
   - Response: `{ code, message, data }`
2. **写 TS 类型** `types/index.ts`：`EmployeeCreateRequest`
3. **写 API 封装** `api/employee.ts`：
   ```ts
   create(payload: EmployeeCreateRequest): Promise<Employee> {
     return request.post('/employees', payload)
   }
   ```
4. **写页面**：
   - ElForm + ElInput 收集数据
   - ElForm 校验通过后调 `employeeApi.create(form)`
   - 成功后 ElMessage 提示、关闭对话框、刷新列表
5. **联调时打开浏览器 DevTools → Network**：
   - 看请求 URL / method / headers / payload 对不对
   - 看响应 status code / body
   - 失败时 Console 一般有红字提示

## 9. 调试技巧

- **改前端不生效**：检查是不是开了浏览器的"禁用缓存"（DevTools → Network 勾选 "Disable cache"）。
- **接口 404**：注意 `baseURL` 拼接，看实际请求的完整 URL 是不是预期。
- **CORS 报错**：先看后端是否开 CORS，再看 Vite 代理是否生效。
- **状态码 415 (Unsupported Media Type)**：通常是 `Content-Type` 没设成 `application/json`。
- **后端返回 500**：去后端控制台看异常栈。
- **打开 IDEA 的 HTTP Client**：用 `docs/api.http` 直接发请求，绕开前端，确认后端工作正常。

## 10. 课后练习

1. 给路由加一个 `/login`，对应一个 `LoginView`（不用真做后端，先做个表单）。
2. 给 `beforeEach` 加上鉴权逻辑：未登录访问任何页面都跳 `/login`。
3. 把 Vite 代理改成代理到一个**非本机**地址（如 `http://192.168.x.x:8080`），重启 dev server 验证依然可以联调。

下一章：[10-Git 与 GitHub 小白教程](./10-Git%20与%20GitHub%20小白教程.md)
