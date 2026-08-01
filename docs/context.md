# 请求上下文透传

`ycr-starter-context` 提供用户/租户/应用三类上下文 Holder（基于 TransmittableThreadLocal，跨线程池可传递），以及同步 HTTP 请求中的上下文解析、签名 Header 校验与请求结束清理。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-context</artifactId>
</dependency>
```

## 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.context.security-mode` | `token-verify` | 安全模式：`gateway-trust` / `token-verify` / `mixed` |
| `ycr.context.header-sign.enabled` | `true` | 是否启用上下文 Header 签名校验 |
| `ycr.context.header-sign.secret` | 无 | HmacSHA256 签名密钥，启用签名时必填 |
| `ycr.context.header-sign.ttl` | `60s` | 签名时间戳有效期 |
| `ycr.context.header-sign.reject-invalid` | `true` | 签名缺失、过期、错误时是否直接拒绝 |
| `ycr.context.header-sign.replay-key-prefix` | `ycr:context:replay:` | Redis nonce 键前缀 |
| `ycr.context.trust-headers` | `false` | 旧配置，已废弃；开启时兼容映射为 `gateway-trust` |

推荐配置：

```yaml
ycr:
  context:
    security-mode: gateway-trust
    header-sign:
      enabled: true
      secret: ${YCR_CONTEXT_SIGN_SECRET}
      ttl: 60s
```

安全模式：

- `gateway-trust`：只接受带签名、未过期且 nonce 未使用的上下文头；裸 `X-User-*` 不可信。
- `token-verify`：忽略身份 Header，只通过 token resolver 还原上下文，适合单体和边界服务。
- `mixed`：优先签名上下文头，缺失时 fallback token；二者身份冲突时拒绝。

## 上下文 Holder

```java
UserContext ctx = UserContextHolder.get();
Long userId     = UserContextHolder.getUserId();
String username = UserContextHolder.getUsername();
UserContextHolder.set(userContext);
UserContextHolder.clear();
```

`UserContext` 字段：`userId`、`username`、`nickname`、`tenantId`、`deptId`、`roles`、`permissions`、`clientId`、`source`。
另有 `TenantContextHolder` / `AppContextHolder` 与对应模型。

## Header 与签名

`ContextFilter` 通过 `UserContextResolverChain` 还原上下文。签名上下文头包含：

| Header | 含义 |
| --- | --- |
| `X-User-Id` / `X-Username` / `X-Nickname` / `X-Roles` / `X-Permissions` / `X-Dept-Id` | 用户上下文 |
| `X-Tenant-Id` / `X-Tenant-Code` | 租户上下文 |
| `X-App-Id` / `X-Client-Id` / `X-User-Source` | 应用与来源 |
| `X-Trace-Id` | 链路 ID |
| `X-Context-Timestamp` / `X-Context-Nonce` / `X-Context-Signature` | 签名字段 |

参与签名的字段顺序固定：`method`、`path`、`timestamp`、`nonce`、`userId`、`username`、`nickname`、`tenantId`、`tenantCode`、`deptId`、`roles`、`permissions`、`clientId`、`appId`、`traceId`。`roles/permissions` 使用逗号分隔字符串。

存在 `RedissonClient` 时自动使用 Redis `SET NX + TTL` 原子防重放。未提供 Redis、Redis 异常或 TTL 非法时，签名身份请求 fail-closed；`token-verify` 模式不依赖 Redis。

## 扩展点

`UserContextResolver` 是身份来源 SPI。内置解析器：

- `SignedHeaderUserContextResolver`：解析并校验签名上下文头。

Context 不注册空的 token、manual 或 system 占位实现。具体认证适配器或业务扩展直接注册真实的 `UserContextResolver` Bean；`ycr-starter-auth-satoken` 提供 Sa-Token 实现。

## 注意

- 跨服务调用时由 `ycr-starter-feign` 重新签名上下文 Header 透传给下游。
- 凭 token 直接访问的链路由 `ycr-starter-auth-satoken` 从会话还原上下文（见 [auth 文档](auth.md)）。
- 审计字段 `createUser/updateUser` 自动填充取 `UserContextHolder.getUserId()`（见 [data-mp 文档](data-mp.md)）。
