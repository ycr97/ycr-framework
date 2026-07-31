# 数据权限（行级过滤）

`ycr-starter-data-permission` 基于 MyBatis-Plus 的 `DataPermissionInterceptor`，按规则在 SQL 上自动追加行级过滤条件（如只看本部门数据）。**改写覆盖 SELECT / UPDATE / DELETE**，框架结构化构造条件并自动转义，**默认 fail-closed**（受治理表漏配/无授权一律 `1=0`，不放行）。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-data-permission</artifactId>
</dependency>
```

> 本模块传递依赖 `ycr-starter-data-mp`：它产出的过滤逻辑注册为 MyBatis-Plus `InnerInterceptor`，
> 由 data-mp 的 `MybatisPlusInterceptor` 自动收集并**织入分页拦截器之前**，在执行前完成行级 SQL 改写。

## 配置

前缀 `ycr.data.permission`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.data.permission.enabled` | `false` | 是否启用数据权限（拦截器 + 注解切面），须显式开启 |
| `ycr.data.permission.log-applied-conditions` | `false` | 是否输出每张表实际追加的权限条件（含 traceId）的 debug 日志，排障用 |

> 引入 starter 不会修改 SQL。生产应用完成规则配置和验证后，再显式设置为 `true`。

## 工作原理

两个 SPI 分工，框架居中合并：

```
DataScopeResolver（L2 实现：当前主体 → 各维度可见值 DataScope）
        │  每请求解析一次，结果缓存于 DataScopeContext（TTL），请求结束由 filter 清理
        ▼
DataPermissionRule（L1/L2：某表 + 按 DataScope 产出 Predicate）
        │
        ▼
DataPermissionHandler（合并：同表多规则 AND；全 Skip→fail-closed 1=0）
        ▼
MyBatis-Plus DataPermissionInterceptor（SELECT/UPDATE/DELETE 改写）
```

- **`DataScopeResolver`**：取数（谁能看哪些维度的哪些值），通常由公司级 common（L2）实现，读 `UserContext`。**每请求只解析一次**，缓存在 `DataScopeContext`（TransmittableThreadLocal），请求结束由 `DataScopeClearFilter` 清理（仅 Servlet 应用装配）。
- **`DataPermissionRule`**：决策（某张表该用哪个维度、拼成什么列条件）。消费已解析的 `DataScope`，产出 `Predicate`。
- 缺省 `DataScopeResolver` 返回**空范围** → 受治理表一律 fail-closed，**生产必须由 L2 覆盖**。

## 实现 DataScopeResolver（取数，L2）

```java
@Component
public class UserDataScopeResolver implements DataScopeResolver {

    @Override
    public DataScope resolve() {
        UserContext user = UserContextHolder.get();
        // 匿名/未登录：返回「适用且空」的维度（→ Deny 1=0），而非缺键，避免 fail-open
        if (user == null) {
            return DataScope.builder().dimension("dept", List.of()).build();
        }
        // 维度名 → 当前主体可见值集合
        return DataScope.builder()
                .dimension("dept", user.getVisibleDeptIds())
                .build();
    }
}
```

语义约定：
- **匿名/无登录** → 返回「适用且空」的维度（值为空集合），落到 `Deny`，**不要返回缺键**（缺键=不适用=放行，会 fail-open）。
- **解析抛异常**（如远程取数失败）→ 框架视为系统级失败，包装为 `DataPermissionException` **fail-loud 中止本次查询**，不静默放行。

## 定义规则 DataPermissionRule（决策）

实现接口并注册为 Bean，框架自动收集：

```java
@Component
public class OrderDeptRule implements DataPermissionRule {

    @Override
    public String table() {
        return "sys_order";                       // 作用表（裸名，匹配不区分大小写）
    }

    @Override
    public Predicate predicate(DataScope scope) {
        if (!scope.has("dept")) {
            return Predicate.skip();               // 维度不适用 → 本规则不加约束
        }
        Collection<?> deptIds = scope.values("dept");
        if (deptIds.isEmpty()) {
            return Predicate.deny();               // 适用但无授权 → 1=0
        }
        return Predicate.in("dept_id", deptIds);   // 结构化条件，框架转义构造
    }
}
```

