# 认证（Sa-Token 集成）

`ycr-starter-auth` 是 Sa-Token 的**薄集成层**，只做三件事：

1. `LoginHelper` —— 把 Sa-Token 登录态与框架 `UserContext` 双向联动；
2. `SaTokenUserContextResolver` —— 在 `token-verify` / `mixed` 模式下从 Sa 会话还原最小 `UserContext`；
3. `SaTokenExceptionHandler` —— 把 Sa-Token 的认证/鉴权异常转为统一 `R` 响应。

> **职责边界**：本 starter 不定义框架级鉴权注解、不让业务层依赖 Sa-Token 注解。接口和服务方法鉴权使用 `ycr-starter-security` 的 `@RequireLogin` / `@RequirePermission` 等 ycr 注解。

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

`getUserContext()` 的懒还原机制专为「直接凭 token 调用」的链路设计。`SaTokenUserContextResolver` 会复用它把 Sa 会话中的最小 `UserContext` 回填到同步请求上下文。

## Token Resolver

`SaTokenUserContextResolver` 实现 `UserContextResolver`：

- 仅支持 `token-verify` / `mixed` 模式。
- 从 Sa 会话键 `ycr_user_context` 读取 `UserContext`。
- 忽略 `X-User-*` 等身份 Header。
- 还原出的上下文默认标记 `source=TOKEN`。

## 异常处理

`SaTokenExceptionHandler`（`@RestControllerAdvice` + `@Order(-1)`，优先于业务全局异常处理）将 Sa-Token 异常映射为统一 `R`：

| 异常 | HTTP | 响应 |
| --- | --- | --- |
| `NotLoginException` | 401 | `R.fail(401, "未登录或登录已过期")` |
| `NotPermissionException` | 403 | `R.fail(403, "权限不足")` |
| `NotRoleException` | 403 | `R.fail(403, "权限不足")` |
| 其他 `SaTokenException` | 401 | `R.fail(401, "认证异常")` |

## 鉴权如何生效（配合 ycr-starter-security）

引入 `ycr-starter-security` 后，使用 ycr 自有注解：

```java
@RequirePermission("user:delete")
@GetMapping("/{id}")
public R<UserResp> get(@PathVariable Long id) { ... }
```

Sa-Token 只负责登录态和 token 解析；权限判断由 `PermissionChecker` 从 `UserContext.roles/permissions` 或远程 SPI 完成。
