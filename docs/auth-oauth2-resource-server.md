# OAuth2 Resource Server 认证适配器

`ycr-starter-auth-oauth2-resource-server` 是可选的 OAuth2 Resource Server 适配器，用于验证外部身份提供方签发的 Access Token，并将认证结果映射为 YCR `UserContext`。

该模块默认关闭。业务授权仍使用 `ycr-starter-security` 的 YCR 注解与 `PermissionChecker`，不把 Spring Security `GrantedAuthority` 作为第二套业务权限模型。

## 职责边界

模块负责：

- JWT Access Token 签名、issuer、audience、算法和时间声明校验；
- Opaque Token Introspection、active、audience 和可选 issuer 校验；
- Claims 到 `UserContext` 的默认映射与自定义 mapper SPI；
- 将认证结果桥接到 YCR `UserContextHolder`、租户上下文和 MDC；
- YCR `authenticated` / `annotated` 端点策略、`@RequirePermission` 和统一 Bearer 错误响应。

模块明确不负责：

- Authorization Server、Token 签发、JWK 发布或密钥轮换服务；
- OAuth2 Client、登录跳转、SSO 页面、授权确认页；
- Refresh Token、Token 撤销、MFA、用户/角色/租户数据库；
- Opaque Introspection 缓存或重试策略；
- Spring `@PreAuthorize` 业务授权。

完整认证中心应使用 Keycloak、企业现有 IdP 或独立 Spring Authorization Server 项目，不作为 YCR 基础 Starter 的传递依赖。

## 引入与启动

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-auth-oauth2-resource-server</artifactId>
</dependency>
```

最小公共配置如下。`enabled` 默认为 `false`；启用后必须显式指定 `mode`，`jwt` 与 `opaque` 互斥。

```yaml
ycr:
  auth:
    oauth2:
      resource-server:
        enabled: true
        mode: jwt # jwt 或 opaque
        endpoint-policy: authenticated # authenticated 或 annotated
        permit-paths:
          - /login
          - /error
          - /actuator/health
```

OAuth2 Resource Server 与 `ycr.auth.satoken.enabled=true` 不能同时启用。OAuth2 模式也不能使用 `ycr.context.security-mode=GATEWAY_TRUST`；需要同时校验网关签名上下文和 OAuth2 Token 时使用 `MIXED`。

## JWT 模式

```yaml
ycr:
  auth:
    oauth2:
      resource-server:
        enabled: true
        mode: jwt
        endpoint-policy: authenticated
        permit-paths:
          - /error
          - /actuator/health
        jwt:
          issuer-uri: https://idp.example.com/realms/order
          # 可选；不配置时由 issuer-uri 执行 OIDC discovery。
          # 配置后直接从该地址读取 JWKS，但仍校验 issuer。
          jwk-set-uri: https://idp.example.com/realms/order/protocol/openid-connect/certs
          audiences:
            - order-api
          allowed-algorithms:
            - RS256
          clock-skew: 60s
        claims:
          user-id: user_id
          username: preferred_username
          nickname: name
          tenant-id: tenant_id
          dept-id: dept_id
          roles: roles
          permissions: permissions
          scopes: scope
          client-id: client_id
```

安全要求：

- `issuer-uri`、`audiences` 和非空的非对称 `allowed-algorithms` 必填；
- 仅允许显式列出的非对称算法，例如 `RS256`、`ES256` 或 `PS256`；禁止 `none`、`HS*` 和其他 HMAC 算法；
- issuer 精确匹配；Token 的 audience 至少与配置 audience 命中一个，且大小写敏感；
- `exp`、`nbf` 按 Spring Security JWT validator 校验，`clock-skew` 必须为非负时长；
- 生产环境应使用 HTTPS 的 issuer/JWKS 地址，并将密钥和 IdP 配置交给配置中心或密钥管理系统。

`jwk-set-uri` 只改变 JWKS 获取方式，不关闭 issuer 校验。不要用共享 HMAC secret 把 JWT 当作对称签名会话 Token。

## Opaque Token 模式

```yaml
ycr:
  auth:
    oauth2:
      resource-server:
        enabled: true
        mode: opaque
        endpoint-policy: authenticated
        permit-paths:
          - /error
        opaque:
          introspection-uri: https://idp.example.com/oauth2/introspect
          client-id: order-resource-server
          client-secret: ${OAUTH2_INTROSPECTION_CLIENT_SECRET}
          audiences:
            - order-api
          # 可选；配置后精确校验 iss。
          issuer: https://idp.example.com
          connect-timeout: 2s
          read-timeout: 2s
