# OAuth2 Resource Server P1 安全审查

## 审查范围

- 审查日期：2026-08-01
- 基线：`main@5371acd` 加本次修复工作区
- 目标版本：`0.9.0-RC3-SNAPSHOT`
- 模块：`ycr-starter-auth-oauth2-resource-server`、Context/Security 依赖边界及发布门禁

## 结论

- 未解决 Critical：0
- 未解决 High：0
- JWT、Opaque Token、MIXED、上下文桥接、统一错误响应和依赖隔离满足 P1 安全不变量。
- 自定义 `JwtDecoder`、`OpaqueTokenIntrospector`、同名 `SecurityFilterChain` 属于完整安全边界接管，约束已写入接入文档。

## 已关闭问题

### MIXED 裸 Header 权限提升

OAuth2 与 `MIXED` 同时启用时，启动门禁强制 `ycr.context.header-sign.enabled=true` 且
`ycr.context.header-sign.secret` 非空。Header 必须通过 HMAC、时间戳和 nonce 防重放后，才允许与
OAuth2 Token 做身份与租户一致性校验。

### 自定义 mapper 空身份绕过登录门禁

OAuth2 上下文桥接器不再只检查 mapper 返回值非空，同时强制 `userId` 或有效 `username`
至少存在一个；无效上下文统一返回 401 并清理 Holder/MDC。

### RC2 制品坐标复用

四个版本源已推进到 `0.9.0-RC3-SNAPSHOT`，`v0.9.0-RC2` 保持在原提交不移动。
新增版本一致性和已发布 tag 不可变性脚本，阻止 tag 后源码继续复用非 SNAPSHOT 坐标。

### 自动配置与扩展边界

- YCR Bearer EntryPoint/AccessDeniedHandler 使用固定 Bean 名称和限定注入，无关业务 Bean 不再静默替换响应语义。
- JWT 算法在启动阶段验证为 Spring Security 支持的非对称算法，并统一规范化大小写。
- `trust-headers` 兼容判断改为基于 `effectiveSecurityMode()`，显式 MIXED 不再被误判。
- 自定义 decoder、安全链的完整接管责任及 `securityMatcher` 约束已文档化。

## 验证记录

```text
mvn -q clean test
469 tests, 0 failures, 0 errors, 2 skipped（默认跳过真实 Redis）

OAuth2 模块
60 tests, 0 failures, 0 errors, 0 skipped

SaTokenRedisIntegrationTest @ 127.0.0.1:6379
2 tests, 0 failures, 0 errors, 0 skipped

./scripts/check-version-consistency.sh
通过

./scripts/check-test-method-names.sh
通过

./scripts/check-autoconfiguration-tests.sh
通过；包含依赖隔离、自动配置行为和配置元数据检查

git diff --check
通过
```

## 发布约束

当前为开发快照，不创建或移动 RC2 tag，不发布为 RC2 制品。进入 RC3 发布前必须在候选提交上重新执行
上述门禁、外部消费 smoke test 和生产安全复审。

历史 feature 分支中已存在与 RC1/RC2 主线 patch-equivalent 的重复提交。为避免重写已推送的 main，
本次不 rebase、不 force-push；后续功能分支必须从当前 main 创建，并通过常规 PR/merge 或 fast-forward
集成。
