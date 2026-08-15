# YCR Framework 0.9.0-RC5 至 1.0 GA 路线与遗留项

> 记录日期：2026-08-12
> 当前版本：`0.9.0-RC5-SNAPSHOT`
> 当前基线提交：`6ab4a04`
> 状态说明：RC4 生产阻断项和 RC5 CI/生产数据库方言矩阵已经完成；以下内容是尚未完成的发布闭环与 1.0 GA 治理项。

## 1. 已完成基线

以下能力不再列为遗留问题，后续只做缺陷修复和兼容性维护：

- Snowflake 多实例节点号显式配置与启动期校验。
- Feign 内部客户端 allowlist、目标 audience 签名和防重放。
- Redis 原子防重放，以及 Redis 缺失或异常时安全链路 fail-closed。
- 请求上下文按任务捕获、恢复、清理，以及业务 `TaskDecorator` 组合语义。
- Tenant、DataPermission 和 Pagination 安全拦截器自动合并与顺序验证。
- DataPermission 受治理表、规则完整性和启动期 fail-fast。
- 字段加密版本化 AES-GCM envelope、key-id 和密钥轮换。
- API Doc 总开关完整关闭 SpringDoc、Swagger UI 和 Knife4j。
- CRUD 显式启用以及不兼容 `WebMvcRegistrations` 的启动期失败。
- Stable/Experimental 模块成熟度边界及依赖门禁。
- JDK 17、JDK 21 全仓 GitHub Actions 构建。
- Redis 7.4 集成测试。
- MySQL 8.4、PostgreSQL 16 Testcontainers 方言矩阵。
- 仓库外 BOM 与 Starter 消费测试。
- 版本一致性、测试命名、自动配置副作用和 Maven 依赖收敛门禁。

## 2. RC5 发布闭环

优先级：`P0`。完成这些事项后，才能将 RC5-SNAPSHOT 判定为正式 RC5。

### RC5-01 发布说明与版本冻结

工作内容：

- 编写 `docs/release-notes-0.9.0-RC5.md`。
- 记录 CI、Redis、MySQL/PostgreSQL 方言矩阵、外部消费门禁和 RC4 之后的兼容性变化。
- 将所有版本入口从 `0.9.0-RC5-SNAPSHOT` 冻结为 `0.9.0-RC5`。
- 运行版本一致性和不可变版本检查。

验收标准：

- 全仓不存在非预期 `0.9.0-RC5-SNAPSHOT`。
- Release Notes 明确新增能力、兼容边界、配置变化、迁移要求和已知限制。
- `mvn -B -ntp clean verify` 通过。

### RC5-02 发布前生产审查

工作内容：

- 对 RC4 Tag 至 RC5 候选提交执行一次增量安全与自动配置审查。
- 确认默认关闭、缺失依赖 fail-fast、安全功能 fail-closed 等约束未退化。
- 复核 Testcontainers 方言矩阵和 Redis 集成测试均在干净 GitHub Runner 通过。

验收标准：

- 不存在未处理的 P0 阻断问题。
- 所有 required CI Job 通过。
- 审查结论持久化到 `docs/`。

### RC5-03 Tag、分支保护与制品发布

工作内容：

- 创建并推送不可变 Tag：`v0.9.0-RC5`。
- 为 `main` 配置分支保护，至少要求 JDK 17/21、Redis、MySQL、PostgreSQL 和外部消费检查通过。
- 明确发布目标：Maven Central 或 GitHub Packages，并实现发布 Workflow。
- 发布任务必须使用受保护 Environment、最小权限 Token 和人工审批。
- 发布后将开发版本推进到下一 Snapshot，禁止继续修改 RC5 Tag。

验收标准：

- Tag 指向通过全部门禁的唯一提交。
- 外部空白 Maven 工程能够从目标制品仓库导入 BOM 并使用 Starter，不依赖本地 Maven 仓库。
- 发布任务可重复执行检查，但不能覆盖同版本制品。

## 3. 1.0 GA 架构治理

### GA-01 Core 轻量化

优先级：`P1`。

当前问题：

- `ycr-starter-core` 仍传递 `spring-boot-starter`、`spring-web`，并直接依赖 Servlet API。
- 纯模型、异常、工具类与 Spring/Servlet 适配处于同一模块，非 Web 使用方会获得额外依赖。

