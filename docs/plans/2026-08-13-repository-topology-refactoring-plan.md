# YCR Framework 项目结构重构方案（v3）

> 状态：已按方案实施完成，待代码审阅与提交。
> 适用基线：`0.9.0-RC5-SNAPSHOT`。
> 当前项目尚无外部使用方或消费方，因此无需提供旧仓库物理路径的迁移兼容；Maven 公共坐标与运行时契约仍按 1.0 前稳定化要求保持不变。
> 目录策略：一级架构分区、分区内模块平铺。功能分类保留在文档中，不继续编码为物理目录层级。

## 1. 文档目的

对 `ycr-framework` 仓库进行一次 **Repository Topology Refactoring**，解决当前大量 Starter 模块全部平铺在仓库根目录后产生的模块职责不清、核心边界模糊、扩展能力与实验能力混杂等问题。

本次重构的核心目标不是改变 YCR Framework 的功能，而是重新建立清晰的框架认知模型：

```text
YCR Framework
│
├── Build
│   └── 构建、版本、BOM
│
├── Foundation
│   └── 最基础、最稳定、低依赖能力
│
├── Platform
│   └── YCR 应用 Runtime Spine
│
├── Extensions
│   └── 建立在 Platform / Foundation 之上的可选能力
│
└── Incubator
    └── 按实际架构职责归类、但 API 和设计仍处于探索阶段的能力
```

最终要求做到：

```text
看到目录
    ↓
即可判断模块职责
    ↓
即可判断模块成熟度
    ↓
即可判断其在框架中的架构位置
```

本次重构应尽可能保持对框架使用方 **零感知**。

---

# 2. 当前问题

当前仓库大量 Maven 模块直接位于根目录：

```text
ycr-framework/
├── ycr-common
├── ycr-dependencies
├── ycr-framework-bom
├── ycr-starter-api-doc
├── ycr-starter-auth-oauth2-resource-server
├── ycr-starter-auth-satoken
├── ycr-starter-business
├── ycr-starter-cache
├── ycr-starter-cache-jetcache
├── ycr-starter-captcha
├── ycr-starter-context
├── ycr-starter-core
├── ycr-starter-crud
├── ycr-starter-data
├── ...
└── ycr-starter-web
```

这种组织方式的问题是所有能力在物理目录层面表现为平级关系。

例如：

```text
ycr-starter-core
ycr-starter-context
ycr-starter-web
ycr-starter-cache
ycr-starter-excel
ycr-starter-ddd
```

从目录本身无法判断：

- 哪些属于框架底座；
- 哪些属于应用 Runtime；
- 哪些只是可选扩展；
- 哪些属于实验模块；
- 哪些模块应该被重点维护；
- 哪些模块不应该被基础模块反向依赖。

随着 Starter 数量继续增加，这种结构会进一步降低项目的可理解性。

---

# 3. 重构原则

本次重构严格遵循以下原则。

## 3.1 Repository Structure ≠ Maven Coordinate

只修改：

```text
repository physical path
```

原则上不修改：

```text
groupId
artifactId
Java package
public API
configuration prefix
AutoConfiguration class
META-INF/spring 配置
runtime behavior
```

例如：

重构前：

```text
/ycr-starter-context
```

重构后：

```text
/platform/ycr-starter-context
```

