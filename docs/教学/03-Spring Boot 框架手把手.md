# 03 Spring Boot 框架手把手

## 1. Spring Boot 解决了什么问题

在 Spring Boot 出现之前，写一个 Web 项目要做这些事：

1. 自己装 Tomcat、配置 `web.xml`
2. 自己写 `applicationContext.xml`，声明每一个 Bean
3. 一堆 jar 包之间版本冲突，反复调试

Spring Boot 把这些自动化了：

- **内嵌 Tomcat**：`main` 方法启动就能跑 Web 服务
- **自动装配（Auto-Configuration）**：classpath 上有什么 jar，它就自动配什么
- **Starter 依赖**：一个 `spring-boot-starter-web` 把所有 Web 相关依赖打包
- **约定大于配置**：默认配置足够好，不需要写一堆 XML

一句话：**Spring Boot = Spring + 自动化配置 + 内嵌容器**。

## 2. 启动类

`CompanyDemoApplication.java`：

```java
@MapperScan("com.example.companydemo.mapper")
@SpringBootApplication
public class CompanyDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompanyDemoApplication.class, args);
    }
}
```

要点：

- `@SpringBootApplication` 是组合注解，等价于：
  - `@Configuration`：声明这是一个配置类
  - `@EnableAutoConfiguration`：启用自动装配
  - `@ComponentScan`：扫描当前包及子包下的 `@Component / @Service / @Controller` 等
- `SpringApplication.run(...)`：启动 Spring 容器、加载配置、启动内嵌 Tomcat。
- **包路径建议**：把启动类放在最外层包（`com.example.companydemo`），其他子包（`controller`、`service`、`mapper`）放在它下面，这样默认扫描能覆盖全部业务代码。

## 3. IoC 与 DI：Spring 的核心思想

### IoC（Inversion of Control，控制反转）

传统写法：

```java
EmployeeService service = new EmployeeService(new EmployeeMapper(), new DepartmentMapper());
```

谁用谁创建，依赖关系硬编码在调用方。

Spring 写法：

```java
@Service
public class EmployeeService {
    private final EmployeeMapper employeeMapper;
    public EmployeeService(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }
}
```

`new` 这件事**反过来**让 Spring 容器干。我们只声明"我需要谁"，容器负责把对象造好、装配好、交给我们。

### DI（Dependency Injection，依赖注入）

三种注入方式：

```java
// 1. 构造方法注入（推荐 ✅）
public EmployeeService(EmployeeMapper mapper) {
    this.mapper = mapper;
}

// 2. Setter 注入
@Autowired
public void setMapper(EmployeeMapper mapper) {
    this.mapper = mapper;
}

// 3. 字段注入（不推荐 ❌，不利于单测、不能加 final）
@Autowired
private EmployeeMapper mapper;
```

**面试常问**：为什么推荐构造方法注入？

1. 字段可以加 `final`，对象创建后不可变，线程安全。
2. 单元测试时可以手动 `new EmployeeService(mockMapper)`，不依赖 Spring 容器。
3. 依赖关系一眼看明白，写在构造方法签名里。

## 4. 注解作用速查表

| 注解 | 用在 | 作用 |
| --- | --- | --- |
| `@Component` | 类 | 通用组件，Spring 自动 new 实例 |
| `@Service` | 类 | 业务层组件，本质同 `@Component` |
| `@Repository` | 类 | 持久层组件，会自动转译 SQL 异常 |
| `@Controller` | 类 | 返回视图的 Web 控制器（传统 MVC） |
| `@RestController` | 类 | 返回 JSON 的 Web 控制器（`= @Controller + @ResponseBody`） |
| `@Configuration` | 类 | 配置类，方法上的 `@Bean` 会被注册 |
| `@Bean` | 方法 | 把方法返回值注册为 Bean |
| `@Autowired` | 字段/方法/构造参数 | 自动注入依赖 |
| `@Value("${k}")` | 字段/参数 | 注入配置文件里的值 |
| `@RequestMapping` | 类/方法 | 映射 URL，需指定 method |
| `@GetMapping` 等 | 方法 | 指定 HTTP 方法的快捷写法 |
| `@PathVariable` | 参数 | 取 URL 路径里的变量 |
| `@RequestParam` | 参数 | 取 URL 查询参数 |
| `@RequestBody` | 参数 | 取请求体 JSON，反序列化成对象 |
| `@Valid` | 参数 | 触发 Bean Validation 校验 |

## 5. Controller 详解

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ApiResponse<PageResult<Employee>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int pageSize) {
        return ApiResponse.success(employeeService.page(keyword, departmentId, pageNum, pageSize));
    }
}
```

要点：

- 类上 `@RequestMapping("/api/employees")`：所有方法的 URL 都以此为前缀。
- 方法上 `@GetMapping`：等价于 `@RequestMapping(method = RequestMethod.GET)`。
- `@RequestParam` 可以指定 `required`、`defaultValue`，省去手工判空。
- `@Min(1) @Max(100)` 是参数级校验，类上要加 `@Validated` 才生效。

### 常见请求方式

| HTTP | 含义 | Spring 注解 |
| --- | --- | --- |
| GET | 查询 | `@GetMapping` |
| POST | 新增 / 提交表单 | `@PostMapping` |
| PUT | 整体更新 | `@PutMapping` |
| PATCH | 部分更新 | `@PatchMapping` |
| DELETE | 删除 | `@DeleteMapping` |

### URL 路径变量 vs 查询参数

```text
GET /api/employees/123            ← /{id} 是路径变量
GET /api/employees?pageNum=1      ← 查询参数
```

```java
@GetMapping("/{id}")
public ApiResponse<Employee> detail(@PathVariable Long id) { ... }
```

## 6. 配置文件 application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: junior-java-company-demo
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:company_demo;MODE=MySQL
    username: sa
    password:
```

