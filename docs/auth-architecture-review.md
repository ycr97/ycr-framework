# YCR Framework 认证授权架构审计与演进建议

> 状态：已确认 / 已纳入 Git
> 日期：2026-08-01
> Git 状态：本文件纳入 OAuth2 Resource Server C10 文档提交

## 1. 背景与目标

YCR Framework 的目标是融合 ContiNew Starter 的开箱即用体验与 YH Framework 的企业级上下文、权限扩展和微服务治理能力，形成一套轻量、可扩展、生产可用的企业后端基础设施。

认证授权部分需要同时满足：

- 默认链路不强制引入 Spring Security；
- 常规内部应用可以通过一个组合 Starter 快速完成登录态恢复和方法鉴权；
- 认证协议与业务授权模型解耦；
- OAuth2/OIDC 作为可选标准协议能力，不污染默认 Sa-Token 路径；
- 完整 Authorization Server 不进入基础 Starter；
- 为单体、微服务、统一 SSO 和开放平台保留清晰的演进路径。

## 2. 已确认的架构决策

本次讨论已确认以下决策：

1. Authorization Server 不进入基础框架。YCR 提供 OAuth2 Resource Server 适配器，完整认证中心作为独立参考项目或外部系统建设。
2. 提供 `ycr-starter-auth-satoken` 组合 Starter，以安全默认值和少量配置实现开箱即用。
3. 主要服务企业内部单体和微服务，同时为统一 SSO、OIDC 和开放平台预留可选扩展。
4. 在 1.0 前将现有 `ycr-starter-auth` 直接更名为 `ycr-starter-auth-satoken`，不保留旧 artifact 兼容别名。
5. 启用 Auth 后，默认所有端点必须登录，通过显式白名单放行匿名端点。
6. Session Store 默认使用内存以方便本地开发；生产环境必须显式选择 Redis，不采用检测到 Redisson 后自动切换的隐式行为。

## 3. 当前架构判断

### 3.1 总体结论

当前 YCR 没有根本偏离最初目标，并且已经避开 YH Framework 中 Spring Security/OAuth2 深度耦合的重型路线。

现有设计的主要问题不是抽象不足，而是协议无关抽象已经较完整，默认 Sa-Token 实现尚未形成生产闭环，表现为：

- `ycr-starter-auth` 名称通用，但实现完全绑定 Sa-Token；
- Auth 与 Security 需要用户自行组合，开箱即用程度不足；
- 缺少默认端点保护、Redis Session DAO 和统一 Bearer Token 默认配置；
- JWT 依赖存在但未真正装配；
- 存在空解析器和重复工具 API；
- Context 模块开始承载较多内部身份协议职责，需要限制继续扩张。

### 3.2 应当坚持的设计

#### 统一 UserContext

认证协议最终统一映射为 `UserContext`：

```text
Sa-Token Session  ─┐
OAuth2 JWT         ├─→ UserContextResolver ─→ UserContextHolder
Opaque Token       │
签名 Context Header ┘
```

业务、数据权限、审计、租户和操作日志只依赖 `UserContext`，不直接依赖 Sa-Token、JWT 或 Spring Security。

#### 协议无关授权层

`ycr-starter-security` 继续负责：

- `@RequireLogin`；
- `@RequireRole` / `@RequireAnyRole`；
- `@RequirePermission` / `@RequireAnyPermission`；
- `PermissionChecker`；
- `context` / `remote` / `mixed` 权限校验；
- `SecurityUtils`。

业务代码不使用 `@SaCheckPermission` 或 `@PreAuthorize`，避免认证产品侵入业务层。

#### 内部签名上下文传播

HMAC 签名、timestamp、nonce、Redis 防重放和身份冲突校验应保留，定位为企业微服务内部的可信身份传播协议。

该协议不替代 OAuth2，也不继续扩张为自研 Authorization Server。

## 4. 目标模块结构