目标方案：

- 拆分纯 Java kernel、Spring support、Servlet/Web support。
- 将 Servlet 工具迁移到 Web 支持模块。
- 清理各 Starter 对 Core 的惯性依赖，只保留实际使用的模块。
- 保留一轮兼容桥接；删除或移动公开类型时提供迁移映射。

验收标准：

- 纯 kernel 的编译依赖不包含 Spring Web 和 Servlet API。
- 非 Web 示例可只引入 kernel/Spring support 并正常启动。
- 使用 `jdeps` 或 Maven dependency tree 固化依赖边界测试。
- 现有 Stable Starter 的公开兼容变化均有迁移说明。

### GA-02 BOM 职责拆分

优先级：`P1`。

当前问题：

- `ycr-framework-bom` 同时管理 YCR 模块和完整第三方平台版本。
- 消费方无法选择“只对齐 YCR 坐标”或“由 YCR 统一整个技术栈”。
- `ycr-dependencies` 中存在未形成 Stable 能力或未实际使用的第三方版本所有权。

目标方案：

- `ycr-framework-bom`：只管理 YCR 自有模块版本。
- `ycr-platform-bom`：可选导入 Spring Boot、Spring Cloud、MyBatis-Plus、Sa-Token 等第三方版本平台。
- 清理未使用或仅 Experimental 模块需要的版本属性。
- 为两种消费模式增加仓库外集成测试。

验收标准：

- 只导入 framework BOM 时，不强制接管无关第三方依赖版本。
- 导入 platform BOM 时，推荐企业组合依赖收敛通过。
- Maven Central/GitHub Packages 中两类 BOM 坐标和使用文档清晰、可独立消费。

### GA-03 Micrometer 与 Actuator 可观测性

优先级：`P1`。

当前问题：

- 当前可观测能力以 MDC、结构化日志和慢请求日志为主。
- 尚未提供统一 Micrometer 指标、Observation 和 Actuator 健康状态约定。

目标方案：

- 提供可选的 observability Starter，不让 Micrometer/Actuator 进入所有模块的必选依赖。
- 首批覆盖 Feign、鉴权、缓存、幂等、限流、MQ、存储的请求量、延迟、失败率和拒绝次数。
- 统一低基数 Tag；禁止直接使用 userId、tenantId、URL 原始路径和异常消息作为指标标签。
- 对 Redis、对象存储、消息系统提供可选 HealthIndicator，并允许业务侧关闭。

验收标准：

- 无 Micrometer 时所有核心能力正常装配。
- 引入 observability Starter 后自动注册约定指标和健康检查。
- 指标名称、单位、Tag 基数和失败分类具有稳定文档与测试。
- 提供至少一套 Prometheus 查询与基础告警建议。

### GA-04 加密 KeyProvider SPI

优先级：`P1`。

当前问题：

- AES-GCM envelope 已支持 key-id 和静态密钥环，但密钥来源仍主要是应用配置。
- 尚无统一 KMS、Vault、配置中心或硬件密钥服务接入边界。

目标方案：

- 定义最小 `KeyProvider` SPI：按 key-id 读取解密密钥、读取当前加密 key-id，并明确缓存与刷新语义。
- 默认实现继续支持本地配置，外部 KMS/Vault 通过独立适配模块接入。
- 明确密钥不可用时 fail-closed，不允许退回明文或未知密钥。
- 设计轮换过程：新写使用新 key-id，旧密文保持可读，后台重加密完成后再撤销旧密钥。

验收标准：

- 密钥值不会进入日志、异常、配置元数据或 Actuator 输出。
- Provider 超时、缺失 key-id、轮换并发和旧密钥撤销均有测试。
- 静态配置使用方式保持兼容。

### GA-05 Experimental 模块决策

优先级：`P1`。

涉及模块：

- `ycr-starter-crud`
- `ycr-starter-sdk`
- `ycr-starter-business`
- `ycr-starter-ddd-*`

每个模块只能选择以下一种结论：

1. 经过两个以上真实项目验证后升级为 Stable。
2. 保持 Experimental，并明确 1.0 不承诺兼容。
3. 从主发布面移出或删除，避免扩大维护范围。

重点评估：

