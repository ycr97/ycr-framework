# 认证（Sa-Token）

`ycr-starter-auth-satoken` 是默认的轻量认证组合 Starter，提供 Sa-Token 登录会话、请求上下文恢复、YCR 方法鉴权、统一异常响应、端点登录门禁和可显式选择的会话存储。OAuth2 Resource Server 是独立的可选适配器，见 [OAuth2 Resource Server 文档](auth-oauth2-resource-server.md)。

默认依赖路径不包含 Spring Security。业务授权统一使用 `ycr-starter-security` 的注解与 `PermissionChecker`，不把 Sa-Token 权限注解作为第二套官方授权模型。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-auth-satoken</artifactId>
</dependency>
```

该组合 Starter 传递引入 `ycr-starter-security`、`ycr-starter-context` 和 `sa-token-spring-boot3-starter`。旧 artifact `ycr-starter-auth` 已在 1.0 前直接删除，不提供兼容别名。

## 最小配置

```yaml
ycr:
  auth:
    satoken:
      enabled: true
      permit-paths:
        - /login
        - /error
        - /actuator/health
```

Auth 默认关闭；启用后默认采用 `authenticated` 策略，除白名单外的所有 Servlet 端点都必须登录。`/error` 是默认白名单，业务登录、健康检查和开放接口仍应显式列出。

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.auth.satoken.enabled` | `false` | 是否启用 YCR Sa-Token 认证适配器 |
| `ycr.auth.satoken.endpoint-policy` | `authenticated` | `authenticated` 全局登录门禁；`annotated` 仅使用方法注解 |
| `ycr.auth.satoken.permit-paths` | `[/error]` | `authenticated` 策略下允许匿名访问的路径模式 |
| `ycr.auth.satoken.session-store` | `memory` | `memory` 或 `redis`，不根据类路径自动切换 |
| `ycr.auth.satoken.auth-domain` | 空 | 认证域；Redis 模式必填，相同值表示显式共享登录态 |

`annotated` 仅适用于明确希望逐个端点声明认证要求的应用：

```yaml
ycr:
  auth:
    satoken:
      enabled: true
      endpoint-policy: annotated
```

## Token 安全默认值

Starter 以最低属性优先级注入以下默认值，应用配置、环境变量和配置中心均可覆盖：

```yaml
sa-token:
  token-name: Authorization
  token-prefix: Bearer
  is-read-header: true
  is-read-body: false
  is-read-cookie: false
```

请求格式：

```http
Authorization: Bearer <token>
```

默认不从请求体和 Cookie 读取 token，避免 token 进入参数解析、业务对象或访问日志。

## Session Store

本地开发和测试默认使用进程内 DAO：

```yaml
ycr:
  auth:
    satoken:
      enabled: true
      session-store: memory
```

生产、多实例和滚动发布必须显式选择 Redis，并复用 `ycr-starter-cache` 装配的唯一 `RedissonClient`：

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-cache</artifactId>
</dependency>
```

```yaml
ycr:
  auth:
    satoken:
      enabled: true
      session-store: redis
      auth-domain: order-platform

spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

`auth-domain` 会绑定为 Sa-Token 的 `loginType`，用于隔离 Redis 中的 token、账号会话和 token 会话 key。共享同一 Redis 的无关应用必须使用不同值；只有明确需要共享登录态的服务才使用相同值。`redis` 模式缺少 `auth-domain`、Redisson 依赖或 `RedissonClient` Bean 时应用启动失败；Redis 异常不会降级到本地会话。

真实 Redis 集成测试可在本地或 CI Redis Service 上执行：

```bash
YCR_REDIS_INTEGRATION_TESTS=true \
YCR_TEST_REDIS_ADDRESS=redis://127.0.0.1:6379 \
mvn -pl ycr-starter-auth-satoken -Dtest=SaTokenRedisIntegrationTest test
```

需要密码时额外设置 `YCR_TEST_REDIS_PASSWORD`。该测试验证多节点共享、TTL、`UserContext` 序列化恢复与删除；未显式启用时跳过，不会连接开发者 Redis。

## 登录与登出

`SaTokenSessionManager` 只管理会话生命周期，不重复提供身份和权限查询 API：

```java
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final SaTokenSessionManager sessionManager;

    @PostMapping("/login")
    public R<String> login(@RequestBody LoginRequest request) {
        UserContext userContext = authenticate(request);
        String token = sessionManager.login(userContext).getTokenValue();
        return R.ok(token);
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        sessionManager.logout();
        return R.ok();
    }
}
```

需要设备、超时或并发登录参数时使用重载：

```java
SaLoginParameter parameter = new SaLoginParameter()
        .setDeviceType("web")
        .setTimeout(7200);
SaTokenInfo tokenInfo = sessionManager.login(userContext, parameter);
```

当前身份与权限统一通过 `SecurityUtils` 读取：

```java
Long userId = SecurityUtils.getUserId();
boolean loggedIn = SecurityUtils.isLogin();
boolean allowed = SecurityUtils.hasPermission("user:delete");
```

## 请求上下文与方法鉴权

`SaTokenUserContextResolver` 仅支持 `token-verify` / `mixed` 模式，从 `Authorization: Bearer` 中解析原始 token，再从该 token 独立会话的 `ycr_user_context` 恢复 `UserContext`。这可以隔离同一账号的多设备、多租户登录上下文。裸 `X-User-*` Header 不参与 token 认证，恢复后的来源标记为 `TOKEN`。

启用 Auth 会同步启用 YCR 方法鉴权，无需额外设置 `ycr.security.enabled=true`：

```java
@RequirePermission("user:delete")
@DeleteMapping("/{id}")
public R<Void> delete(@PathVariable Long id) {
    return R.ok();
}
```

Sa-Token 负责 token 读取、登录态维护和会话失效；YCR `AuthorizeAspect` 与 `PermissionChecker` 负责业务角色、权限以及 `context` / `remote` / `mixed` 校验策略。

标准 CORS preflight 请求会绕过全局登录门禁，由 Spring MVC CORS 配置处理；实际业务请求仍按端点策略校验登录态。

## 异常响应

`SaTokenExceptionHandler` 将 Sa-Token 异常映射为统一 `R`：

| 异常 | HTTP | 响应 |
| --- | --- | --- |
| `NotLoginException` | 401 | `R.fail(401, "未登录或登录已过期")` |
| `NotPermissionException` | 403 | `R.fail(403, "权限不足")` |
| `NotRoleException` | 403 | `R.fail(403, "权限不足")` |
| 其他 `SaTokenException` | 401 | `R.fail(401, "认证异常")` |

YCR 方法鉴权抛出的 `AuthException` / `ForbiddenException` 由 `ycr-starter-web` 的 `GlobalExceptionHandler` 分别映射为 HTTP 401/403。

## 职责边界

默认 Starter 不包含 JWT 模式、Spring Security、OAuth2 Authorization Server、登录页面、用户表、MFA、短信/扫码登录或 OAuth2 Client 管理。

`ycr-starter-auth-satoken` 与 `ycr-starter-auth-oauth2-resource-server` 是两条互斥的认证适配路径：Sa-Token 负责默认的本地/Redis 可撤销会话；OAuth2 适配器负责外部 IdP Access Token 验证。两者不能同时启用，业务授权仍统一使用 YCR 注解和 `PermissionChecker`。完整认证中心应独立建设，不进入基础 Starter。