但 Maven 坐标仍然必须保持：

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-context</artifactId>
</dependency>
```

禁止因为目录调整把 artifactId 改成：

```text
ycr-platform-context
```

或其他新名称。

---

## 3.2 本次是结构重构，不是功能重构

禁止顺手执行：

```text
业务逻辑重写
API 重命名
package 重命名
AutoConfiguration 重构
依赖升级
Spring Boot 升级
代码风格大规模修改
模块合并
模块拆分
```

除非某项修改是修复结构迁移造成的编译或路径问题所必需。

---

## 3.3 使用 `git mv`

所有目录迁移优先：

```bash
git mv <old-path> <new-path>
```

保证 Git 尽可能正确识别 rename，而不是产生：

```text
delete old
+
create new
```

的大量无意义 diff。

---

## 3.4 一级架构分区、分区内模块平铺

本次只建立：

```text
build
foundation
platform
extensions
incubator
```

五个一级架构区域。区域内不再按 Data、Security、Distributed、Integration、Observability、Support 等功能创建二级目录。

采用该策略的原因：

1. 当前模块名已经能够表达具体能力；
2. 二级功能分类不会形成 Maven、发布或 Runtime 边界；
3. 部分模块存在跨领域属性，过早固化分类会产生无价值的再次迁移；
4. 平铺能够控制源码和脚本路径长度；
5. 功能分类通过本文档和 README 模块表表达即可。

只有未来满足以下条件之一时，才重新评估在某个区域下增加二级目录：

```text
Extensions 增长到约 35～40 个模块
单一功能类别稳定达到 6～8 个以上模块
某类别需要独立 Maven reactor 或独立发布
某类别形成独立聚合 POM
某类别由独立团队长期维护
```

在这些条件出现前，不预建空分类目录，不为视觉对称增加层级。

---

# 4. 目标架构

最终仓库结构调整为：

```text
ycr-framework/
│
├── build/
│   ├── ycr-dependencies/
│   └── ycr-framework-bom/
│
├── foundation/
│   ├── ycr-starter-core/
│   ├── ycr-common/
│   ├── ycr-starter-json/
│   └── ycr-starter-validation/
│
├── platform/
│   ├── ycr-starter-context/
│   ├── ycr-starter-web/
│   │
│   ├── ycr-starter-data/
│   │   ├── ycr-starter-data-core/
│   │   └── ycr-starter-data-mp/
│   │
│   └── ycr-starter-security/
│
├── extensions/
│   ├── ycr-starter-api-doc/
│   ├── ycr-starter-auth-oauth2-resource-server/
│   ├── ycr-starter-auth-satoken/
│   ├── ycr-starter-cache/
│   ├── ycr-starter-cache-jetcache/
│   ├── ycr-starter-captcha/
│   ├── ycr-starter-data-permission/
│   ├── ycr-starter-encrypt/
│   ├── ycr-starter-excel/
│   ├── ycr-starter-feign/
│   ├── ycr-starter-i18n/
│   ├── ycr-starter-id-generate/
│   ├── ycr-starter-idempotent/
│   ├── ycr-starter-log/
│   ├── ycr-starter-messaging/
│   ├── ycr-starter-mq/
│   │   ├── ycr-starter-mq-core/
│   │   └── ycr-starter-mq-rocketmq/
│   ├── ycr-starter-protect/
│   ├── ycr-starter-ratelimiter/
│   ├── ycr-starter-storage/
│   ├── ycr-starter-tenant/
│   ├── ycr-starter-trace/
│   └── ycr-starter-translate/
│
├── incubator/
│   ├── ycr-starter-business/
│   ├── ycr-starter-crud/
│   ├── ycr-starter-ddd/
│   │   ├── ycr-starter-ddd-core/
│   │   ├── ycr-starter-ddd-extension/
│   │   └── ycr-starter-ddd-statemachine/
│   └── ycr-starter-sdk/
│
├── docs/
├── scripts/
├── .mvn/
├── pom.xml
├── settings.xml
└── README.md
```

---

# 5. 模块分层定义

## 5.1 Build

目录：

```text
build/
```

包含：

```text
ycr-dependencies
ycr-framework-bom
```

职责：

```text
第三方版本统一管理
YCR Starter BOM
Maven dependencyManagement
框架版本发布
```

Build 不属于 Runtime Dependency Layer。

---

# 5.2 Foundation

目录：

```text
foundation/
```

包含：

```text
ycr-starter-core
ycr-common
ycr-starter-json
ycr-starter-validation
```

Foundation 定义为：

> YCR Framework 最稳定、最基础、依赖最少的共享能力。

这里的 Foundation 表示当前框架的共享基础层，不等价于“纯 Java kernel”。当前 `ycr-starter-core` 仍包含 Spring Boot、Spring Web 和 Servlet 依赖，Core 轻量化拆分属于独立的 1.0 架构任务，不在本次物理目录重构中处理，也不得用新的目录命名掩盖该技术债务。

典型内容：

```text
基础响应模型
异常模型
错误码
枚举
基础 DTO
通用工具
JSON contract
Validation extension
```

原则：

```text
Foundation 不允许依赖 Platform

Foundation 不允许依赖 Extensions

