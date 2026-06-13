# 请求上下文透传

`ycr-starter-context` 提供用户/租户/应用三类上下文 Holder（基于 TransmittableThreadLocal，跨线程池可传递），以及网关后的 Header 还原与请求结束清理。

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
| `ycr.context.trust-headers` | `false` | 是否信任上游（网关）经 HTTP Header 透传的身份并据此还原上下文 |

> **安全提示**：`trust-headers` 默认关闭。仅在确认本服务处于受信任网关之后、外部无法直接伪造请求头时才开启，否则存在身份伪造风险。无论开关如何，请求结束都会清理上下文，避免线程复用串号。

## 上下文 Holder

```java
UserContext ctx = UserContextHolder.get();
Long userId     = UserContextHolder.getUserId();
String username = UserContextHolder.getUsername();
UserContextHolder.set(userContext);
UserContextHolder.clear();
```

`UserContext` 字段：`userId`、`username`、`nickname`、`roles`、`deptId` 等。
另有 `TenantContextHolder` / `AppContextHolder` 与对应模型。

## Header 还原（ContextFilter）

开启 `trust-headers` 后，`ContextFilter` 从受信任上游的 HTTP Header 还原上下文：

| Header | 含义 |
| --- | --- |
| `X-User-Id` / `X-Username` / `X-Roles` / `X-Dept-Id` | 用户上下文 |
| `X-Tenant-Id` / `X-Tenant-Code` | 租户上下文 |
| `X-App-Id` | 应用上下文 |

## 注意

- 跨服务调用时由 `ycr-starter-feign` 的拦截器把当前上下文写入上述 Header 透传给下游。
- 凭 token 直接登录的链路由 `ycr-starter-auth` 的 `LoginHelper` 从会话还原上下文（见 [auth 文档](auth.md)）。
- 审计字段 `createUser/updateUser` 自动填充取 `UserContextHolder.getUserId()`（见 [data-mp 文档](data-mp.md)）。
