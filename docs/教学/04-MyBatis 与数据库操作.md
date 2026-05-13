# 04 MyBatis 与数据库操作

MyBatis 是国内公司用得最多的持久层框架，特点是**写 SQL 你自己来，参数和结果集映射框架帮你**。和 JPA / Hibernate 完全自动生成 SQL 不同，MyBatis 让你完全掌控 SQL，因此在国内"重 SQL"的业务场景里非常受欢迎。

## 1. 三件套：Entity、Mapper 接口、Mapper XML

### Entity（实体）

对应数据库表，字段名一般驼峰，数据库列名下划线。

`Employee.java`：

```java
public class Employee {
    private Long id;
    private String name;
    private Long departmentId;     // 对应数据库 department_id
    // getter/setter ...
}
```

### Mapper 接口

定义"我想做什么操作"，接口方法名 = XML 里的 `<select id="...">` id。

`EmployeeMapper.java`：

```java
public interface EmployeeMapper {

    List<Employee> search(@Param("keyword") String keyword,
                          @Param("departmentId") Long departmentId,
                          @Param("offset") int offset,
                          @Param("pageSize") int pageSize);

    int count(@Param("keyword") String keyword, @Param("departmentId") Long departmentId);

    Employee findById(@Param("id") Long id);

    int insert(Employee employee);

    int update(Employee employee);

    int disable(@Param("id") Long id);
}
```

- `@Param("xxx")` 给参数命名，方便 XML 里用 `#{xxx}` 引用。
- 单个对象参数（如 `Employee employee`）可以不写 `@Param`，XML 里直接用属性名 `#{name}`、`#{phone}`。

### Mapper XML

写真正的 SQL，文件放在 `src/main/resources/mapper/` 下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.companydemo.mapper.EmployeeMapper">

    <resultMap id="EmployeeResultMap" type="com.example.companydemo.entity.Employee">
        <id column="id" property="id"/>
        <result column="department_id" property="departmentId"/>
        ...
    </resultMap>

    <select id="findById" resultMap="EmployeeResultMap">
        SELECT id, name, phone, email, department_id, position, hire_date, salary, status,
               created_at, updated_at
        FROM employee
        WHERE id = #{id}
    </select>
</mapper>
```

要点：

- `namespace` 必须是 Mapper 接口的全限定名。
- `<resultMap>` 把数据库列 → Java 字段映射写一次，多个 `<select>` 复用。
- `<id>` 标记主键列，`<result>` 标记普通列。

> **简化方案**：在 `application.yml` 里开启
> ```yaml
> mybatis:
>   configuration:
>     map-underscore-to-camel-case: true
> ```
> 列名 `department_id` 会自动映射到 `departmentId`，简单查询可以省略 `<resultMap>`，直接 `resultType="com.example.companydemo.entity.Employee"`。

## 2. `#{}` vs `${}`

```sql
WHERE id = #{id}    -- ✅ 预编译，安全
WHERE id = ${id}    -- ❌ 字符串拼接，会有 SQL 注入风险
```

99% 的情况都用 `#{}`。只有"传表名 / 排序方向"这种**不能预编译**的情况才用 `${}`，并且要白名单校验。

## 3. 动态 SQL：if、where、foreach、choose

### `<if>` 条件拼接

```xml
<select id="search" resultMap="EmployeeResultMap">
    SELECT * FROM employee
    <where>
        status = 1
        <if test="keyword != null and keyword != ''">
            AND (name LIKE CONCAT('%', #{keyword}, '%')
              OR phone LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test="departmentId != null">
            AND department_id = #{departmentId}
        </if>
    </where>
    ORDER BY id DESC
    LIMIT #{pageSize} OFFSET #{offset}
</select>
```

- `<where>` 标签会自动处理 `WHERE` 关键字和多余的 `AND/OR`。

### `<foreach>` 拼接 IN

```xml
<select id="findByIds" resultMap="EmployeeResultMap">
    SELECT * FROM employee WHERE id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</select>
```

Java：

```java
List<Employee> findByIds(@Param("ids") List<Long> ids);
```

### `<choose>` 多分支

```xml
<choose>
    <when test="sort == 'salary'">ORDER BY salary DESC</when>
    <when test="sort == 'hireDate'">ORDER BY hire_date DESC</when>
    <otherwise>ORDER BY id DESC</otherwise>
</choose>
```

### `<set>` 动态更新

```xml
<update id="update">
    UPDATE employee
    <set>
        <if test="name != null">name = #{name},</if>
        <if test="salary != null">salary = #{salary},</if>
    </set>
    WHERE id = #{id}
</update>
```

`<set>` 自动去掉末尾多余的逗号。

## 4. 插入与主键回填

```xml
<insert id="insert"
        parameterType="com.example.companydemo.entity.Employee"
        useGeneratedKeys="true"
        keyProperty="id">
    INSERT INTO employee (name, phone, department_id, salary, status)
    VALUES (#{name}, #{phone}, #{departmentId}, #{salary}, #{status})
</insert>
```

