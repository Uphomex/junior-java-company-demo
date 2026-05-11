# 初级 Java 后端学习项目：员工与考勤管理

这是一个适合初级 Java 开发者阅读的 Spring Boot 后端项目，覆盖国内公司常见的 Java 后端基础需求：REST 接口、CRUD、分层架构、参数校验、统一响应、统一异常处理、MyBatis SQL、H2 本地数据库和 MySQL 配置切换。

项目刻意不使用 Lombok，让你能直接看到 Java 类、字段、构造方法、Getter/Setter、泛型、集合、日期时间、BigDecimal、枚举、异常等基础语法。

## 技术栈

- Java 11
- Spring Boot 2.7
- Spring MVC
- Bean Validation
- MyBatis
- H2 内存数据库，启动即用
- MySQL Connector，便于切换真实 MySQL
- Maven

## 业务功能

- 部门管理：查询部门、新增部门
- 员工管理：分页查询、新增、详情、修改、离职
- 考勤管理：上班打卡、下班打卡、按日期范围查询
- 统一接口返回格式：`ApiResponse<T>`
- 统一业务异常：`BusinessException`
- 全局异常处理：`GlobalExceptionHandler`

## 项目结构

```text
src/main/java/com/example/companydemo
  common        通用返回、分页、异常
  controller    Web 接口层
  dto           接收前端请求的参数对象
  entity        数据库实体类
  mapper        MyBatis Mapper 接口
  service       业务逻辑层
  support       枚举、工具类
```

## 启动方式

```bash
cd E:\junior-java-company-demo
mvn spring-boot:run
```

启动后访问：

- 接口地址：`http://localhost:8080/api`
- H2 控制台：`http://localhost:8080/h2-console`
- JDBC URL：`jdbc:h2:mem:company_demo`
- 用户名：`sa`
- 密码：留空

## 常用接口

```bash
curl http://localhost:8080/api/departments
curl "http://localhost:8080/api/employees?pageNum=1&pageSize=10"
curl http://localhost:8080/api/employees/1
```

新增员工：

```bash
curl -X POST http://localhost:8080/api/employees ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"赵六\",\"phone\":\"13900000006\",\"email\":\"zhaoliu@example.com\",\"departmentId\":1,\"position\":\"Java开发工程师\",\"hireDate\":\"2026-05-11\",\"salary\":12000}"
```

上班打卡：

```bash
curl -X POST http://localhost:8080/api/attendance/check-in ^
  -H "Content-Type: application/json" ^
  -d "{\"employeeId\":1,\"note\":\"到公司\"}"
```

下班打卡：

```bash
curl -X POST http://localhost:8080/api/attendance/check-out ^
  -H "Content-Type: application/json" ^
  -d "{\"employeeId\":1,\"note\":\"正常下班\"}"
```

## 切换 MySQL

1. 在 MySQL 中执行 `src/main/resources/schema.sql` 建表。
2. 修改 `src/main/resources/application-mysql.yml` 中的数据库账号密码。
3. 用 MySQL profile 启动：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## 推荐学习顺序

1. 先看 `entity`，理解 Java 类、字段、Getter/Setter 和日期类型。
2. 再看 `mapper` 和 `resources/mapper/*.xml`，理解 SQL 与 Java 方法如何绑定。
3. 然后看 `service`，理解业务规则和异常处理。
4. 最后看 `controller`，理解 HTTP 请求如何进入 Java 后端。
5. 修改 `EmployeeService` 的规则，例如薪资范围、手机号唯一性、离职限制，再重新启动验证。

## 练习题

- 给员工增加身份证号字段，并完成新增、修改、查询。
- 给考勤增加“请假”接口。
- 新增“岗位表”，让员工的岗位从岗位表中选择。
- 把员工分页查询改成按入职日期排序。
- 增加单元测试，测试 `AttendanceService` 的迟到和早退逻辑。

