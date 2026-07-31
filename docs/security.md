# 安全（注解鉴权 + 端点放行）

`ycr-starter-security` 提供 ycr 自有鉴权注解、Controller/Service AOP、`PermissionChecker` SPI 和静态 `SecurityUtils`。业务代码不直接依赖 Sa-Token、Spring Security 或具体认证中心。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-security</artifactId>
</dependency>
```

## 配置

前缀 `ycr.security`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.security.enabled` | `false` | 是否注册 ycr 鉴权切面，须显式开启 |
| `ycr.security.permission.mode` | `context` | 权限校验模式：`context` / `remote` / `mixed` |
| `ycr.security.permission.sensitive-permissions` | 空 | `mixed` 模式下走远程二次校验的敏感权限 |

示例：

```yaml
ycr:
  security:
    enabled: true
    permission:
      mode: mixed
      sensitive-permissions:
        - payment:refund
        - user:grant-role
```

引入 starter 只会提供默认 `PermissionChecker` 等基础 Bean，不会自动启用鉴权切面。

## 鉴权注解

注解可放在 Controller 或 Service 的类/方法上；方法级注解覆盖类级要求。

```java
@RequireLogin
@RequireRole("admin")
@RequireAnyRole({"admin", "manager"})
@RequirePermission("order:create")
@RequireAnyPermission({"order:create", "order:update"})
```

异常语义：

- 未登录 / 登录过期：`AuthException` -> HTTP 401，code `AUTH_UNAUTHORIZED`。
- 无角色 / 无权限：`ForbiddenException` -> HTTP 403，code `AUTH_FORBIDDEN`。

响应体由 `ycr-starter-web` 的全局异常处理器统一输出 `R`。

## PermissionChecker

默认 `ContextPermissionChecker` 从 `UserContext.roles` / `UserContext.permissions` 判断，适合普通接口快速鉴权。

业务可注册 `RemotePermissionChecker` Bean 对接 auth-center/user-center。`mixed` 模式下普通权限走上下文快照，`sensitive-permissions` 走远程二次校验；远程异常默认 fail-closed。

## SecurityUtils

```java
boolean logged = SecurityUtils.isLogin();
Long uid       = SecurityUtils.getUserId();
String token   = SecurityUtils.getTokenValue();

SecurityUtils.hasPermission("user:edit");
SecurityUtils.hasPermissionOr("a", "b");
SecurityUtils.hasPermissionAnd("a", "b");
SecurityUtils.hasRole("admin");
SecurityUtils.hasRoleOr("a", "b");
SecurityUtils.hasRoleAnd("a", "b");
List<String> roles = SecurityUtils.getRoleList();
List<String> perms = SecurityUtils.getPermissionList();
```

适用于注解粒度不够、需在方法体内做条件鉴权的场景。