Foundation 不允许依赖 Incubator
```

---

# 5.3 Platform

目录：

```text
platform/
```

这是 YCR Framework 的：

> Runtime Spine

包含：

```text
ycr-starter-context
ycr-starter-web
ycr-starter-data
ycr-starter-security
```

这些模块定义企业应用运行时最关键的基础能力：

```text
HTTP Request
      │
      ▼
Web
      │
      ▼
Context
      │
 ┌────┴────┐
 ▼         ▼
Security  Data
```

Platform 是后续重点进行源码理解和人工维护的框架核心区域。

---

# 5.4 Extensions

目录：

```text
extensions/
```

Extensions 表示：

> 建立在 Foundation / Platform 之上的可选能力。

使用方应该：

```text
按需依赖
按需启用
```

而不是成为所有业务服务的默认底座。

Extensions 采用单层平铺目录。下面的 Data、Security、Distributed、Integration、Observability、Support 只是文档中的功能分类，不对应二级物理目录。原因是现有 Starter 名称已经具备足够语义，继续增加目录层级不会形成新的 Maven 或运行时边界，反而增加路径、脚本和未来重新归类的成本。

---

## 5.4.1 Data Extensions

包含：

```text
tenant
data-permission
id-generate
encrypt
```

这些能力本质上属于：

```text
Persistence Enhancement
```

而不是基础 Data Runtime 本身。

---

## 5.4.2 Security Extensions

包含：

```text
auth-satoken
auth-oauth2-resource-server
protect
```

其中：

```text
ycr-starter-security
```

保留在：

```text
platform/
```

作为安全基础抽象和运行时基础能力。

具体认证机制属于扩展：

```text
Sa-Token
OAuth2 Resource Server
```

---

## 5.4.3 Distributed Extensions

包含：

```text
cache
cache-jetcache
ratelimiter
idempotent
```

表示分布式环境下的基础设施增强。

---

## 5.4.4 Integration Extensions

包含：

```text
feign
mq
messaging
storage
```

主要负责：

```text
Remote Call
Messaging
Broker Integration
External Storage
```

其中现有：

```text
ycr-starter-mq
```

继续作为 Maven aggregator：

```text
ycr-starter-mq
├── ycr-starter-mq-core
└── ycr-starter-mq-rocketmq
```

禁止拆散其内部聚合关系。

---

## 5.4.5 Observability

包含：

```text
trace
log
```

用于：

```text
Trace
Logging
Observability
```

---

## 5.4.6 Support

包含：

```text
api-doc
excel
i18n
captcha
translate
```

这些都是通用应用增强能力，但不属于 YCR Runtime 核心。

---

# 5.5 Incubator

目录：

```text
incubator/
```

Incubator 是成熟度隔离区域，不是与 Foundation、Platform、Extensions 等价的 Runtime Dependency Layer。其模块在物理目录内直接平铺，实际架构职责在文档中表达：

```text
application: crud, business
integration: sdk
modeling: ddd
```

保持现有：

```text
stable
experimental
```

成熟度定义不变。

Incubator 内模块继续保持 `experimental` maturity。Stable 模块不得因为此次重构新增指向 Incubator 模块的生产依赖。

DDD 保持原来的聚合结构：

```text
ycr-starter-ddd
├── ycr-starter-ddd-core
├── ycr-starter-ddd-extension
└── ycr-starter-ddd-statemachine
```

禁止拆散。

---

# 6. Maven 改造方案

## 6.1 不新增目录级 Aggregator POM

本阶段不要新增：

```text
foundation/pom.xml
platform/pom.xml
extensions/pom.xml
incubator/pom.xml
```

这些目录目前仅承担：

```text
Repository Organization
```

职责。

原因：

本次目标是重构物理目录，而不是增加一批没有业务意义的 Maven artifact。

根：

```text
pom.xml
```

继续作为整个仓库唯一顶层 reactor aggregator。

以后如果确实出现：

```text
分组构建
独立发布
独立 Maven lifecycle
```

需求，再考虑增加 aggregator POM。

---

# 6.2 根 POM modules

根：

```text
pom.xml
```

中的：

```xml
<modules>
```

必须改为新的物理路径。

示意：

```xml
<modules>

    <!-- Build -->
    <module>build/ycr-dependencies</module>
    <module>build/ycr-framework-bom</module>

    <!-- Foundation -->
    <module>foundation/ycr-starter-core</module>
    <module>foundation/ycr-common</module>
    <module>foundation/ycr-starter-json</module>
    <module>foundation/ycr-starter-validation</module>

    <!-- Platform -->
    <module>platform/ycr-starter-context</module>
    <module>platform/ycr-starter-web</module>
    <module>platform/ycr-starter-data</module>
    <module>platform/ycr-starter-security</module>

    <!-- Extensions（功能分类仅保留在文档，物理目录平铺） -->
    <module>extensions/ycr-starter-api-doc</module>
    <module>extensions/ycr-starter-auth-oauth2-resource-server</module>
    <module>extensions/ycr-starter-auth-satoken</module>
    <module>extensions/ycr-starter-cache</module>
    <module>extensions/ycr-starter-cache-jetcache</module>
    <module>extensions/ycr-starter-captcha</module>
    <module>extensions/ycr-starter-data-permission</module>
    <module>extensions/ycr-starter-encrypt</module>
    <module>extensions/ycr-starter-excel</module>
    <module>extensions/ycr-starter-feign</module>
    <module>extensions/ycr-starter-i18n</module>
    <module>extensions/ycr-starter-id-generate</module>
    <module>extensions/ycr-starter-idempotent</module>
    <module>extensions/ycr-starter-log</module>
    <module>extensions/ycr-starter-messaging</module>
    <module>extensions/ycr-starter-mq</module>
    <module>extensions/ycr-starter-protect</module>
    <module>extensions/ycr-starter-ratelimiter</module>
    <module>extensions/ycr-starter-storage</module>
    <module>extensions/ycr-starter-tenant</module>
    <module>extensions/ycr-starter-trace</module>
    <module>extensions/ycr-starter-translate</module>

    <!-- Incubator -->
    <module>incubator/ycr-starter-business</module>
    <module>incubator/ycr-starter-crud</module>
    <module>incubator/ycr-starter-ddd</module>
    <module>incubator/ycr-starter-sdk</module>

