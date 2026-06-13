# 安全（注解鉴权 + 端点放行）

`ycr-starter-security` 在 Servlet Web 下注册 Sa-Token 注解鉴权拦截器，拦截 `/**` 并放行白名单；同时提供静态 `SecurityUtils`。与 `ycr-starter-auth`（登录态/上下文）配合构成完整认证授权链路。

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
| `ycr.security.enabled` | `true` | 是否注册鉴权拦截器（关掉则不拦截） |
| `ycr.security.exclude-paths` | 见下 | 放行路径（不需认证即可访问） |

默认白名单已含：`/doc.html`、`/swagger-resources/**`、`/webjars/**`、`/v3/api-docs/**`、`/favicon.ico`、`/error`、`/actuator/**`。追加登录/验证码等接口：

```yaml
ycr:
  security:
    exclude-paths:
      - /doc.html
      - /v3/api-docs/**
      - /actuator/**
      - /api/login
      - /api/captcha
```

> 自定义 `exclude-paths` 会**覆盖**默认值，需把仍要放行的默认项一并列出。

## 鉴权方式（注解模式）

拦截器开启注解模式，框架不做全局强制登录——以方法/类上的 Sa-Token 原生注解为准，按需鉴权：

```java
@SaCheckLogin                       // 需登录
@SaCheckRole("admin")               // 需角色
@SaCheckPermission("user:delete")   // 需权限
```

鉴权失败抛出的 Sa-Token 异常由 `ycr-starter-auth` 的 `SaTokenExceptionHandler` 统一转为 401/403 的 `R` 响应（见 [auth 文档](auth.md)）。

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
