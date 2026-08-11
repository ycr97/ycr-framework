# YCR Framework 0.9.0-RC4 发布说明

## 发布结论

`0.9.0-RC4` 完成两批生产安全收敛，重点修复多实例 ID、上下文生命周期、跨服务身份透传、
数据隔离装配和字段加密等生产风险。当前版本可作为真实项目试运行基线；DDD、Business、
SDK、CRUD 仍属于 `experimental`，不进入默认生产底座依赖。

## 主要变更

### 生产安全

- 分布式 ID 改为显式启用，Snowflake `worker-id`、`datacenter-id` 必填并启动期校验。
- Feign 身份透传改为内部客户端 allowlist，签名包含目标 audience，防止向第三方客户端泄露身份头和跨服务重放。
- 上下文 Holder 使用普通 `ThreadLocal`；Spring 异步执行器通过 `ContextTaskDecorator` 按任务捕获、恢复和清理。
- Redis 防重放使用原子 `SET NX + TTL`；Redis 缺失或异常时签名身份链路 fail-closed。
- Tenant、DataPermission 自动合并到用户自定义 MyBatis-Plus 拦截器链，并验证受治理表配置。
- MyBatis-Plus 安全拦截器顺序统一为 `Tenant -> DataPermission -> Pagination`。
- 存储、幂等、验证码、限流等副作用能力补齐显式开关和缺失依赖 fail-fast。

### 第二批 RC4 收敛

- 字段加密升级为版本化 AES-GCM envelope，支持 key-id、密钥环和密钥轮换。
- 旧版无前缀 AES-CBC 密文保留只读兼容，通过 `legacy-key-id` 指定唯一历史密钥。
- `ycr.api-doc.enabled=false` 同时关闭 SpringDoc、Swagger UI、Knife4j 及文档入口。
- Context 支持非 Servlet 应用，并自动组合业务 `TaskDecorator`；多 Primary 配置启动失败。
- CRUD 改为显式 `ycr.crud.enabled=true`，自定义 `WebMvcRegistrations` 不兼容时启动失败。
- 新增 OAuth2、Tenant、DataPermission、MyBatis-Plus、Feign 推荐企业组合栈集成测试。
- 模块成熟度划分为 `stable` 和 `experimental`，并增加稳定模块依赖边界门禁。

## 兼容性与迁移

### 字段加密

- 新密文格式为 `ycr:v1:aes-gcm:<key-id>:<nonce>:<ciphertext-and-tag>`，不再是纯十六进制字符串。
- 上线前必须确认数据库字段长度、字符集以及业务校验逻辑允许版本化 envelope。
- 旧密钥应保留在 `ycr.encrypt.keys`，并由 `legacy-key-id` 指向旧 CBC 密钥；完成数据重加密后再移除。
- `@FieldEncrypt` 已废弃，字段必须显式绑定 `EncryptTypeHandler`。

### 自动配置

- `ycr.crud.enabled` 默认 `false`，使用继承式 CRUD 端点的应用必须显式开启。
- `ycr.id.enabled` 默认 `false`；启用后必须配置唯一节点号。
- 启用数据权限时必须配置 `governed-tables`，每个受治理表必须存在对应规则。
- Feign 上下文或 Token 透传必须配置 `ycr.feign.internal-clients`。
- 业务 `TaskDecorator` 不得标记 `@Primary`，由 YCR 组合装饰器统一作为主装饰器。
- 文档关闭不会阻断共享 `/webjars/**` 命名空间，但文档入口和 OpenAPI 数据端点均不可访问。

## 验证矩阵

- 全仓 `mvn -q test`：通过。
- 自动配置副作用、版本、测试命名、模块成熟度门禁：通过。
- 仓库外独立 Maven 消费项目：通过 3 项测试，覆盖推荐企业组合栈、API Doc 总开关和 AES-GCM 密钥轮换。
- 本地 Redis `127.0.0.1:6379`：Sa-Token Redis 会话、幂等原子占位、验证码一次性消费集成测试通过。

## 已知边界

- DDD、Business、SDK、CRUD 仍为实验性模块，API 和配置可能在 1.0 前调整。
- 推荐组合栈当前以 H2 完成框架级集成验证；MySQL、PostgreSQL Testcontainers 方言矩阵安排在 1.0 前完成。
- 核心模块轻量化、BOM 职责拆分、Micrometer/Actuator 可观测性和密钥 Provider SPI 不进入本次 RC4。