</modules>
```

注意：

`data`、`mq`、`ddd` 的子模块仍由它们自己的 aggregator POM 管理，因此根 POM 不需要直接列：

```text
ycr-starter-data-core
ycr-starter-data-mp

ycr-starter-mq-core
ycr-starter-mq-rocketmq

ycr-starter-ddd-core
...
```

---

# 6.3 Root Parent relativePath

当前根 POM 使用：

```text
ycr-dependencies
```

作为 parent。

移动以后：

```text
ycr-dependencies
```

位于：

```text
build/ycr-dependencies
```

因此根 POM：

```xml
<parent>
    ...
    <relativePath>build/ycr-dependencies/pom.xml</relativePath>
</parent>
```

必须同步修改。

---

# 6.4 子模块 Parent relativePath

由于本轮不创建：

```text
foundation/pom.xml
platform/pom.xml
extensions/pom.xml
incubator/pom.xml
```

所以所有以：

```text
ycr-framework
```

作为 parent 的模块必须检查 `<relativePath>`。

例如：

```text
platform/ycr-starter-context/pom.xml
```

到根 POM 为：

```xml
<relativePath>../../pom.xml</relativePath>
```

例如：

```text
extensions/ycr-starter-tenant/pom.xml
```

到根 POM 为：

```xml
<relativePath>../../pom.xml</relativePath>
```

不能继续依赖 Maven 默认：

```text
../pom.xml
```

因为新的上级目录不再是 Maven parent。

---

# 6.5 内部 Aggregator Parent 保持

以下结构：

```text
platform/ycr-starter-data/
├── pom.xml
├── ycr-starter-data-core
└── ycr-starter-data-mp
```

子模块：

```text
ycr-starter-data-core
ycr-starter-data-mp
```

继续：

```xml
<parent>
    ...
    <artifactId>ycr-starter-data</artifactId>
    ...
    <relativePath>../pom.xml</relativePath>
</parent>
```

同理：

```text
ycr-starter-mq
ycr-starter-ddd
```

内部 parent relationship 保持不变。

---

# 7. Maven 坐标兼容要求

迁移前后以下内容必须完全一致。

## groupId

```text
com.ycr.framework
```

保持不变。

## artifactId

例如：

```text
ycr-starter-context
ycr-starter-web
ycr-starter-cache
ycr-starter-mq-core
```

保持不变。

## Java Package

例如：

```text
com.ycr.framework.*
```

禁止因为目录层级改变而修改为：

```text
com.ycr.framework.platform.*
```

Repository Layer 与 Java Package Layer 不做强绑定。

---

# 8. BOM 兼容要求

`ycr-framework-bom` 中管理的 Maven coordinates 原则上完全不变。

本次目录变化不能导致消费者修改：

```xml
<dependencyManagement>
    ...