```text
                    ycr-starter-context
                    用户/租户/应用上下文
                           ↑
                           │
                    ycr-starter-security
               注解 + PermissionChecker + SecurityUtils
                      协议无关授权核心
                      ↑              ↑
                      │              │
      ycr-starter-auth-satoken   ycr-starter-auth-oauth2-resource-server
         默认、轻量、开箱即用        可选、标准协议、隔离 Spring Security
                      │              │
                      └──── UserContextResolver ────┘

      独立 ycr-auth-server-example / 企业认证中心
      不进入基础 Starter，不成为默认依赖
```

依赖规则：

```text
auth-satoken
  ├─ security
  ├─ context
  └─ Sa-Token

auth-oauth2-resource-server
  ├─ security
  ├─ context
  └─ Spring Security OAuth2 Resource Server

security/context
  └─ 不依赖任何具体认证产品
```

默认 Sa-Token 依赖树中不得出现 Spring Security。只有显式引入 OAuth2 Resource Server Starter 时才允许出现 Spring Security 依赖。

## 5. P0：补齐 Sa-Token 默认生产闭环

### 5.1 模块更名与组合依赖

在 1.0 前：

```text
ycr-starter-auth → ycr-starter-auth-satoken
```

`ycr-starter-auth-satoken` 应传递引入 `ycr-starter-security`，使普通应用只需一个认证组合依赖即可获得：

- Sa-Token 登录态；
- Servlet 请求上下文恢复；
- YCR 方法鉴权；
- 统一 401/403 响应；
- Session Store 选择；
- 安全默认配置。

### 5.2 全局端点保护

启用 Auth 后默认执行：

```text
所有端点必须登录 + 显式匿名白名单
```

建议配置：

```yaml
ycr:
  auth:
    satoken:
      enabled: true
      endpoint-policy: authenticated
      permit-paths:
        - /login
        - /error
        - /actuator/health
```

可选保留：

```yaml
endpoint-policy: annotated
```

`annotated` 仅用于明确希望通过方法注解控制登录要求的应用，不作为默认值。

### 5.3 Session Store

建议配置：

```yaml
ycr:
  auth:
    satoken:
      session-store: memory
```

语义：

| 值 | 用途 | 行为 |
| --- | --- | --- |
| `memory` | 本地开发、测试、明确的单实例应用 | 使用 Sa-Token 内存 DAO |
| `redis` | 生产、多实例、滚动发布 | 必须存在 `RedissonClient`，否则启动失败 |

生产环境必须显式配置：

```yaml
ycr:
  auth:
    satoken:
      session-store: redis
```

Redis 实现直接复用项目已有的 `RedissonClient`，通过 `SaTokenDaoForRedisson` 提供分布式登录态，不创建第二套 Redis 连接池。

禁止以下行为：

- 配置 `redis` 后缺少 Redisson 时静默回退内存；
- Redis 运行异常时自动降级到本地会话；
- 根据类路径隐式改变 Session Store 语义。

### 5.4 Token 安全默认值

提供最低优先级、允许业务覆盖的默认配置：

```yaml
sa-token:
  token-name: Authorization
  token-prefix: Bearer
  is-read-header: true
  is-read-body: false
  is-read-cookie: false
```

不默认从请求体读取 Token，避免 Token 出现在参数解析、访问日志或业务对象中。

### 5.5 登录会话 API 边界

业务代码统一通过 `SecurityUtils` 读取当前身份和执行权限判断。

Sa-Token 适配器只提供会话生命周期能力，例如：

```text
login(UserContext, LoginOptions) → TokenResult
logout()
```

现有 `LoginHelper` 应收缩或更名为 `SaTokenSessionManager`，不再与 `SecurityUtils` 同时提供两套 `isLogin/getUserId/getTokenValue/getUserContext` 语义。

不对 Sa-Token 全部 API 做无意义包装；高级设备管理、踢人等能力可由明确依赖 Sa-Token 的认证应用直接使用原生 API。

### 5.6 授权模型统一

YCR 官方授权模型只支持 YCR 注解和 `PermissionChecker`。

Sa-Token 在默认实现中只负责：

- Token 读取；
- 登录态维护；
- Session 生命周期；
- 登录失效判断。

不把 Sa-Token 角色/权限注解作为第二套官方授权体系，避免业务项目混用两套语义。

## 6. 应删除或收缩的内容

### 6.1 删除当前未启用的 sa-token-jwt

