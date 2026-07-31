# 0.9.0-RC1 生产安全审查

- 审查日期：2026-07-31
- 审查分支：`integration/enterprise-baseline`
- 审查范围：认证、请求上下文、注解鉴权、Feign 透传、数据权限、操作日志、Trace
- 发布门槛：未解决的 Critical / High 为 0
- 结论：通过，可进入 `0.9.0-RC1` 发布流程

## 已修复发现

| 编号 | 级别 | 攻击面 | 发现 | 修复 |
| --- | --- | --- | --- | --- |
| SEC-01 | High | Context | 签名请求缺少分布式 nonce 防重放，且旧流程在验签前占用 nonce | 增加 Redisson `SET NX + TTL` 原子防重放；先验签再占用 nonce；Redis 缺失或异常时 fail-closed |
| SEC-02 | High | Context / Feign | `nickname`、`tenantCode`、`appId` 被信任但未进入 HMAC 原文 | 入站与 Feign 出站统一将三个字段纳入固定顺序签名，篡改直接拒绝 |
| SEC-03 | High | Security | Spring 容器中的 `PermissionChecker` 获取异常会静默降级到上下文校验 | 仅无 Spring 容器的独立调用允许本地 fallback；容器内异常直接传播，禁止鉴权模式降级 |
| SEC-04 | High | Mixed auth | 混合模式仅比较双方非空 `userId`，用户名身份和租户冲突可能漏检 | 优先比较 `userId`，否则比较 `username`；无法证明同一身份或双方租户冲突时拒绝 |
| SEC-05 | Medium | Context | 时间戳差值使用 `Math.abs`，极端 long 值存在溢出边界 | 使用 `Math.subtractExact`，非法 TTL 和溢出统一视为过期 |
| SEC-06 | Medium | Security docs | `ycr.security.exclude-paths` 从未接入鉴权切面，却默认包含 actuator，可能造成错误安全假设 | 删除无效属性及默认值，文档明确端点放行由网关、Spring Security 或业务过滤器负责 |

## 已确认的安全语义

- `GATEWAY_TRUST` / `MIXED`：身份 Header 必须 HMAC 验签、在 TTL 内且 nonce 首次出现。
- Redis：key 为 `replayKeyPrefix + nonce`，值通过 Redisson `RBucket.setIfAbsent(value, ttl)` 原子写入。
- Redis 不可用：不回退本地内存或 no-op，签名身份请求返回认证失败。
- `TOKEN_VERIFY`：忽略裸身份 Header，不依赖 Redis 防重放组件。
- 敏感权限远程校验异常：返回无权限，不回退上下文权限。
- 数据权限解析失败：中止 SQL；空规则可使用 `DENY` 生成 `1=0`；`Raw` 只作为明确的受信任逃生口。
- Feign 原始 Authorization 透传默认关闭；上下文签名每次调用重新生成 timestamp 和 nonce。
- 日志请求头对 Authorization、Cookie、Set-Cookie 强制脱敏；上下文 Holder 与 MDC 均在 finally 清理。

## 验证证据

- Redis 原子语义：32 并发调用同一 nonce，仅 1 次首次写入，其余均判定重放。
- Redis 异常、Redis 类路径缺失、非法签名、附加字段篡改、极端时间戳均有 fail-closed 回归测试。
- 自动配置门禁校验全部 imports 均有行为测试，并强制 11 个副作用能力默认关闭、显式开启。
- 框架全量测试：43 个 Maven 模块，459 tests，0 failures，0 errors。

## RC 后续加固项

以下均为 Medium / Low，不阻塞 RC：

1. 对入站 `traceId` / `requestId` 增加长度上限和字符白名单，降低日志污染与高基数风险。
2. 为操作日志的 `X-Forwarded-For` 增加可信代理配置，未配置时优先使用连接源地址。
3. 对 `gateway-trust` / `mixed` 增加启动期配置校验，包括签名密钥最小熵和 Redis 可用性健康检查。
4. 在 CI 增加真实 Redis 容器集成测试，覆盖 Redisson 序列化、TTL 到期和多客户端竞争。

## 生产启用要求

使用 `gateway-trust` 或 `mixed` 时必须同时满足：

- 配置独立的高熵 `ycr.context.header-sign.secret`；
- 容器中存在可用 `RedissonClient`；
- 外部流量不能绕过网关直接访问内部服务；
- 网关与服务使用相同签名字段顺序和规范化规则；
- Redis 告警、连接池、超时和容量已纳入生产监控。