要点：

- YAML 用缩进表示层级，**只能用空格，不能用 Tab**。
- 同名 key 用 `.` 拼起来就是它的"全路径"，比如 `server.port = 8080`。
- 多 profile：写一份 `application-mysql.yml`，用 `--spring.profiles.active=mysql` 启动就能切换。

### 自定义读取配置

```yaml
biz:
  late-threshold: 09:30:00
```

```java
@Value("${biz.late-threshold}")
private LocalTime lateThreshold;
```

或者用 `@ConfigurationProperties`（推荐，可以读一整个对象）：

```java
@Component
@ConfigurationProperties(prefix = "biz")
public class BizProps {
    private LocalTime lateThreshold;
    // getter/setter
}
```

## 7. Service 层

```java
@Service
public class EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;

    public EmployeeService(EmployeeMapper employeeMapper, DepartmentMapper departmentMapper) {
        this.employeeMapper = employeeMapper;
        this.departmentMapper = departmentMapper;
    }

    public PageResult<Employee> page(String keyword, Long departmentId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        long total = employeeMapper.count(keyword, departmentId);
        List<Employee> list = employeeMapper.selectPage(keyword, departmentId, offset, pageSize);
        return new PageResult<>(total, pageNum, pageSize, list);
    }
}
```

约定：

- Service 写**业务逻辑**，例如校验"部门存不存在"、"是否已离职"、"什么样的薪资合法"。
- Service 不直接处理 HTTP，也不直接拼 SQL；它**调用 Mapper、组装数据、抛业务异常**。
- 大公司常按业务能力切 Service：`EmployeeService` / `AttendanceService` / `SalaryService`，每个负责一块。

## 8. 异常与统一返回

为什么要统一？前端最怕"接口有时返回 200，有时返回 500，有时返回纯文本"。统一格式：

```json
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "timestamp": "2026-05-13T10:00:00"
}
```

实现：

```java
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

全局异常处理：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBiz(BusinessException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ApiResponse.error(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleAll(Exception e) {
        log.error("未预期异常", e);
        return ApiResponse.error(500, "系统繁忙，请稍后再试");
    }
}
```

`@RestControllerAdvice` = `@ControllerAdvice + @ResponseBody`，专门拦截 Controller 抛出的异常。

## 9. 参数校验

DTO：

```java
public class EmployeeCreateRequest {
    @NotBlank(message = "姓名不能为空")
    @Size(max = 40, message = "姓名最多 40 个字符")
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
```

Controller：

```java
@PostMapping
public ApiResponse<Employee> create(@Valid @RequestBody EmployeeCreateRequest request) { ... }
```

常用校验注解：

| 注解 | 用途 |
| --- | --- |
| `@NotNull` | 不能 null |
| `@NotBlank` | 不能 null 也不能空字符串（String 专用） |
| `@NotEmpty` | 不能 null 也不能空（List / Map / String） |
| `@Size(min, max)` | 长度范围 |
| `@Min` / `@Max` | 数值范围 |
| `@DecimalMin` / `@DecimalMax` | BigDecimal 范围 |
| `@Pattern(regexp = "...")` | 正则 |
| `@Email` | 邮箱格式 |
| `@Past` / `@Future` | 日期 |

## 10. CORS 跨域

前端跑在 `http://localhost:5173`，后端跑在 `http://localhost:8080`，端口不同浏览器认为是**跨域**，会拒绝请求。两种解法：

1. **后端开 CORS**（本项目用这种）：见 `common/CorsConfig.java`。
2. **前端配代理**：Vite 把 `/api` 请求转发到后端（见 `frontend/vite.config.ts`）。

实际上**两种一起用**最稳：开发用 Vite 代理，生产用后端 CORS。

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

## 11. 启动、调试、日志

### 启动方式

```bash
# 1. IDEA：右键 CompanyDemoApplication → Run
# 2. 命令行
mvn spring-boot:run

# 3. 打成 jar 后启动
mvn clean package -DskipTests
java -jar target/junior-java-company-demo-0.0.1-SNAPSHOT.jar
```

### 配置日志级别

```yaml
logging:
  level:
    root: INFO
    com.example.companydemo: DEBUG
    org.mybatis: DEBUG          # 看 MyBatis 生成的 SQL
```

### 用 Logger

```java
private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

log.info("创建员工：name={}", request.getName());
log.warn("部门不存在：id={}", id);
log.error("未预期异常", e);
```

`{}` 是占位符，比字符串拼接性能好。

## 12. 课后练习

1. 在 `CompanyDemoApplication` 同级建一个 `HealthController`，提供 `GET /api/health` 返回 `pong`。
2. 给 `EmployeeService.create` 加一个业务校验：手机号在系统里已存在则抛 `BusinessException("手机号已存在")`。
3. 写一个 `@ConfigurationProperties` 类，读 `biz.allow-overtime-minutes: 60`，并在考勤逻辑里使用。
4. 试着把 `CorsConfig.allowedOriginPatterns` 改成只允许 `http://localhost:5173`，重启后用浏览器访问其他端口，看是否被拦截。

下一章：[04-MyBatis 与数据库操作](./04-MyBatis%20与数据库操作.md)