当前虽然引入 `sa-token-jwt`，但没有注册任何 JWT `StpLogic`，不能视为已经支持 JWT。

P0 删除该依赖。将来出现明确需求时再设计 Sa-Token JWT 模式，不与 OAuth2 JWT 混用概念。

内部企业系统默认推荐可撤销的随机 Token + Redis Session，而不是为了 JWT 形式而引入 JWT。

### 6.2 删除空解析器 Bean

删除以下返回 `null` 或永不支持请求的占位实现：

- `ManualUserContextResolver`；
- `SystemUserContextResolver`；
- 默认空 `TokenUserContextResolver`。

保留 `UserContextResolver` SPI 即可。具体认证适配器按需注册实现，不需要空 Bean 表示未来能力。

### 6.3 收敛 Context 模块职责

Context 模块继续保留：

- 上下文模型和 Holder；
- Resolver SPI 和解析链；
- Servlet 请求恢复与 finally 清理；
- 签名上下文协议及防重放。

禁止继续加入：

- 用户密码认证；
- OAuth2 Client 管理；
- Token 签发；
- 登录页面；
- MFA；
- 业务角色和菜单加载。

### 6.4 不复制 ContiNew/YH 的全部认证功能

以下能力不进入默认 Auth Starter：

- JustAuth；
- 短信、邮箱、扫码等登录流程；
- 用户表和组织架构；
- OAuth2 Client 管理；
- 授权确认页面；
- Authorization Server；
- 自动登录业务；
- JDBC TokenStore。

这些属于认证中心应用或业务扩展，不属于通用资源服务基础设施。

## 7. P1：可选 OAuth2 Resource Server

本阶段已落地 `ycr-starter-auth-oauth2-resource-server`，当前实现状态为：JWT 与 Opaque Token 均可作为显式选择的 Resource Server 适配器运行；模块默认关闭，且与 Sa-Token、`GATEWAY_TRUST` 互斥。Servlet 链路已覆盖统一 401/403/503、YCR `UserContext`/权限桥接、CORS preflight、MIXED 身份冲突和请求 finally 清理。

具体配置、Claims 映射、错误语义、外部 IdP 边界和非目标见 [OAuth2 Resource Server 集成文档](auth-oauth2-resource-server.md)。本文件在本次确认后纳入 Git，作为架构审计结论与实现状态记录，不扩展为 Authorization Server 设计。

新增：

```text
ycr-starter-auth-oauth2-resource-server
```

职责严格限定为：

- 验证 OAuth2 JWT Access Token；
- 支持 Opaque Token Introspection；
- 校验 issuer、audience、签名和时间声明；
- 将标准或自定义 Claims 映射为 `UserContext`；
- 将 OAuth2 认证结果接入 YCR `PermissionChecker`；
- 统一 401/403 响应；
- 提供 Claims 映射 SPI。

不包含：

- Authorization Server；
- 用户登录页面；
- Client 注册管理；
- 用户、角色、租户数据库；
- Refresh Token 存储；
- MFA；
- 授权确认流程。

Spring Security 在该模块中只承担 Bearer Token 验证边界。业务授权仍由 YCR 注解和 `PermissionChecker` 完成，避免同时维护 Spring `GrantedAuthority` 与 YCR Permission 两套业务授权模型。

## 8. P2：独立认证中心参考方案

提供独立项目或示例：

```text
ycr-auth-server-example
```

可采用：

- Spring Authorization Server；或
- Keycloak、企业现有 IdP 等外部产品。

参考方案覆盖：

- OIDC Authorization Code + PKCE；
- 单点登录；
- OAuth2 Client；
- JWKS 与密钥轮换；
- Token 撤销与 Introspection；
- 用户和租户 Claims 规范；
- Resource Server 接入示例。

该项目不成为 YCR 基础 Starter 的传递依赖。

## 9. 场景支持矩阵

