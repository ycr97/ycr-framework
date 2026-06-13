# MyBatis-Plus 数据访问增强

`ycr-starter-data-mp` 在 MyBatis-Plus 之上提供：实体基类与审计字段自动填充、注解式查询条件构建、统一分页模型。引入即自动配置（分页插件、自动填充处理器）。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-data-mp</artifactId>
</dependency>
```

> 业务库还需自备数据源（`spring.datasource.*`）与数据库驱动（如 `mysql-connector-j`）。

## 配置

前缀 `ycr.data.mp`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.data.mp.auto-fill-enabled` | `true` | 是否启用审计字段自动填充 |
| `ycr.data.mp.pagination-enabled` | `true` | 是否启用分页插件 |
| `ycr.data.mp.max-limit` | `1000` | 单页最大条数上限 |

## 实体基类与自动填充

`BaseDO` 提供四个审计字段，由 `AutoFillMetaObjectHandler` 自动写入，业务实体无需手动赋值：

| 字段 | 插入时 | 更新时 | 来源 |
| --- | --- | --- | --- |
| `createTime` | ✔ | — | `LocalDateTime.now()` |
| `updateTime` | ✔ | ✔ | `LocalDateTime.now()` |
| `createUser` | ✔ | — | `UserContextHolder.getUserId()` |
| `updateUser` | ✔ | ✔ | `UserContextHolder.getUserId()` |

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class UserDO extends BaseDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String username;
}
```

> `createUser`/`updateUser` 取当前线程的用户上下文（见 `ycr-starter-context`）；线程内无上下文时填 `null`。

## Mapper 基类

`BaseMapperX<T>` 继承 MyBatis-Plus `BaseMapper<T>`。标注 `@Mapper` 即由 MyBatis-Plus 自动配置从启动类基包向下扫描注册，**无需 `@MapperScan`**。

```java
@Mapper
public interface UserMapper extends BaseMapperX<UserDO> {
}
```

## 注解式查询条件

查询对象字段用 `@Query` 标注查询方式，`QueryWrapperHelper.build(query)` 反射构造 `QueryWrapper`：

```java
@Data
public class UserQuery {
    @Query(type = QueryType.LIKE)   private String username;
    @Query(type = QueryType.EQ)     private Integer status;
}
```

```java
QueryWrapper<UserDO> wrapper = QueryWrapperHelper.build(query);
```

`null` 或空字符串字段自动跳过；列名默认取字段名的下划线形式，可用 `@Query(column = "...")` 覆盖。`QueryType` 支持：`EQ/NE/GT/GE/LT/LE/LIKE/LIKE_LEFT/LIKE_RIGHT/IN/BETWEEN/IS_NULL`。

## 统一分页

`PageQuery`（入参：`page` 从 1 起、`size`、`sortField`、`sortOrder`）与 `PageResult<T>`（出参：`list`、`total`、`page`、`size`）通过 `MpPageHelper` 与 MyBatis-Plus 互转：

```java
Page<UserDO> page = MpPageHelper.toPage(pageQuery);            // 含排序，排序列名白名单校验防注入
PageResult<UserDO> result = MpPageHelper.toResult(userMapper.selectPage(page, wrapper));
```

## 关联示例

`ycr-scaffold-mvc` Example 的 `UserServiceImpl`（手写分层分页）与 `ycr-starter-crud`（自动端点）均基于本模块。
