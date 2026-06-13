# 数据权限（行级过滤）

`ycr-starter-data-permission` 基于 MyBatis-Plus 的 `DataPermissionInterceptor`，按规则在 SQL 上自动追加行级过滤条件（如只看本部门数据）。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-data-permission</artifactId>
</dependency>
```

> 本模块传递依赖 `ycr-starter-data-mp`：它产出的过滤逻辑注册为 MyBatis-Plus `InnerInterceptor`，
> 由 data-mp 的 `MybatisPlusInterceptor` 自动收集并**织入分页拦截器之前**，在查询执行前完成行级 SQL 改写。

## 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.data.permission.enabled` | `true` | 是否启用数据权限（拦截器 + 注解切面） |

> 默认开启取安全侧，避免漏配导致越权查询；设为 `false` 整体关闭。

## 定义规则

实现 `DataPermissionRule` 并注册为 Bean，框架自动收集到规则注册表：

```java
@Component
public class DeptDataPermissionRule implements DataPermissionRule {

    @Override
    public String getTableName() {
        return "sys_order";                 // 适用的表
    }

    @Override
    public String getSqlSegment() {
        Long deptId = UserContextHolder.get().getDeptId();
        return "dept_id = " + deptId;       // 追加的过滤片段
    }

    @Override
    public boolean isApplicable() {
        return UserContextHolder.get() != null;   // 何时生效（如超管可返回 false 不过滤）
    }
}
```

## 注解控制

数据权限默认对所有查询生效，用注解局部调整：

| 注解 | 作用 | 目标 |
| --- | --- | --- |
| `@DataPermissionIgnore` | 跳过数据权限过滤（管理员查询、定时任务、内部 RPC 等） | 方法 / 类 |
| `@DataPermission` | 在被 `@DataPermissionIgnore` 标注的类中，对个别方法强制重新启用 | 方法 / 类 |

方法级优先于类级。

## 注意

- 过滤 SQL 片段由规则自行拼装，注入用户/部门等动态值时务必确保来源可信（取自服务端上下文而非请求参数），避免 SQL 注入。
- 规则按表名匹配；同一表命中第一条 `isApplicable()` 为真的规则。