| 场景 | 推荐方案 | Spring Security |
| --- | --- | --- |
| 内部单体开发 | `auth-satoken` + memory | 无 |
| 内部单体生产 | `auth-satoken` + redis | 无 |
| 内部微服务 | 网关认证 + 签名上下文或 Token Relay | 默认无 |
| 接入企业 OIDC/Keycloak | OAuth2 Resource Server | 仅可选模块有 |
| 多系统 SSO | 独立认证中心 + OIDC | 认证中心/客户端按需 |
| 第三方开放平台 | 独立 Authorization Server | 不进入默认 Starter |

## 10. 测试与验收门禁

### 10.1 依赖隔离

默认 Sa-Token 路径：

```bash
mvn -pl ycr-starter-auth-satoken dependency:tree
```

验收：compile/runtime 依赖中不得出现：

```text
org.springframework.security
```

OAuth2 模块删除或不引入时，不影响 Sa-Token 应用编译、启动和测试。

### 10.2 自动配置

必须覆盖：

- Auth 未启用时不注册登录拦截器；
- Auth 启用时默认保护全部端点；
- 白名单路径允许匿名访问；
- 用户自定义 Bean 能覆盖默认 Bean；
- `memory` 与 `redis` 配置互斥；
- `redis` 模式缺少 `RedissonClient` 时启动失败；
- 不存在 Sa-Token 类路径时无错误装配。

### 10.3 真实 Web 链路

必须覆盖：

```text
业务认证成功
→ 登录并签发 Token
→ Authorization: Bearer
→ 恢复 UserContext
→ @RequirePermission
→ 200/401/403
→ finally 清理所有 Context
```

### 10.4 Redis 集成

使用真实 Redis/Testcontainers 覆盖：

- 多实例共享登录态；
- 登出后 Token 失效；
- TTL 到期；
- Redis 序列化后的 `UserContext` 可正确恢复；
- Redis 不可用时不降级本地会话。

### 10.5 OAuth2 模块

必须覆盖：

- JWT 签名、issuer、audience、exp 校验；
- Opaque Token active/inactive；
- Claims → `UserContext`；
- OAuth2 模块存在时 Spring Security Bean 正确装配；
- OAuth2 模块不存在时默认路径完全无 Spring Security。

## 11. 分阶段路线

### P0：Sa-Token 生产闭环

1. 模块更名并调整 BOM；
2. 组合引入 `security`；
3. 增加全局端点保护和白名单；
4. 增加 memory/redis Session Store；
5. 增加 Authorization/Bearer 安全默认值；
6. 删除未启用 JWT；
7. 删除空解析器；
8. 收敛 `LoginHelper` 与 `SecurityUtils`；
9. 增加真实 Web 与 Redis 集成测试；
10. 更新认证、安全和自动配置文档。

### P1：OAuth2 Resource Server

1. 创建隔离的可选模块；
2. 支持 JWT 与 Opaque Token；
3. 提供 Claims 映射 SPI；
4. 映射为统一 `UserContext`；
5. 接入 YCR 权限体系；
6. 建立依赖隔离门禁。

### P2：认证中心参考方案

1. 独立 Spring Authorization Server/外部 IdP 接入示例；
2. OIDC SSO；
3. Authorization Code + PKCE；
4. JWKS、撤销和 Introspection；
5. 单体、微服务、SSO、开放平台完整样例。

## 12. 最终原则

YCR Auth 后续演进遵守以下原则：

1. 默认轻量：默认路径不引入 Spring Security。
2. 显式选择：Session Store、OAuth2 和内部身份传播方式均具有明确语义。
3. 安全默认：启用 Auth 后默认保护全部端点，匿名访问显式声明。
4. 协议解耦：认证产品最终只负责生成统一 `UserContext`。
5. 单一授权模型：业务授权统一使用 YCR 注解和 `PermissionChecker`。
6. 不建设通用 IAM 产品：完整认证中心独立于基础 Starter。
7. 不为未来需求预埋空实现：扩展点保留接口，具体能力按真实需求增加。
8. 每项能力必须具备自动配置、行为、失败语义和真实集成测试。

最终目标不是功能最多，而是：

> 以 ContiNew 的使用成本完成常规认证接入，以 YH 的企业上下文能力支持复杂系统，同时通过可选适配器接入 OAuth2/OIDC，且不让默认应用承担 Spring Security 和完整认证中心的复杂度。
