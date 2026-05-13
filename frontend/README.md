# 前端：员工与考勤管理（Vue3 + TypeScript + Element Plus）

这是 `junior-java-company-demo` 的前端工程，对接同仓库根目录的 Spring Boot 后端。详细教学请回到仓库根目录的 [docs/教学/](../docs/%E6%95%99%E5%AD%A6/) 系列文档。

## 技术栈

- Vue 3（组合式 API + `<script setup>`）
- TypeScript（严格模式）
- Vite 5（开发服务器 + 构建）
- Element Plus（UI 组件库，中文 locale）
- Pinia（状态管理）
- Vue Router 4（HTML5 history）
- Axios（HTTP 封装 + 拦截器统一解 `ApiResponse`）

## 目录结构

```text
frontend/
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
├── .env                  本地环境变量（VITE_API_BASE_URL）
├── public/               静态资源
└── src/
    ├── main.ts           应用入口
    ├── App.vue           根组件，只放 <RouterView />
    ├── env.d.ts          Vite / Vue / Element Plus 类型声明
    ├── api/              后端接口封装（按业务模块拆分）
    ├── assets/           CSS / 图片
    ├── components/       通用组件（暂为空）
    ├── router/           Vue Router 路由配置
    ├── stores/           Pinia store
    ├── types/            TypeScript 类型定义
    ├── utils/            通用工具（含 axios 实例 request.ts）
    └── views/
        ├── layout/       MainLayout：左侧菜单 + 内容
        ├── employee/     员工管理页
        ├── department/   部门管理页
        └── attendance/   考勤打卡页
```

## 开发

```bash
pnpm install   # 安装依赖
pnpm dev       # 启动开发服务器（默认 5173）
```

`pnpm dev` 会启动 Vite，把 `/api/*` 请求转发到 `http://localhost:8080`（即后端），避免开发期跨域问题。

请确保后端已经启动（项目根目录 `mvn spring-boot:run`）。

## 构建

```bash
pnpm build         # 类型检查 + 打包到 dist/
pnpm preview       # 本地预览打包后的产物
```

## 环境变量

`.env` 默认：

```env
VITE_API_BASE_URL=/api
```

部署到生产时一般改成完整域名，如：

```env
VITE_API_BASE_URL=https://api.your-company.com
```

只有以 `VITE_` 开头的变量才会被 Vite 暴露给前端代码。

## 与后端如何对接

- 前端用 `request.get/post/put/delete`（封装在 `src/utils/request.ts`）。
- 拦截器自动剥掉后端 `ApiResponse` 的外壳，业务代码只关心 `data`。
- 失败的 `code` 会用 Element Plus 的 `ElMessage` 弹错，并 `Promise.reject`。

详细对接讲解：[docs/教学/09-Vue Router 与前后端联调](../docs/%E6%95%99%E5%AD%A6/09-Vue%20Router%20%E4%B8%8E%E5%89%8D%E5%90%8E%E7%AB%AF%E8%81%94%E8%B0%83.md)

## 学习路线

如果你是前端新手，建议阅读顺序：

1. [docs/教学/06-Vue3 + TypeScript 入门](../docs/%E6%95%99%E5%AD%A6/06-Vue3%20%2B%20TypeScript%20%E5%85%A5%E9%97%A8.md)
2. [docs/教学/07-Element Plus 组件库](../docs/%E6%95%99%E5%AD%A6/07-Element%20Plus%20%E7%BB%84%E4%BB%B6%E5%BA%93.md)
3. [docs/教学/08-Pinia 与 Axios](../docs/%E6%95%99%E5%AD%A6/08-Pinia%20%E4%B8%8E%20Axios.md)
4. [docs/教学/09-Vue Router 与前后端联调](../docs/%E6%95%99%E5%AD%A6/09-Vue%20Router%20%E4%B8%8E%E5%89%8D%E5%90%8E%E7%AB%AF%E8%81%94%E8%B0%83.md)

## License

MIT