</dependencyManagement>
```

或者：

```xml
<dependency>
```

的任何 artifact 坐标。

重点验证：

```text
data-core
data-mp

mq-core
mq-rocketmq

ddd-core
ddd-extension
ddd-statemachine
```

仍全部存在于 BOM。

---

# 9. 依赖方向

目标认知模型为：

```text
Incubator
      │
      ▼
Extensions
      │
      ▼
Platform
      │
      ▼
Foundation
```

Build 不参与 Runtime dependency graph。

理想规则：

```text
Foundation
    ↓
禁止依赖 Platform / Extensions / Incubator

Platform
    ↓
允许依赖 Foundation
禁止生产依赖 Extensions / Incubator

Extensions
    ↓
允许依赖 Foundation / Platform
并允许合理的 Extension → Extension 依赖

Incubator
    ↓
允许使用 Foundation / Platform / Extensions
```

同时：

```text
Stable → Incubator
```

禁止新增。

依赖边界检查必须区分 Maven scope：`test` 依赖不作为生产依赖违规。例如 Platform 为组合栈测试使用 Extension 的 `test` scope 依赖可以保留，但不得因此形成 Runtime 传递依赖。

---

# 10. 本次不要同时进行依赖架构重构

Codex 必须区分：

```text
Physical Structure Refactoring
```

和：

```text
Dependency Architecture Refactoring
```

本次主要完成前者。

如果扫描现有 dependency graph 时发现：

```text
Foundation → Platform
Platform → Extension
Stable → Incubator
循环依赖
不合理 Extension → Extension
```

不要为了让目录模型绝对成立而擅自重写代码。

应该：

1. 记录现状；
2. 确认是否为生产依赖或 test dependency；
3. 输出 dependency violation report；
4. 只有结构迁移必需的问题本次修复；
5. 其他依赖治理留到后续独立任务。

避免一次重构同时改变：

```text
Repository Structure
+
Dependency Graph
+
Runtime Behavior
```

导致验证范围失控。

---

# 11. 配置与资源兼容

目录迁移过程中必须检查所有：

```text
src/main/resources
```

内容完整保留。

重点包括：

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports

application properties metadata

SPI files

Spring factories

XML resources

MyBatis mapper XML

SQL resources

template resources
```

目录迁移不得改变任何 AutoConfiguration 注册行为。

---

# 12. Scripts 改造

扫描：

```text
scripts/
```

中的所有脚本。

禁止假设脚本只通过：

```text
find .
```

发现模块。

搜索是否存在硬编码：

```text
ycr-starter-xxx/
```

路径。

重点检查：

```text
scripts/check-autoconfiguration-tests.sh
scripts/check-test-method-names.sh
scripts/autoconfiguration-side-effect-contracts.tsv
```

以及其他所有脚本。

凡是引用旧物理路径的地方全部更新。

当前已确认需要修改的脚本至少包括：

```text
scripts/check-autoconfiguration-tests.sh
scripts/check-module-maturity.sh
scripts/check-version-consistency.sh
```

其中 Maven reactor 选项不得改成新的长目录路径。所有 `-pl ycr-starter-*` 统一改为与物理路径无关的 artifactId selector：

```bash
mvn -pl :ycr-starter-data-permission -am test

mvn \
  -pl :ycr-starter-auth-satoken,:ycr-starter-idempotent,:ycr-starter-captcha \
  test
```

脚本中需要读取 `target` 或 POM 的位置属于真实 filesystem path，必须更新为新目录，或通过一个集中维护的模块路径映射定位；不能把 Maven selector 与文件路径混为一谈。

新增：

```text
scripts/check-module-boundaries.sh
```

作为仓库拓扑的持续门禁，至少检查：

