# 02 Java 基础语法回顾

本章从项目里能看到的代码反推语法知识。如果你**完全没学过 Java**，建议先看一遍《Java 核心技术》或 B 站尚硅谷 Java 基础，再回头看本章；如果你**学过一点但忘得差不多**，照着本章把项目代码读一遍就够了。

## 1. 包（package）与 import

打开 `src/main/java/com/example/companydemo/entity/Employee.java`：

```java
package com.example.companydemo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Employee {
    // ...
}
```

要点：

- `package` 写在文件最开头，包名必须和目录结构对应。
- `import` 把别的包的类引入进来。同包下的类不用 import。
- `java.lang` 默认 import（`String`、`Integer` 等不用写 import）。
- 同一个 `.java` 文件**只能有一个 public 类**，类名必须和文件名一致。

## 2. 类、字段、构造方法、Getter/Setter

```java
public class Employee {

    private Long id;          // 字段
    private String name;
    // ...

    public Employee() { }     // 无参构造方法

    public Long getId() {     // Getter
        return id;
    }

    public void setId(Long id) { // Setter
        this.id = id;
    }
}
```

要点：

- `private` 表示字段只能在本类访问，外部要通过 Getter/Setter 操作。这是 Java 的"封装"思想。
- 字段默认值：对象引用 `null`，`int=0`、`long=0L`、`double=0.0`、`boolean=false`。
- **`this`** 表示当前对象，避免参数和字段同名时混淆。
- IDEA 用 `Alt+Insert → Getter and Setter` 自动生成，不用手写。
- 没写构造方法时，Java 会自动生成一个无参构造。一旦你写了任何构造方法，编译器就不再自动生成。

> 项目刻意不用 Lombok 的 `@Data`，就是为了让你看到每一个 getter/setter。等你熟练了，正式项目里可以用 Lombok。

## 3. 数据类型与常用类

| Java 类型 | 对应数据库 | 例子 |
| --- | --- | --- |
| `Long` | BIGINT | `private Long id;` |
| `Integer` | INT | `private Integer status;` |
| `String` | VARCHAR / TEXT | `private String name;` |
| `BigDecimal` | DECIMAL（钱、精确小数） | `private BigDecimal salary;` |
| `LocalDate` | DATE | `private LocalDate hireDate;` |
| `LocalDateTime` | TIMESTAMP / DATETIME | `private LocalDateTime createdAt;` |
| `Boolean` | TINYINT(1) | `private Boolean enabled;` |
| `byte[]` | BLOB | 文件二进制 |

要点：

- **金额一定用 `BigDecimal`**，不要用 `double`。`double` 有浮点误差，`0.1 + 0.2 != 0.3`。
- **日期一律用 `java.time` 系列**（`LocalDate` / `LocalDateTime` / `LocalTime`），不要用旧的 `java.util.Date`。
- 数据库 ID 字段建议用 `Long`，对应 BIGINT。

## 4. 方法签名

```java
public BigDecimal totalSalary(int year, int month) {
    // 返回某月的工资总额
}
```

拆解：

- `public`：访问修饰符（`public` / `protected` / 默认 / `private`）
- `BigDecimal`：返回类型，无返回值用 `void`
- `totalSalary`：方法名，驼峰式
- `(int year, int month)`：参数列表
- `{ ... }`：方法体

### 重载（overload） vs 重写（override）

- **重载**：同名方法，参数不同。
- **重写**：子类把父类的方法重新实现，方法签名必须一致。

```java
@Override
public String toString() { ... }    // 重写 Object#toString
```

`@Override` 是注解，告诉编译器"我是在重写父类方法"，写错会编译报错。

## 5. 集合：List / Map / Set

```java
import java.util.List;
import java.util.Map;

List<Employee> employees = employeeMapper.selectAll();
Map<Long, String> deptNames = new HashMap<>();
```

要点：

- `List<E>`：有序、可重复，常用实现 `ArrayList`、`LinkedList`。
- `Map<K, V>`：键值对，常用实现 `HashMap`、`LinkedHashMap`。
- `Set<E>`：无序、不重复，常用 `HashSet`、`TreeSet`。
- 尖括号 `<>` 是**泛型**，把"容器装什么"在编译期写死，避免 ClassCastException。
- 遍历用 `for (Employee e : employees)` 增强 for 循环。