- CRUD 的继承式 DO 直通和全局 HandlerMapping 是否值得保留。
- SDK 的字段注入基类是否应删除，改为组合式 Feign 客户端。
- Business 拦截链是否存在跨项目稳定用例。
- DDD 组件是否属于通用基础设施，还是应迁移为独立扩展项目。

验收标准：

- 每个 Experimental 模块都有明确处置 ADR。
- Stable 模块不得传递依赖 Experimental 模块。
- 默认脚手架不引入未升级为 Stable 的模块。

## 4. 1.0 GA 工程与供应链治理

### GA-06 依赖升级与兼容策略

优先级：`P1`。

- 定义 Spring Boot、Spring Cloud、MyBatis-Plus、Sa-Token、Redisson 的支持矩阵。
- 配置 Renovate 或 Dependabot，仅生成受控升级 PR，不自动合并核心依赖。
- 增加二进制/API 兼容检查，破坏性变化必须进入明确的主版本或迁移流程。
- 每次依赖升级必须通过全仓、Redis、MySQL、PostgreSQL 和外部消费矩阵。

### GA-07 软件供应链与质量门禁

优先级：`P1`。

- 生成 CycloneDX SBOM，并随 Release 保留。
- 接入依赖漏洞扫描，定义阻断等级和例外到期机制。
- 接入 CodeQL 或等价静态分析。
- 建立 JaCoCo 覆盖率基线；重点约束安全、上下文、数据隔离和加密模块，不追求无意义的全仓统一高比例。
- GitHub Actions 第三方 Action 使用主版本后，GA 发布前进一步评估固定到完整 Commit SHA。

验收标准：

- Release 可追溯到源码提交、CI Run、SBOM 和制品校验和。
- Critical/High 漏洞无未审批长期例外。
- 安全关键模块的分支覆盖率和异常路径测试有明确下限。

### GA-08 文档、示例与真实项目试运行

优先级：`P1`。

- 提供最小 MVC、微服务 Resource Server、Sa-Token 单体三类可运行示例。
- 增加从 ContiNew Starter 或已有 Spring Boot 项目迁移的依赖与配置映射。
- 在至少一个真实项目完成灰度试运行，记录启动时间、依赖体积、关键接口延迟和故障演练结果。
- 建立兼容性、弃用和发布节奏说明。

验收标准：

- 新项目只依赖公开制品仓库即可运行示例。
- 至少完成 Redis 故障、认证中心不可用、数据库慢查询和密钥不可用演练。
- 真实项目反馈形成缺陷清单，P0/P1 全部关闭后再进入 1.0 GA。

## 5. 明确不纳入当前默认底座

以下内容除非出现真实项目需求，否则不应为了“功能齐全”继续加入核心框架：

- 内置 OAuth2 Authorization Server。认证中心应作为独立应用建设，YCR 保持 Resource Server Starter 定位。
- 通用工作流、组织权限后台、用户中心等业务系统能力。
- 任意 IAM 产品的专用 Claim DSL；通过 Mapper/SPI 扩展。
- 默认启用的重量级全家桶 Starter。
- 未经多项目验证的通用业务抽象。

## 6. 推荐执行顺序

1. 完成 RC5 发布说明、版本冻结、发布审查、Tag 和制品发布。
2. 在真实项目以 Stable 模块进行试运行，不默认引入 Experimental 模块。
3. 并行设计 Core 轻量化和 BOM 拆分，但分别提交，避免一次性大范围迁移。
4. 增加可选 Observability Starter 和 KeyProvider SPI。
5. 对 Experimental 模块逐个做保留、重构或移出决策。
6. 完成供应链、兼容性、覆盖率和故障演练门禁。
7. P0/P1 清零后冻结并发布 `1.0.0-RC1`；经过真实项目观察期后发布 `1.0.0`。

## 7. 完成定义

### 0.9.0-RC5 完成

- RC5 发布闭环三项全部完成。
- GitHub required checks 全绿。
- Tag 和远程制品不可变且可被仓库外项目消费。
- 无未解决 P0。

### 1.0 GA 完成

- GA-01 至 GA-08 的 P1 项全部完成，或通过 ADR 明确移出 1.0 范围。
- Stable/Experimental 边界与默认脚手架一致。
- 至少一个真实项目完成试运行和故障演练。
- 无未解决 P0/P1；公开 API、配置、发布和升级策略已经冻结。