1. 根 reactor 声明的模块路径全部存在、无重复；
2. 仓库内所有预期 Maven module 均被 reactor 覆盖；
3. 所有 parent `relativePath` 指向声明坐标匹配的真实 POM；
4. Foundation 无指向 Platform / Extensions / Incubator 的生产依赖；
5. Platform 无指向 Extensions / Incubator 的生产依赖；
6. Stable 无指向 Incubator 的生产依赖；
7. Maven `test` scope 依赖不被误判为 Runtime 边界违规；
8. 原有 BOM 中的 YCR artifacts 没有因为移动而丢失。

该脚本只验证本次已经成立的边界。若扫描发现历史依赖违规，应先记录到 dependency violation report，不得为了让门禁通过而在本次结构提交中重写依赖关系；门禁规则应准确反映经确认的基线和禁止新增的方向。

---

# 13. CI / Maven / Tooling

全仓库搜索：

```bash
git grep "ycr-starter-"
```

但不能机械替换 artifactId。

需要区分：

```text
Maven coordinate reference
```

和：

```text
filesystem path reference
```

前者原则上保持不变。

后者需要修改。

CI 和当前可执行文档中的 Maven reactor 选择也必须统一使用：

```text
-pl :artifactId
```

避免 CI 再次绑定仓库物理路径。

重点扫描：

```text
.github/
.gitlab-ci.yml
Jenkinsfile
scripts/
.mvn/
docs/
README.md
Dockerfile
Makefile
IDE config
shell scripts
```

仅修改真实存在的文件。

不要为了满足文档假设创建不存在的 CI 文件。

---

# 14. 文档改造

README 和当前有效的 docs 应同步表达新的架构分组。

历史审计报告、已完成版本的验证记录和历史计划原则上不重写，因为其中命令与路径表示当时真实状态。如确有必要避免误用，只在文档顶部增加“路径为仓库重构前结构”的提示，不修改历史结果。

README 建议增加：

```text
## Repository Structure
```

说明：

```text
build
foundation
platform
extensions
incubator
```

分别承担什么职责。

模块说明仍以 Maven artifactId 为主要名称。

例如：

```text
platform/ycr-starter-context
```

文档应该表达为：

```text
ycr-starter-context
```

而不是把：

```text
platform
```

变成 Maven namespace。

---

# 15. Module Maturity 文档

保留现有：

```text
stable
experimental
```

兼容承诺。

需要补充：

```text
Repository Layer
```

和：

```text
Module Maturity
```

是两个不同维度。

例如：

```text
platform
```

描述架构职责。

```text
stable
```

描述 API / Compatibility maturity。

不要把它们合并成同一个概念。

---

# 16. 推荐新增架构文档

新增：

```text
docs/architecture/repository-structure.md
```

内容至少描述：

```text
Build
Foundation
Platform
Extensions
Incubator
```

以及依赖方向：

```text
Foundation ← Platform ← Extensions

Incubator 中的模块按实际需要依赖 Foundation / Platform / Extensions，
但 Incubator 不构成一个新的 Runtime 层级。
```

同时明确：

```text
目录层级用于认知和组织代码，
不改变 Maven coordinates。
```

---

# 17. README 推荐架构图

增加：

```text
                         YCR Framework
                              │
              ┌───────────────┴───────────────┐
              │                               │
            Build                         Runtime
                                              │
                         ┌────────────────────┴────────────────────┐
                         │                                         │
                   Foundation                                  Platform
                         │                                         │
                         │                    ┌────────┬────────┬───┴────┐
                         │                  Context     Web     Data   Security
                         │                                         │
                         └──────────────────────┬──────────────────┘
                                                │
                                           Extensions
                                                │
                 ┌──────────┬───────────┬────────┬──────────────┬─────────┐
                 │          │           │        │              │         │
               Data      Security  Distributed Integration Observability Support
                                                │
                                  Incubator (maturity isolation)
```

注意：

Incubator 不是 Extension 的 Runtime 下层，只表示成熟度隔离区域；其中模块仍按 application / integration / modeling 表达架构职责。

---

# 18. 实施步骤

Codex 按以下顺序执行。

## Phase 1：Baseline

修改任何代码前：

```bash
git status
```

确认工作区。

记录当前 Git 状态，明确哪些修改和未跟踪文件是任务开始前已经存在的内容，迁移过程不得误提交或覆盖。

然后记录当前：

```bash
mvn clean verify
```

结果。

如果当前 baseline 已失败：

不要把已有失败误认为本次重构引入。

记录失败并继续基于现有 baseline 判断。

Baseline 完成后再执行：