`useGeneratedKeys + keyProperty="id"` 表示：执行插入后，把数据库自增的主键回填到 `employee.id` 字段。这样 Service 拿到的 `employee` 对象就有 id 了。

## 5. Mapper 怎么"变成"可注入的 Bean？

启动类上有：

```java
@MapperScan("com.example.companydemo.mapper")
```

它告诉 MyBatis-Spring 去扫描这个包下所有接口，给它们动态生成代理实现类、注册到 Spring 容器。所以你能在 Service 里直接：

```java
@Service
public class EmployeeService {
    private final EmployeeMapper employeeMapper;
    public EmployeeService(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }
}
```

## 6. 事务：`@Transactional`

```java
@Service
public class EmployeeService {

    @Transactional
    public Employee leave(Long id) {
        Employee employee = employeeMapper.findById(id);
        if (employee == null) throw new BusinessException("员工不存在");
        if (employee.getStatus() == 0) throw new BusinessException("员工已离职");
        employeeMapper.disable(id);
        attendanceMapper.disableFuture(id);   // 同事务里的另一个写操作
        return employee;
    }
}
```

要点：

- `@Transactional` 默认遇到 `RuntimeException` 回滚，**遇到受检异常不会回滚**，如有需要写 `@Transactional(rollbackFor = Exception.class)`。
- 同一个事务里多个 Mapper 调用要么全部成功，要么全部回滚。
- **传播行为**（propagation）：默认 `REQUIRED`，有则加入、无则新建。其他常见：`REQUIRES_NEW`（强制新事务）、`NESTED`。
- 注意：`@Transactional` 失效场景——**自身方法内调用** ("`this.方法()`")、**非 public 方法**、**异常被 catch 吞掉**。

## 7. 分页

MyBatis 自身不提供分页，国内常用做法：

1. **手写 LIMIT 分页**（本项目这样写）：自己算 `offset`，再写一条 `count` SQL。
2. **PageHelper**：阿里巴巴 GitHub 上的 pagehelper 插件，自动加 LIMIT。
3. **MyBatis-Plus**：内置 `Page<T>`，更现代，推荐进阶后用。

手写分页代码：

```java
public PageResult<Employee> page(String keyword, Long departmentId, int pageNum, int pageSize) {
    int offset = (pageNum - 1) * pageSize;
    long total = employeeMapper.count(keyword, departmentId);
    List<Employee> list = employeeMapper.search(keyword, departmentId, offset, pageSize);
    return new PageResult<>(total, pageNum, pageSize, list);
}
```

## 8. 看到生成的 SQL

`application.yml` 已经开启：

```yaml
mybatis:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

启动项目后，每条 SQL 都会打印到控制台，方便排错：

```text
==>  Preparing: SELECT id, name, ... FROM employee WHERE id = ?
==> Parameters: 1(Long)
<==      Total: 1
```

## 9. H2 与 MySQL 切换

`application.yml`（默认 H2 内存数据库）：

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:company_demo;MODE=MySQL
    username: sa
    password:
```

H2 启动会自动执行 `schema.sql`（建表）和 `data.sql`（初始化数据）。

切换到真实 MySQL：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

`application-mysql.yml`：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/company_demo?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

JDBC URL 关键参数：

- `useUnicode=true&characterEncoding=utf8`：中文不乱码
- `useSSL=false`：本地开发关 SSL
- `serverTimezone=Asia/Shanghai`：避免 8 小时偏差

## 10. 索引与 SQL 优化（面试小抄）

- **WHERE 字段、ORDER BY 字段建立索引**，避免全表扫描
- **避免 `SELECT *`**，只选需要的列
- **不要在索引列上做函数**，如 `WHERE date(created_at) = '2024-01-01'` 会失效
- **联合索引最左匹配原则**：`(a, b, c)` 上的索引，`WHERE a=? AND b=?` 能命中，`WHERE b=?` 不能
- 用 `EXPLAIN SELECT ...` 看执行计划，type 至少要到 `range/ref`，避免 `ALL`（全表）

## 11. 课后练习

1. 给 `Employee` 加一个字段 `String idCard`（身份证号），同步修改 schema、Entity、DTO、Mapper XML，并加上身份证号正则校验。
2. 写一个 Mapper 方法 `BigDecimal totalSalary(@Param("departmentId") Long departmentId)`，返回该部门所有在职员工的薪资总和（SQL: `SELECT SUM(salary) FROM employee WHERE ...`）。
3. 体会 `@Transactional` 的回滚：在 `leave` 方法里 disable 之后，手动 `throw new RuntimeException()`，重启项目调接口，确认数据库**没有**变化。

下一章：[05-后端分层架构与统一响应](./05-后端分层架构与统一响应.md)