### Stream（重要！中级常用）

```java
List<String> names = employees.stream()
        .filter(e -> e.getStatus() == 1)
        .map(Employee::getName)
        .collect(Collectors.toList());
```

- `stream()`：把集合变成流
- `filter`：过滤
- `map`：转换
- `collect(Collectors.toList())`：收集回 List
- `e -> e.getName()`：Lambda 表达式，简化"一个参数返回一个值"的函数
- `Employee::getName`：方法引用，是 `e -> e.getName()` 的简写

## 6. 枚举（enum）

```java
public enum EmployeeStatus {
    ACTIVE(1, "在职"),
    LEFT(0, "离职");

    private final int code;
    private final String description;

    EmployeeStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }
}
```

要点：

- 枚举常量是"单例对象"，可以带字段、构造方法、方法。
- 国内公司常用枚举表示状态、类型、错误码。
- 枚举的构造方法默认是 `private`。

## 7. 异常处理

```java
public Employee getById(Long id) {
    Employee employee = employeeMapper.selectById(id);
    if (employee == null) {
        throw new BusinessException("员工不存在");
    }
    return employee;
}
```

```java
try {
    int n = Integer.parseInt(text);
} catch (NumberFormatException e) {
    log.warn("解析失败：{}", text, e);
} finally {
    // 一定执行
}
```

要点：

- 异常分两大类：
  - **受检异常**（checked，比如 `IOException`）：必须 `try-catch` 或 `throws`。
  - **非受检异常**（unchecked，继承 `RuntimeException`）：可以不处理，比如 `NullPointerException`。
- 业务异常自定义一个 `BusinessException extends RuntimeException`，统一处理（见后端 `common/BusinessException`）。
- 别 catch 完不打 log 直接 return null，那是"吞异常"，线上排查噩梦。

## 8. 泛型

```java
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
}
```

`T` 是占位符，使用时具体化：

```java
ApiResponse<Employee> resp = ...
ApiResponse<List<Department>> resp2 = ...
```

泛型方法：

```java
public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(0, "success", data);
}
```

要点：

- 泛型让代码"可复用 + 类型安全"，是统一响应、统一分页等通用组件的核心。
- 编译期检查类型，运行时擦除。

## 9. 注解（annotation）

项目里到处是 `@RestController`、`@Service`、`@Valid` 这种注解。注解本身**不直接执行代码**，它只是"挂个标签"，由框架（Spring、MyBatis、JUnit 等）扫描这些标签后做相应的事情。

常见注解类别：

- Spring：`@SpringBootApplication`、`@Configuration`、`@Bean`、`@Component`、`@Service`、`@RestController`、`@Autowired`、`@Value`
- MVC：`@RequestMapping`、`@GetMapping`、`@PostMapping`、`@RequestBody`、`@PathVariable`、`@RequestParam`
- 校验：`@NotNull`、`@NotBlank`、`@Email`、`@Pattern`、`@Min`、`@Max`、`@Size`、`@Valid`
- JPA / MyBatis：`@Mapper`、`@MapperScan`、`@Param`
- 测试：`@Test`、`@SpringBootTest`、`@Mock`

## 10. 访问修饰符 & static & final

| 修饰符 | 同类 | 同包 | 子类 | 任意位置 |
| --- | :---: | :---: | :---: | :---: |
| `private` | √ | × | × | × |
| 默认（无修饰符） | √ | √ | × | × |
| `protected` | √ | √ | √ | × |
| `public` | √ | √ | √ | √ |

- `static`：属于类而不是对象，用 `类名.方法()` 调用。`Collections.sort()` 就是 static 方法。
- `final`：
  - 修饰字段：常量，不可变。
  - 修饰方法：不能被重写。
  - 修饰类：不能被继承（如 `String`）。
- `final` + `private` 组合常出现在 Spring 注入：

```java
private final EmployeeService employeeService;

public EmployeeController(EmployeeService employeeService) {
    this.employeeService = employeeService;
}
```

构造方法注入 + `final` 字段是当前推荐写法（比 `@Autowired` 字段注入更优）。

## 11. 接口（interface） vs 抽象类

```java
public interface EmployeeMapper {
    Employee selectById(@Param("id") Long id);
}
```

