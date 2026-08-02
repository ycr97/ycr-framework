# Changelog

## Unreleased - 0.9.0-RC3-SNAPSHOT

### Added

- 独立的 OAuth2 Resource Server Starter，支持 JWT、Opaque Token、Claims 映射与 YCR 权限注解。

### Security

- MIXED 模式强制启用 Header HMAC 签名并配置密钥，阻断裸 Header 权限和租户注入。
- OAuth2 上下文桥接统一拒绝缺少 userId/username 的无效身份。
- YCR Bearer 处理器按固定 Bean 名称隔离，避免无关业务 Bean 改写 401/403/503 语义。
- JWT 算法配置在启动阶段拒绝不受支持的算法。

## 0.9.0-RC2 - 2026-08-01

### Added

- Sa-Token 生产闭环：显式启用、全局端点保护、memory/redis 会话存储、真实 Web/Redis 集成测试。

### Changed

- 认证模块更名为 ycr-starter-auth-satoken。
- 业务授权统一使用 YCR 注解与 PermissionChecker。
- 测试方法名统一为 ASCII lowerCamelCase。

### Security

- Redis 模式强制 auth-domain，并绑定 Sa-Token loginType，隔离共享 Redis 的认证命名空间。
- 标准 CORS preflight 绕过登录拦截器，实际业务请求继续校验认证。
- 默认认证依赖路径不包含 Spring Security 和 sa-token-jwt。
- 认证异常日志不记录 token 原文。

## 0.9.0-RC1 - 2026-07-31

首个企业后端生产基线候选版本。

### Added

- Redis 原子 nonce 防重放与签名上下文 fail-closed 自动配置。
- 企业级上下文、认证、权限、Feign、日志、Trace、数据权限与自动配置测试基线。
- MVC 与 DDD 两套脚手架适配的框架能力。
- 可执行的自动配置语义门禁和副作用能力契约。

### Security

- 上下文附加字段全部纳入 HMAC，阻断租户、应用和昵称字段篡改。
- 修复权限检查器异常降级和 mixed 模式身份/租户冲突漏检。
- 修复 nonce 在验签前消耗及时间戳溢出边界。
- 移除未生效的端点放行配置，避免错误安全假设。

完整审查结论见 [0.9.0-RC1 生产安全审查](docs/security-audit-0.9.0-RC1.md)。