```

默认 introspector 使用 HTTP POST、Basic Auth 和上述两个超时调用 `introspection-uri`。响应必须表示 active Token；随后至少命中一个配置 audience，并在配置 `issuer` 时精确匹配 `iss`。

Introspection 故障不缓存、不重试、不降级为本地会话，最终返回 503。Token inactive、audience 错误或 issuer 错误返回 401。`client-secret` 只能通过环境变量、密钥管理系统或配置中心注入，不能写入日志、异常响应或业务响应。

注册自定义 `OpaqueTokenIntrospector` 后，默认 HTTP introspector 会 back-off。自定义实现必须自行保持 active、audience、issuer 和故障 fail-closed 语义。

## Claims 映射

默认 mapper 将平面 Claims 映射为 `UserContext`。身份字段支持数字或十进制字符串；非法数字会直接认证失败。

| UserContext 字段 | 默认 Claim | 规则 |
| --- | --- | --- |
| `userId` | `user_id` | 数字或十进制字符串 |
| `username` | `preferred_username` | 缺失时回退 `sub` |
| `nickname` | `name` | 可选 |
| `tenantId` | `tenant_id` | 数字或十进制字符串 |
| `deptId` | `dept_id` | 数字或十进制字符串 |
| `roles` | `roles` | 字符串、数组或 Collection；空值去除并去重 |
| `permissions` | `permissions` | 字符串、数组或 Collection；空值去除并去重 |
| `permissions` | `scope` | 空格分隔后合并 |
| `permissions` | `scp` | Collection/数组或字符串后合并 |
| `clientId` | `client_id` | 缺失时回退 `azp` |

至少必须能从 `user_id` 或 `preferred_username`/`sub` 证明身份；否则 Token 被拒绝。认证桥接过滤器会将最终来源标记为 `TOKEN`，不会信任 mapper 返回的来源字段。

### 自定义 mapper

当 IdP 使用嵌套或企业自定义 Claims 时，实现 `OAuth2UserContextMapper` 并注册 Bean：

```java
import com.ycr.framework.auth.oauth2.mapper.OAuth2UserContextMapper;
import com.ycr.framework.context.model.UserContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
class OAuth2ClaimsConfiguration {

    @Bean
    OAuth2UserContextMapper oauth2UserContextMapper() {
        return claims -> {
            UserContext context = new UserContext();
            context.setUserId(Long.valueOf(String.valueOf(claims.get("uid"))));
            context.setUsername((String) claims.get("login"));
            context.setTenantId(Long.valueOf(String.valueOf(claims.get("tenant"))));
            context.setPermissions(Set.of("order:read"));
            return context;
        };
    }
}
```

自定义 mapper 抛出异常或返回 `null` 时请求按认证失败处理，不会把错误转成 500，也不会把底层 Claims 或 Token 回显给客户端。

## 端点策略与业务权限

`endpoint-policy=authenticated` 是默认策略：除 `permit-paths` 外，所有请求都需要有效 Bearer Token。标准 CORS preflight 会放行，但实际业务请求仍执行认证。

```yaml
ycr:
  auth:
    oauth2:
      resource-server:
        endpoint-policy: annotated
        permit-paths:
          - /error
```

`annotated` 只让端点级门禁放行；带 YCR 注解的方法仍由 `AuthorizeAspect` 和 `PermissionChecker` 保护：

```java
@RequirePermission("order:read")
@GetMapping("/api/orders")
public R<List<Order>> listOrders() {
    return R.ok(orderService.list());
}
```

业务代码继续通过 `UserContextHolder`、`SecurityUtils` 和 YCR 注解读取身份与权限，不直接依赖 `JwtAuthenticationToken`、`BearerTokenAuthentication` 或 Spring Security authority。

## MIXED：签名上下文与 OAuth2 Token

需要保留可信网关透传的租户/应用附加上下文时，可使用 `MIXED`：

```yaml
ycr:
  context:
    security-mode: mixed
    header-sign:
      enabled: true
      secret: ${YCR_CONTEXT_SIGN_SECRET}
      ttl: 60s
      reject-invalid: true
  auth:
    oauth2:
      resource-server:
        enabled: true
        mode: jwt
```

签名 Header 必须先通过 HMAC、时间戳和 nonce 防重放校验。随后 OAuth2 Token 与签名上下文必须证明相同 userId，缺少 userId 时必须以相同 username 证明身份；双方同时存在 tenantId 时也必须一致。任一冲突或无法证明同一身份都拒绝请求。不要用裸 `X-User-*` Header 构造 MIXED 测试或生产请求。

## 错误响应

认证适配器不回显 Token、Claims、client secret 或底层 IdP 异常：

| 场景 | HTTP | `WWW-Authenticate` | 响应 |
| --- | ---: | --- | --- |
| 缺少/非法/过期 Token | 401 | `Bearer` | `R.fail(401, "未登录或登录已过期")` |
| 权限不足 | 403 | `Bearer error="insufficient_scope"` | `R.fail(403, "权限不足")` |
| Introspection 服务故障 | 503 | `Bearer error="temporarily_unavailable"` | `R.fail(503, "认证服务暂不可用")` |

响应 `Content-Type` 为 JSON。`@RequirePermission` 产生的 YCR `AuthException` / `ForbiddenException` 也分别返回 401/403。

## 外部 IdP 接入边界

Keycloak、企业 OIDC 或其他 IdP 只需要提供符合资源服务契约的配置：

- JWT：issuer、JWKS、audience、签名算法和稳定的身份/租户/权限 Claims；
- Opaque：introspection endpoint、resource-server client credentials、active/audience/issuer Claims；
- Claims 结构无法匹配默认 mapper 时，提供自定义 `OAuth2UserContextMapper`。

IdP 负责用户登录、授权码、PKCE、SSO、密钥发布、Token 签发和撤销；YCR 资源服务只验证 Access Token 并执行本地业务授权。该模块不创建用户、Client 或 Authorization Server。

## 依赖隔离与排障

Spring Security 仅存在于 `ycr-starter-auth-oauth2-resource-server` 的依赖边界。默认 `ycr-starter-auth-satoken` compile tree 不应出现 Spring Security；OAuth2 模块也不包含 OAuth2 Client 或 Authorization Server。

启用 OAuth2 后如果启动失败，优先检查：

1. `enabled=true` 时是否显式指定了 `mode`；
2. JWT 的 issuer、audience、算法和 OIDC/JWKS 地址是否完整；
3. Opaque 的 endpoint、client credentials、audience 和正超时是否完整；
4. 是否同时启用了 Sa-Token，或错误使用了 `GATEWAY_TRUST`；
5. 自定义 decoder/introspector/mapper 是否注册为正确类型的 Bean。