- 接口里**默认所有方法都是 `public abstract`**，可以不写。
- Java 8 后支持 `default` 方法（带默认实现）。
- 抽象类用 `abstract class`，可以有字段、构造方法，子类必须继承。

项目里的 Mapper 都是接口，由 MyBatis 在运行时动态生成实现类。

## 12. 字符串操作小抄

```java
String s = "hello";
s.length();             // 5
s.charAt(0);            // 'h'
s.toUpperCase();        // "HELLO"
s.substring(1, 3);      // "el"
s.contains("ll");       // true
s.startsWith("he");     // true
s.split(",");           // 按 , 切
String.join(",", list); // 反向：list 拼成字符串

// 拼接：性能差，循环里别用
String a = "x" + i + "y";

// 拼接：循环里用
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 100; i++) sb.append(i);
String result = sb.toString();

// 格式化
String.format("年龄=%d, 名字=%s", 18, "小明");
```

## 13. 控制流

```java
if (x > 0) { ... } else if (x == 0) { ... } else { ... }

for (int i = 0; i < 10; i++) { ... }
for (Employee e : employees) { ... }       // 增强 for
while (cond) { ... }
do { ... } while (cond);

switch (status) {                          // 老版 switch
    case 1: ... break;
    case 2: ... break;
    default: ...
}

// Java 14+ switch 表达式
String name = switch (status) {
    case 1 -> "在职";
    case 0 -> "离职";
    default -> "未知";
};
```

## 14. 项目里几段典型代码逐行拆解

### 14.1 Service 创建员工

```java
@Service
public class EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;

    public EmployeeService(EmployeeMapper employeeMapper, DepartmentMapper departmentMapper) {
        this.employeeMapper = employeeMapper;
        this.departmentMapper = departmentMapper;
    }

    public Employee create(EmployeeCreateRequest request) {
        Department department = departmentMapper.selectById(request.getDepartmentId());
        if (department == null) {
            throw new BusinessException("部门不存在");
        }
        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setPhone(request.getPhone());
        employee.setEmail(request.getEmail());
        employee.setDepartmentId(request.getDepartmentId());
        employee.setPosition(request.getPosition());
        employee.setHireDate(request.getHireDate());
        employee.setSalary(request.getSalary());
        employee.setStatus(EmployeeStatus.ACTIVE.getCode());
        employeeMapper.insert(employee);
        return employee;
    }
}
```

逐行：

- `@Service` 让 Spring 把这个类当一个"业务组件"，启动时自动 new 一个实例放进容器。
- `private final` 字段 + 构造方法注入：Spring 看到 `EmployeeService` 构造参数是 `EmployeeMapper`，会自动把它对应的实例传进来。
- `request.getXxx()`：从入参 DTO 取字段。
- `new Employee()`：手工创建一个 entity，再把字段一一拷过去（公司里常用 MapStruct、BeanUtils 自动拷贝，初学者先手写）。
- `employeeMapper.insert(employee)`：调 Mapper 把数据写入数据库。MyBatis 会把自增主键回填到 `employee.id`。

### 14.2 Controller 接收 JSON

```java
@PostMapping
public ApiResponse<Employee> create(@Valid @RequestBody EmployeeCreateRequest request) {
    return ApiResponse.success(employeeService.create(request));
}
```

- `@PostMapping`：HTTP POST。
- `@Valid`：触发 DTO 上 `@NotBlank`、`@Pattern` 等校验，失败抛 `MethodArgumentNotValidException`，由 `GlobalExceptionHandler` 统一处理。
- `@RequestBody`：把请求体 JSON 反序列化成 `EmployeeCreateRequest` 对象（依赖 Jackson）。

## 15. 课后练习

1. 自己写一个 `Department` 类，包含 `id`、`code`、`name`、`status` 四个字段，加 Getter/Setter。
2. 写一个 `static` 方法 `Map<Integer, String> codeMap()`，返回 `EmployeeStatus` 的 code → description 映射。
3. 用 Stream 写一段代码：从 `List<Employee>` 中筛出在职且薪资 > 10000 的员工，按薪资倒序排序。
4. 手动抛一个 `BusinessException`，用 try-catch 接住并打印消息。

下一章：[03-Spring Boot 框架手把手](./03-Spring%20Boot%20框架手把手.md)
