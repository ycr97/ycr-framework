# 认证（Sa-Token 集成）

`ycr-starter-auth` 是 Sa-Token 的**薄集成层**，只做两件事：

1. `LoginHelper` —— 把 Sa-Token 登录态与框架 `UserContext` 双向联动；
2. `SaTokenExceptionHandler` —— 把 Sa-Token 的认证/鉴权异常转为统一 `R` 响应。

> **职责边界**：本 starter **不**注册路由拦截器、**不**做强制登录。实际的「拦截 `/**` + 放行白名单 + 注解鉴权」由 `ycr-starter-security` 负责（见下文「鉴权如何生效」）。两者通常一起引入。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-auth</artifactId>
</dependency>
```

传递引入 `sa-token-spring-boot3-starter`、`sa-token-jwt`、`ycr-starter-context`。Sa-Token 自身配置走原生 `sa-token.*` 前缀。

## LoginHelper

封装 `StpUtil`，登录时同步把完整 `UserContext` 写入 Sa 会话（键 `ycr_user_context`）与当前线程：

```java
// 登录：签发 token、写入会话、填充当前线程上下文（userId 必填，否则抛 IllegalArgumentException）
LoginHelper.login(userContext);

boolean logged = LoginHelper.isLogin();
String token   = LoginHelper.getTokenValue();
UserContext c  = LoginHelper.getUserContext();   // 线程内为空但已登录时，从会话懒还原并回填线程
Long uid       = LoginHelper.getUserId();         // 未登录返回 null
String name    = LoginHelper.getUsername();

LoginHelper.logout();          // 注销登录态，并在 finally 中清理线程上下文

// 供 Filter/手动场景：只动线程上下文，不触发 Sa-Token 登录/登出
LoginHelper.setUserContext(userContext);
LoginHelper.clearContext();
```

`getUserContext()` 的懒还原机制专为「未经 `ContextFilter`、直接凭 token 调用」的链路设计——无需手动 set 即可拿到上下文。

## 异常处理

`SaTokenExceptionHandler`（`@RestControllerAdvice` + `@Order(-1)`，优先于业务全局异常处理）将 Sa-Token 异常映射为统一 `R`：

| 异常 | HTTP | 响应 |
| --- | --- | --- |
| `NotLoginException` | 401 | `R.fail(401, "未登录或登录已过期")` |
| `NotPermissionException` | 403 | `R.fail(403, "权限不足")` |
| `NotRoleException` | 403 | `R.fail(403, "权限不足")` |
| 其他 `SaTokenException` | 401 | `R.fail(401, "认证异常")` |

## 鉴权如何生效（配合 ycr-starter-security）

引入 `ycr-starter-security` 后，它在 Servlet Web 下注册 `SaInterceptor`（注解模式）拦截 `/**`，并放行 `ycr.security.exclude-paths`（默认已含 `/doc.html`、`/v3/api-docs/**`、`/actuator/**` 等）。鉴权点以方法/类上的 Sa-Token 原生注解为准，框架不做全局强制登录：

```java
@SaCheckLogin                       // 需登录
@SaCheckRole("admin")               // 需角色
@SaCheckPermission("user:delete")   // 需权限
@GetMapping("/{id}")
public R<UserResp> get(@PathVariable Long id) { ... }
```

放行白名单配置（在 `ycr-starter-security`）：

```yaml
ycr:
  security:
    enabled: true                   # 关掉则不注册拦截器
    exclude-paths:
      - /api/login
      - /api/captcha
```

## 注意

- 当前版本 `ycr.auth.exclude-paths` 属性虽存在，但**未被 auth 的自动配置消费**（不要依赖它放行）；放行请用上面的 `ycr.security.exclude-paths`。