```bash
mvn clean
```

确保模块目录中不存在 `target/`、`.flattened-pom.xml` 等构建产物，避免 `git mv` 把非源码内容一并迁移。

---

## Phase 2：建立目录

创建：

```text
build
foundation
platform

extensions
incubator
```

---

## Phase 3：Git Move

全部使用：

```bash
git mv
```

移动现有 Maven 模块。

不要：

```text
copy
delete
```

---

## Phase 4：修复 Maven Reactor

更新：

```text
root pom.xml
```

包括：

```text
parent relativePath
modules path
```

---

## Phase 5：修复 Parent RelativePath

扫描所有：

```text
pom.xml
```

检查：

```xml
<parent>
```

实际指向。

特别注意：

```text
foundation/*
platform/*
extensions/*
incubator/*
```

对应深度不同。

---

## Phase 6：验证 Aggregator

重点验证：

```text
platform/ycr-starter-data

extensions/ycr-starter-mq

incubator/ycr-starter-ddd
```

它们自己的子 reactor 仍正常。

---

## Phase 7：扫描硬编码路径

执行类似：

```bash
git grep "ycr-starter-"
```

逐条区分：

```text
artifactId
filesystem path
documentation
```

只修改 filesystem path 和因目录迁移失效的文档链接。

同时把当前有效的 CI、脚本和使用文档中的 `mvn -pl ycr-starter-*` 改为 `mvn -pl :ycr-starter-*`。历史审计记录按第 14 节原则处理。

---

## Phase 8：更新文档

至少更新：

```text
README.md
docs/module-maturity.md
```

并新增：

```text
docs/architecture/repository-structure.md
scripts/check-module-boundaries.sh
```

---

## Phase 9：验证

运行完整 Maven：

```bash
mvn clean test
```

然后：

```bash
mvn clean verify
```

执行项目已有质量门禁，例如：

```bash
scripts/check-autoconfiguration-tests.sh
scripts/check-module-boundaries.sh
```

以及仓库中其他现有检查脚本。

---

# 19. 必须验证的功能

本次虽然原则上不修改业务代码，但仍必须确认以下行为未发生变化。

## Maven

```text
所有 reactor module 能发现
所有 artifact 能正确构建
所有内部 dependency 可解析
所有 parent POM 可解析
BOM 正常生成
所有基于 `-pl :artifactId` 的局部 reactor 构建正常
```

---

## Spring Boot

```text
所有 AutoConfiguration 正常加载
enabled=false 行为不变
enabled=true 行为不变
Conditional Bean 行为不变
用户 Bean override 行为不变
```

---

## Testing

所有原有：

```text
unit test
AutoConfiguration test
integration test
```

继续通过。

---

# 20. Artifact 与行为等价性验证

对重构前后 Maven reactor 输出 artifact 列表进行比较。

至少比较：

```text
groupId
artifactId
packaging
```

同时比较：

```text
BOM 中 YCR dependencyManagement 条目
JAR 文件清单
META-INF/spring AutoConfiguration imports
Spring configuration metadata
SPI 文件和其他 src/main/resources 内容
```

除纯仓库路径外，不应该出现意外差异。

必须满足：

```text
Before coordinates
==
After coordinates
```

当前虽无消费方，仍执行外部临时消费者 smoke test。它不是为了兼容历史物理路径，而是验证发布后的 BOM 和 Starter 可以脱离源码 reactor 正常使用。

---

# 21. Git Diff 要求

最终 diff 应主要表现为：

```text
rename
+
pom relative path change
+
documentation path change
+
script filesystem path change
```

如果出现大量：

```text
Java source modification
public API change
package rename
AutoConfiguration rewrite
```

需要重新检查是否超出本次任务范围。

---

# 22. 版本处理原则

Repository restructuring 与版本升级是两个不同任务。

本次：

```text
不要为了结构调整主动升级版本
不要修改 Spring Boot / Spring Cloud / MyBatis 等依赖版本
不要主动修改 revision
```

如果发现仓库当前不同 POM 的：

```text
revision
```

存在不一致：

先报告。

除非这种不一致导致当前 Maven reactor 无法正常构建，否则不要顺手把版本治理混入本次提交。

---

# 23. Commit 建议

本次最好作为一个独立结构重构提交。

建议：

```text
refactor: reorganize framework modules by architecture layer
```

