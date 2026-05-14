# 初级 Java + Vue3 全栈学习项目：员工与考勤管理

这是一个面向**初级 Java / 全栈开发者**的大厂级教学样例项目。它覆盖国内公司日常 Java 后端 + Vue3 前端的常见技术栈，并配有手把手教学文档，让初学者既能跑起来一个完整项目，又能逐章理解每一个语法 / 框架知识点。

## 项目特点

- 既包含**后端单体**精读路线（保留原 `docs/从0到1实操指南.md`），也包含**前后端全栈**教学路线（`docs/教学/` 12 章）。
- 后端故意**不使用 Lombok**，让你看到完整的 Java 类、字段、构造方法、Getter/Setter、泛型、集合、日期时间、BigDecimal、枚举、异常等基础语法。
- 前端使用大厂主流的 **Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router + Axios** 组合。
- 一行命令跑起来：后端用 H2 内嵌数据库免装 MySQL；前端用 Vite 开发代理免配 CORS（但项目同时已经开了后端 CORS）。

## 技术栈

### 后端

- Java 11 / Spring Boot 2.7 / Spring MVC
- Bean Validation（`@Valid` / `@NotBlank` / `@Pattern` 等参数校验）
- MyBatis（手写 SQL 风格的 ORM）
- H2 内嵌数据库（默认）/ MySQL（profile 切换）
- 统一 `ApiResponse<T>` 响应、统一异常处理、全局 CORS
- Maven 构建

### 前端 `frontend/`

- Vue 3 + TypeScript + 组合式 API
- Vite 构建与开发服务器（含到后端的 `/api` 代理）
- Element Plus UI 组件库（中文 locale）
- Pinia 状态管理（部门数据全局共享）
- Vue Router 4（HTML5 history 模式）
- Axios 实例 + 拦截器（统一解开 `ApiResponse` 包装）
- 不依赖任何后台模板，干净的代码可以照着学

## 业务功能

- **部门管理**：查询部门、新增部门
- **员工管理**：分页查询 / 关键字搜索 / 部门过滤、新增、编辑、详情、离职
- **考勤管理**：选员工后上下班打卡，按日期范围查询考勤记录
- **统一接口返回格式**：`{ code, message, data, timestamp }`
- **统一业务异常**：`BusinessException`
- **全局异常处理**：`GlobalExceptionHandler`

## 项目结构

```text
junior-java-company-demo/
├── pom.xml                  Maven 工程文件（后端）
├── src/                     后端源码（Spring Boot）
├── frontend/                前端工程（Vue3 + TS + Vite）
└── docs/
    ├── api.http             后端接口测试样例
    ├── 从0到1实操指南.md     后端单体精读（前身资料）
    ├── 教学/                 全栈手把手教学 12 章（推荐入口）
    └── 测试/                 全栈端到端测试报告（含视频和截图）
```

教学文档清单（推荐入口）：[docs/教学/00-总览](./docs/%E6%95%99%E5%AD%A6/00-%E6%80%BB%E8%A7%88-%E6%80%8E%E4%B9%88%E8%AF%BB%E8%BF%99%E4%BB%BD%E8%B5%84%E6%96%99.md)

| 序号 | 文档 | 主题 |
| --- | --- | --- |
| 00 | 总览：怎么读这份资料 | 全套文档的阅读路径 |
| 01 | 环境准备与工具安装 | JDK / Maven / Node / pnpm / MySQL / Git / IDE |
| 02 | Java 基础语法回顾 | 类、集合、泛型、注解、异常等 |
| 03 | Spring Boot 框架手把手 | IoC、自动装配、Controller、Service、配置 |
| 04 | MyBatis 与数据库操作 | Mapper、XML、动态 SQL、参数与结果集 |
| 05 | 后端分层架构与统一响应 | DTO/Entity/Service/Controller、统一返回、异常处理 |
| 06 | Vue3 + TypeScript 入门 | 组合式 API、响应式、组件、TS 基础 |
| 07 | Element Plus 组件库 | 表单、表格、对话框、消息提示、布局 |
| 08 | Pinia 与 Axios | 状态管理、HTTP 封装、拦截器 |
| 09 | Vue Router 与前后端联调 | 前端路由、跨域、代理、CORS |
| 10 | Git 与 GitHub 小白教程 | clone/add/commit/push/pull/branch/PR |
| 11 | 从 0 到 1 全栈搭建步骤 | 把整个项目从空文件夹搭起来 |
| 12 | 备份到 E 盘与日常工作流 | 把仓库克隆到 `E:\`、定时备份、协同流程 |

## 跑起来：5 分钟上手

### 准备环境

- JDK 11+
- Maven 3.6+
- Node.js 18+
- pnpm 8+

> 详细安装步骤见 [docs/教学/01-环境准备与工具安装](./docs/%E6%95%99%E5%AD%A6/01-%E7%8E%AF%E5%A2%83%E5%87%86%E5%A4%87%E4%B8%8E%E5%B7%A5%E5%85%B7%E5%AE%89%E8%A3%85.md)

### 启动后端

```bash
# 在项目根目录
mvn spring-boot:run
```

启动后：

- 接口地址：<http://localhost:8080/api>
- H2 控制台：<http://localhost:8080/h2-console>（JDBC URL: `jdbc:h2:mem:company_demo`，用户名 `sa`，密码空）

### 启动前端

```bash
cd frontend
pnpm install
pnpm dev
```

打开 <http://localhost:5173>，即可看到员工管理 / 部门管理 / 考勤打卡三个模块。

### 仅用 curl / api.http 测后端

参考 `docs/api.http`，可在 IntelliJ IDEA 中右键运行；或用 curl：

```bash
curl http://localhost:8080/api/departments
curl "http://localhost:8080/api/employees?pageNum=1&pageSize=10"
curl http://localhost:8080/api/employees/1
```

新增员工示例：

```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "name":"赵六","phone":"13900000006","email":"zhaoliu@example.com",
    "departmentId":1,"position":"Java开发工程师",
    "hireDate":"2026-05-11","salary":12000
  }'
```

## 切换到 MySQL

1. 在 MySQL 中执行 `src/main/resources/schema.sql` 建表
2. 修改 `src/main/resources/application-mysql.yml` 中的账号密码
3. 用 MySQL profile 启动：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## 跑测试

```bash
mvn test                  # 后端单元测试
cd frontend && pnpm build # 前端类型检查 + 构建
```

完整的端到端测试报告（含演示视频和 7 条断言）：[docs/测试/E2E测试报告](./docs/%E6%B5%8B%E8%AF%95/E2E%E6%B5%8B%E8%AF%95%E6%8A%A5%E5%91%8A.md)

## 给完全的新手

如果你是真零基础，强烈建议按 `docs/教学/00 → 12` 顺序读完，每一章后面都有"课后练习"。

## 备份你自己的 fork 到本地 E 盘

参见 [docs/教学/12-备份到 E 盘与日常工作流](./docs/%E6%95%99%E5%AD%A6/12-%E5%A4%87%E4%BB%BD%E5%88%B0E%E7%9B%98%E4%B8%8E%E6%97%A5%E5%B8%B8%E5%B7%A5%E4%BD%9C%E6%B5%81.md)。一行命令：

```powershell
# 在 Windows Git Bash
cd /e
git clone https://github.com/你的用户名/junior-java-company-demo.git
```

之后 `cd /e/junior-java-company-demo && git pull` 即可保持与 GitHub 同步。

## License

MIT