### Predicate（谓词）

| 工厂 / 类型 | 渲染 | 用途 |
| --- | --- | --- |
| `Predicate.in(column, values)` | `col IN (...)` | 结构化、框架转义（字符串自动加引号并转义 `'`） |
| `Predicate.eq(column, value)` | `col = ?` | 结构化单值 |
| `new Predicate.Raw(sql)` | 原样解析 | **逃生口**：仅在结构化表达不了时用，**禁止内插用户输入** |
| `Predicate.skip()` | 不加约束 | 本规则本次不参与 |
| `Predicate.deny()` | `1 = 0` | 适用但无授权，拒绝 |

### DataScope 语义（缺键 vs 空值）

| 情形 | `has()` | `values()` | 规则应返回 |
| --- | --- | --- | --- |
| 维度**缺键** | `false` | 空集合 | `skip()`（不适用） |
| 维度存在但**值为空** | `true` | 空集合 | `deny()`（适用但无授权 → 1=0） |
| 维度有值 | `true` | 非空 | `in(...)` / `eq(...)` |

> **空 IN 是规则的责任**：值为空时请显式返回 `deny()`，不要传空集合给 `in(...)`。

### fail-closed 合并语义

- **同表多规则**：各规则产出的 `Predicate` 以 **AND** 合并（取交集，安全侧）。
- **受治理表全 Skip**：某表存在在效规则、但本次全部返回 `Skip` → 框架兜底 **`1=0`**（fail-closed），不放行未约束查询。
- **非受治理表**：无任何规则匹配的表，不改写（正常全量）。

## 写语句过滤（commands）

规则默认对 **SELECT / UPDATE / DELETE** 三类语句生效。如只想约束查询，覆写 `commands()`：

```java
@Override
public Set<SqlCommandType> commands() {
    return EnumSet.of(SqlCommandType.SELECT);   // 仅 SELECT 改写，写语句放行
}
```

## 按 mapper 方法生效（appliesTo）

细粒度控制规则对哪些 mapper 方法生效，默认全生效：

```java
@Override
public boolean appliesTo(String mappedStatementId) {
    // mappedStatementId 形如 com.x.mapper.OrderMapper.selectExport
    return !mappedStatementId.endsWith(".selectExport");   // 导出语句不改写
}
```

## 注解控制

显式开启后，数据权限对匹配规则的查询生效，可用注解局部调整：

| 注解 | 作用 | 目标 |
| --- | --- | --- |
| `@DataPermissionIgnore` | 跳过数据权限过滤（管理员查询、定时任务、内部 RPC 等） | 方法 / 类 |
| `@DataPermission` | 在被 `@DataPermissionIgnore` 标注的类中，对个别方法强制重新启用 | 方法 / 类 |

优先级（高 → 低）：方法 `@DataPermissionIgnore` → 方法 `@DataPermission`（覆盖类级忽略）→ 类 `@DataPermissionIgnore` → 默认生效。

> **超管放行用 `@DataPermissionIgnore`**，不要让规则对超管返回 `Skip`——受治理表全 Skip 会 fail-closed 成 `1=0`。

## 注意

- `Predicate.Raw` 是逃生口：注入用户/部门等动态值务必取自服务端上下文（`UserContext`），**绝不内插请求参数**，避免 SQL 注入。`Column` 由框架转义构造，是首选。
- 默认 fail-closed：缺 `DataScopeResolver`、resolver 返回空范围、受治理表全 Skip，都会落到 `1=0`。这是有意为之——漏配宁可查不到，不可越权。
- `DataScopeResolver` 抛异常是 fail-loud（`DataPermissionException`），不要在 resolver 里吞异常返回空范围，否则会被当成「无授权」而非「系统故障」。