不要与新功能混合提交。

---

# 24. 本次明确不做

以下内容全部属于后续任务：

```text
重新设计模块依赖
拆分 core
合并 starter
重构 context
重构 web
重新设计 security
重新设计 data
升级第三方依赖
升级 Spring Boot
修改 package
修改 artifactId
修改公开 API
删除 incubator/experimental maturity module
增加新的业务能力
```

本次只有一个目标：

> 建立清晰、稳定、可持续演进的 Repository Architecture。

---

# 25. 完成后的认知模型

重构完成后，开发者应该可以仅通过目录回答：

```text
YCR 的构建基础在哪里？
→ build

YCR 最底层公共能力在哪里？
→ foundation

YCR Runtime 核心在哪里？
→ platform

YCR 可选基础设施能力在哪里？
→ extensions

YCR 尚未稳定的探索能力在哪里？
→ incubator（内部继续按 application / integration / modeling 分类）
```

其中源码学习和后续人工重点维护顺序建议：

```text
foundation/core
        ↓
platform/context
        ↓
platform/web
        ↓
platform/data
        ↓
platform/security
        ↓
extensions/*
```

---

# 26. Codex 执行要求

执行本任务时：

1. 先完整扫描仓库结构和所有 Maven POM。
2. 不要根据本文档假定某个文件一定存在，以实际仓库为准。
3. 本文档定义的是目标架构和不可违反的约束。
4. 使用 `git mv` 完成目录迁移。
5. 保持所有 Maven public coordinates 不变。
6. 保持所有 Java packages 不变。
7. 保持所有 Runtime behavior 不变。
8. 保持 stable / experimental 成熟度不变。
9. 保留 data / mq / ddd 已有子模块聚合关系。
10. 不新增目录级 aggregator POM。
11. 修复所有 Maven `relativePath`。
12. 修复 scripts / docs / CI 中失效的物理路径。
13. 完成后运行完整 Maven build 和项目已有质量门禁。
14. 新增并执行 module boundary 持续门禁。
15. 不因为发现历史代码问题而进行无关重构。
16. 对发现但不属于本次范围的问题输出报告，不擅自修复。
17. 不重写历史审计结果；当前有效命令和 CI 必须修复。
18. 最后输出完整变更总结。

最终输出至少包含：

```text
1. Final repository tree

2. Module migration mapping
   old path → new path

3. Modified Maven POMs

4. Modified scripts / documentation

5. Maven coordinate compatibility result

6. Test / verify result

7. Dependency violations discovered but not changed

8. Remaining risks

9. git diff --stat summary

10. Pre-existing worktree files preserved
```

---

# 27. Definition of Done

只有同时满足以下条件才算完成：

- [ ] 根目录不再平铺大量 Starter。
- [ ] `build / foundation / platform / extensions / incubator` 结构建立完成。
- [ ] 所有现有 Maven module 均完成归类。
- [ ] `data / mq / ddd` 内部 reactor 结构保持。
- [ ] 所有 Maven `groupId/artifactId` 保持不变。
- [ ] 所有 Java package 保持不变。
- [ ] 所有 AutoConfiguration 注册方式保持不变。
- [ ] 所有配置前缀保持不变。
- [ ] 所有 stable / experimental 标记保持不变。
- [ ] 根 reactor 可以正确发现全部模块。
- [ ] Maven BOM 仍管理原有 artifacts。
- [ ] 所有 parent `relativePath` 正确。
- [ ] scripts 中旧 filesystem path 已处理。
- [ ] 当前 CI / scripts / 使用文档中的 Maven `-pl` 已改为 `:artifactId` selector。
- [ ] module boundary 门禁已建立并通过。
- [ ] README / architecture docs 已更新。
- [ ] 原有测试通过。
- [ ] AutoConfiguration 质量门禁通过。
- [ ] `mvn clean verify` 通过，或明确记录与本次无关的既有失败。
- [ ] 外部临时消费者 smoke test 通过。
- [ ] 重构前后 BOM、JAR 资源和 AutoConfiguration 清单等价。
- [ ] 没有夹带功能重构。
- [ ] 任务开始前已有的工作区文件未被覆盖或误提交。
- [ ] 最终 Git diff 以 rename/path change 为主体。

完成以上内容后，本次 YCR Framework Repository Architecture Refactoring 才可以提交。
