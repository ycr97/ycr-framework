# 模块成熟度与兼容边界

YCR 以模块为单位声明成熟度。成熟度描述公开 API、自动配置契约和生产支持边界，
不代表业务项目引入后无需完成容量、故障和安全验证。

仓库目录层级和模块成熟度是两个独立维度：`foundation`、`platform`、`extensions` 表达架构职责，
`stable`、`experimental` 表达兼容承诺。实验模块集中放在 `incubator/`，但 Incubator 不构成新的 Runtime 依赖层。

## 等级定义

| 等级 | 兼容承诺 | 使用建议 |
| --- | --- | --- |
| `stable` | 纳入 RC/GA 兼容面；破坏性变更必须提供迁移说明 | 可作为企业项目默认底座按需启用 |
| `experimental` | API、配置和实现可能在小版本调整 | 仅显式评审后采用，不作为默认脚手架依赖 |

## Stable

- 基础：`core`、`common`、`json`、`validation`、`web`、`api-doc`、`context`
- 数据：`data-core`、`data-mp`、`tenant`、`data-permission`、`id-generate`、`encrypt`
- 身份安全：`security`、`auth-satoken`、`auth-oauth2-resource-server`、`protect`
- 分布式与集成：`cache`、`cache-jetcache`、`feign`、`ratelimiter`、`idempotent`、`storage`、`mq`、`messaging`
- 通用增强：`trace`、`log`、`i18n`、`captcha`、`translate`、`excel`

Stable 表示框架维护其装配和 API 契约；具有副作用的能力仍遵循默认关闭、配置完整性校验和 fail-fast 原则。

## Experimental

| 模块 | 原因 | 当前建议 |
| --- | --- | --- |
| `ycr-starter-crud` | 继承式 DO 直通和全局 HandlerMapping 对业务 API 约束较强 | 仅内部低复杂度后台使用；默认关闭 |
| `ycr-starter-sdk` | 字段注入基类与 Feign 封装边界尚未稳定 | 优先直接使用 `ycr-starter-feign` |
| `ycr-starter-business` | 通用业务拦截链缺少跨项目实践验证 | 业务确有统一扩展点模型时再引入 |
| `ycr-starter-ddd-*` | 战术建模、扩展点和状态机 API 仍可能调整 | 作为可选建模工具，不进入基础设施默认依赖 |

上述 Maven 模块同时声明 `ycr.module.maturity=experimental`，POM description 带有 `[Experimental]`。
Experimental 模块不得被 Stable starter 新增为传递依赖。
