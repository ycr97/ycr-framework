# YCR Framework 0.9.0-RC2 增量安全审计

## 审计范围

- 审查日期：2026-08-01
- 审查分支：`release/0.9.0-RC2`
- RC1 标签：`v0.9.0-RC1` → `976baa9d23c6d248bf869a64d6a4d673b23bc741`
- RC2 审查基线：`4abb7a3e636c185f4d9b789dbdda56913781fd1a`
- 增量范围：`v0.9.0-RC1..HEAD`
- 当前审查头：`5b1f2a2 fix(auth): redact token values from exception logs`

本次审查覆盖 RC1 之后的测试命名治理、Sa-Token 生产闭环、Redis 认证域隔离、CORS preflight 处理，以及审查期间发现的认证异常日志脱敏修复。未审查或实施 OAuth2 Resource Server；该能力属于本计划 Part C，明确不进入 RC2 发布分支。

## 审计结论

- 未解决 Critical：0
- 未解决 High：0
- RC2 版本修改与标签操作可以进入下一阶段，但必须继续完成 A2/A3 的发布门禁。

## 阻断项及证据

### Redis 认证命名空间碰撞

发现的问题是 Redis 会话模式如果沿用默认登录类型，多个共享 Redis 的应用可能使用相同的 Sa-Token key 命名空间。

已完成的修复与约束：

- `ycr.auth.satoken.auth-domain` 在 Redis 模式下强制配置；
- `auth-domain` 绑定为 `StpLogic.loginType`，使 token、账号会话和 token session key 带有明确认证域；
- 自定义 `StpLogic` 的 loginType 与 `auth-domain` 不一致时启动失败；
- memory/redis 完全由 `ycr.auth.satoken.session-store` 显式选择，不根据类路径隐式切换；
- Redis 或 Redisson 不可用时启动/认证失败，不回退到内存会话；
- 共享 Redis 的无关应用必须使用不同 `auth-domain`；只有确认需要共享登录态的服务才使用相同值。

测试证据：

- `SaTokenAuthAutoConfigurationTest` 验证认证域绑定及 key 中包含认证域；
- `SaTokenSessionStoreAutoConfigurationTest` 验证缺失认证域、认证域匹配和自定义 loginType 不匹配；
- `SaTokenMissingRedisAutoConfigurationTest` 与 `SaTokenRedisSessionStoreAutoConfigurationTest` 验证 Redis 依赖和 RedissonClient 缺失时 fail-closed；
- 真实 Redis `SaTokenRedisIntegrationTest` 验证两节点共享 token 状态/TTL 和 UserContext 序列化恢复：2 tests，0 failures，0 errors，0 skipped；测试使用随机 key 并 finally 清理，未执行 `FLUSHDB`。

### CORS preflight 登录门禁

发现的问题是全局端点登录拦截器会把合法 CORS preflight 当作普通未登录请求拒绝，导致浏览器无法执行实际业务请求。

已完成的修复与约束：

- 仅 `CorsUtils.isPreFlightRequest` 请求绕过登录检查；
- 普通请求仍执行 `StpUtil.checkLogin()`；
- `authenticated` 策略默认保护 `/**`，仅显式 `permit-paths` 匿名放行；
- `SaTokenAuthWebIntegrationTest` 验证合法 Origin 的 preflight 成功，且无 token 的私有业务请求仍返回 401。

### 认证异常日志中的 token 泄露

A1 复审发现 `SaTokenExceptionHandler` 原先记录 `NotLoginException#getMessage()` 和 `SaTokenException#getMessage()`；Sa-Token 的无效 token 异常消息可能包含原始 token。专项回归测试先稳定复现了该泄露，随后修复为只记录异常类型，不记录异常消息。

测试证据：

- `SaTokenExceptionHandlerTest.invalidTokenExceptionLogShouldNotContainRawToken`；
- `SaTokenExceptionHandlerTest.saTokenExceptionLogShouldNotContainRawToken`；
- 两个测试均通过，响应仍保持 401 和通用认证错误语义。

## 验证记录

- `./scripts/check-test-method-names.sh`：通过；
- `./scripts/check-autoconfiguration-tests.sh`：通过；
- `mvn -q clean -pl ycr-starter-auth-satoken -am test`：34 tests，0 failures，0 errors；其中 Redis 集成测试在未设置集成环境变量的该次运行中 skipped 2 项；
- `mvn -q clean test`：138 个 Surefire 报告，共 483 tests，0 failures，0 errors，2 skipped；skipped 项为未设置集成环境变量的 Redis 测试；
- `YCR_REDIS_INTEGRATION_TESTS=true YCR_TEST_REDIS_ADDRESS=redis://127.0.0.1:6379 mvn -q -pl ycr-starter-auth-satoken -am -Dtest=SaTokenRedisIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false test`：2 tests，0 failures，0 errors，0 skipped；
- 外部临时消费者项目 `/private/tmp/ycr-rc2-smoke` 使用 `ycr-framework-bom:0.9.0-RC2` 管理 `ycr-starter-web`、`ycr-starter-auth-satoken` 和 `redisson` 版本，执行内存认证闭环及 Redis `auth-domain` 启动语义：3 tests，0 failures，0 errors，0 skipped；
- `git diff --check`：通过。

以上数字仅记录本次实际执行结果；不得用 RC1 审计中的旧测试数字替代。Redis 命令使用 `-am` 是因为 RC2 依赖尚未 install 时，计划原始的独立模块命令无法解析当前 RC2 reactor 依赖；随后已完成 install。

## 已知 Medium / Low 项

- Medium：B0 验证矩阵中的双实例、不同认证域互斥、Redis 故障和滚动发布场景尚未在本次 A1 中全部执行；它们属于 RC2 验证期的必做验证，不作为已完成声明。
- Low：当前 Redis 集成测试验证 DAO 层的跨节点 token/Session 数据共享，尚未替代真实双应用进程级验证；B0 阶段仍需补充应用形态验证。

本次增量复审未发现其他 Critical / High 问题。

## 生产配置要求

Redis 模式必须显式配置：

```yaml
ycr:
  auth:
    satoken:
      enabled: true
      session-store: redis
      auth-domain: order-api
```

应用必须提供可用的 `RedissonClient`（通常通过 `ycr-starter-cache` 及其 Redis 配置提供）。共享 Redis 的不同应用使用不同 `auth-domain`；有意共享登录态时才配置相同值，并承担兼容性与登出联动责任。禁止依赖 Redis 异常时回退 memory。
