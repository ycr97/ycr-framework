# YCR Framework：0.9.0-RC2 与 OAuth2 Resource Server 执行计划

> 交接对象：后续执行模型（GPT-5.6 Luna）
> 规划基线：2026-08-01，`main@4abb7a3e636c185f4d9b789dbdda56913781fd1a`
> 计划性质：执行规格，不是已完成声明
> 文件策略：当前仅作为本地审阅文件，不加入 Git；用户确认后再决定是否纳入仓库

## 1. 目标与执行顺序

严格按以下顺序工作，不允许把 OAuth2 功能混入 RC2 发布分支：

1. **R0：0.9.0-RC2 发布收口**
   - 对 `main@4abb7a3` 做增量生产安全复审；
   - 修正版本号与已有 `v0.9.0-RC1` 标签之间的偏差；
   - 完成构建、真实 Redis、依赖隔离、消费方 smoke test；
   - 形成不可变的 `v0.9.0-RC2` 标签。
2. **R1：RC2 验证期**
   - 用真实业务形态验证 Sa-Token memory/Redis、多实例、CORS、认证域隔离；
   - RC2 只修缺陷，不继续扩功能。
3. **P1：可选 OAuth2 Resource Server**
   - 新建隔离模块；
   - 支持单 issuer 的 JWT 和 Opaque Token 两种显式模式；
   - 把认证结果映射为统一 `UserContext`；
   - 业务授权仍只使用 YCR 注解与 `PermissionChecker`；
   - 默认 Sa-Token 路径继续不包含 Spring Security。
4. **P2：认证中心参考方案**
   - 不在本计划中实施；
   - 等 P1 稳定后另行规划独立示例或外部 IdP 集成。

## 2. 当前事实与必须修正的问题

### 2.1 Git 与版本事实

- 当前 `main`：`4abb7a3`。
- 当前标签：`v0.9.0-RC1`，指向 `b7b3c20`。
- `main` 比 `v0.9.0-RC1` 多 3 个提交，其中包括：
  - 测试方法命名规范治理；
  - Sa-Token 生产闭环；
  - Redis 认证域隔离与 CORS preflight 修复。
- 以下四处仍声明 `0.9.0-RC1`：
  - `.mvn/maven.config`；
  - `pom.xml`；
  - `ycr-dependencies/pom.xml`；
  - `ycr-framework-bom/pom.xml`。

结论：**不能重新发布或移动 `v0.9.0-RC1`，下一版本必须是 `0.9.0-RC2`。**

### 2.2 发布基础设施事实

仓库当前不存在：

- `.github/workflows`；
- Maven `distributionManagement`；
- Maven Central、Nexus、Artifactory 或 GitHub Packages 发布配置；
- 可确认的远程制品发布命令与凭据注入方案。

因此在用户明确制品仓库之前，RC2 的“发布”只允许表示：

- 版本已冻结；
- 测试已通过；
- Git 提交与 annotated tag 已推送；
- 本地 Maven 仓库可完成消费验证。

**禁止后续模型擅自选择制品仓库、把凭据写入仓库、或声称远程 Maven 制品已发布。**

### 2.3 本地用户文件

以下文件当前未跟踪，属于用户内容或审阅材料：

- `docs/auth-architecture-review.md`；
- `img.png`；
- 本计划文件。

执行过程中不得 `git add .`，必须逐文件暂存。未经用户明确确认，不得把这些文件加入 Git。

## 3. 全局工程约束

后续模型必须遵守：

1. 不 force-push，不移动、不删除、不覆盖已有 tag。
2. 不使用 `git reset --hard`、`git checkout --` 清理用户修改。
3. 每个阶段开始先执行 `git status --short --branch`。
4. 测试方法名必须是 ASCII lowerCamelCase；`@DisplayName` 可以使用中文。
5. 不顺带升级 Spring Boot、Spring Cloud、Sa-Token、Redisson 或其他依赖。
6. 不把 Spring Security 引入 `ycr-starter-auth-satoken` 或默认框架消费路径。
7. 不引入 `sa-token-jwt`。
8. 不在 OAuth2 模块中实现 Authorization Server、OAuth2 Client、OAuth2 Login、用户库、MFA、登录页或 Token 签发。
9. 不同时启用 Sa-Token 与 OAuth2 Resource Server；必须启动失败而不是猜测优先级。
10. 不建立第二套业务授权模型；Spring `GrantedAuthority` 只作为框架内部认证结果，不作为 YCR 官方业务权限 API。
11. 所有安全失败必须 fail-closed；不得回退匿名、回退本地会话或跳过校验。
12. 一个提交只解决一个清晰阶段；提交前执行与阶段风险相匹配的验证。

---

# Part A：0.9.0-RC2 发布收口

## 4. A0：创建发布分支与基线确认

### 4.1 命令

```bash
git switch main
git fetch origin --prune
git pull --ff-only origin main
git status --short --branch
git rev-parse HEAD
git rev-parse origin/main
git show-ref --verify refs/tags/v0.9.0-RC1
git switch -c release/0.9.0-RC2
```

### 4.2 验收

- `HEAD` 与 `origin/main` 都是 `4abb7a3e636c185f4d9b789dbdda56913781fd1a`，或者用户后续明确加入的新提交；
- 不存在未提交的 tracked 修改；
- 只允许看到已知的未跟踪用户文件；
- `v0.9.0-RC1` 存在且保持不变。

如果 `origin/main` 已前进，停止执行，先审查新增提交，不能直接套用本计划中的 commit 数量。

## 5. A1：对 RC1 之后的增量做生产安全复审

### 5.1 审查范围

```bash
git log --oneline v0.9.0-RC1..HEAD
git diff --stat v0.9.0-RC1..HEAD
git diff v0.9.0-RC1..HEAD -- \
  ycr-starter-auth-satoken \
  ycr-starter-context \
  ycr-starter-security \
  ycr-starter-web \
  pom.xml ycr-dependencies/pom.xml ycr-framework-bom/pom.xml \
  scripts docs
```

重点核对：

1. **端点门禁**
   - Auth 默认关闭；
   - 开启后 `authenticated` 默认保护 `/**`；
   - `/error` 默认放行；
   - 业务白名单显式配置；
   - CORS preflight 绕过登录检查，但普通 `OPTIONS` 不应被无条件放行。
2. **Token 输入面**
   - 只读 `Authorization: Bearer`；
   - 默认不读 body，不读 cookie；
   - 不记录 token 原文。
3. **Redis 会话隔离**
   - Redis 模式必须配置 `ycr.auth.satoken.auth-domain`；
   - 该值绑定为 `StpLogic.loginType`；
   - 自定义 `StpLogic` 的 loginType 必须与 auth-domain 一致；
   - 无关服务共享 Redis 时使用不同 auth-domain；
   - Redis 缺失或异常不回退内存。
4. **上下文生命周期**
   - 每 token 独立 `UserContext`；
   - 请求结束 finally 清理 User/Tenant/App Holder 和 MDC；
   - mixed 模式身份及租户冲突继续 fail-closed。
5. **授权模型**
   - YCR 注解和 `PermissionChecker` 是唯一官方授权入口；
   - Auth 启用时 YCR 方法鉴权切面可用；
   - 默认编译依赖不出现 Spring Security 和 `sa-token-jwt`。
6. **自动配置语义**
   - 副作用能力默认关闭；
   - memory/redis 语义不随类路径隐式改变；
   - 缺依赖时错误信息明确；
   - 自定义 Bean 的 back-off 不破坏安全不变量。

### 5.2 审计文档

新增 tracked 文件：

```text
docs/security-audit-0.9.0-RC2.md
```

不要修改 RC1 审计结论。RC2 文档至少包含：

- 审查日期、分支、基线 commit；
- 增量范围 `v0.9.0-RC1..HEAD`；
- Redis namespace collision 与 CORS preflight 两个阻断项的发现、修复、测试证据；
- 未解决 Critical / High 数量；
- 实际执行的测试数量，不复制 RC1 的旧数字；
- 已知 Medium / Low 项；
- 生产配置中 auth-domain 的要求。

### 5.3 退出条件

- 未解决 Critical / High 必须为 0；
- 如果出现新的阻断项，先建独立 `fix/...` 分支修复并复审；
- 审计没有通过时禁止改版本、打 tag 或推 main。

## 6. A2：版本与变更日志

### 6.1 只修改版本源文件

将以下四处从 `0.9.0-RC1` 改成 `0.9.0-RC2`：

```text
.mvn/maven.config
pom.xml
ycr-dependencies/pom.xml
ycr-framework-bom/pom.xml
```

不要手工修改 `.flattened-pom.xml`；它们已被 `.gitignore` 忽略，由 Maven 生成。

### 6.2 CHANGELOG

在 `CHANGELOG.md` 顶部增加：

```markdown
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
```

最终措辞以实际 diff 和审计结果为准，不能写未实现能力。

### 6.3 版本一致性检查

```bash
rg -n '<revision>0\.9\.0-RC1</revision>|-Drevision=0\.9\.0-RC1' \
  .mvn pom.xml ycr-dependencies ycr-framework-bom

mvn -q help:evaluate -Dexpression=project.version -DforceStdout
```

预期：

- 第一条没有输出；
- Maven 输出 `0.9.0-RC2`。

历史文档中的 RC1 链接和 `docs/security-audit-0.9.0-RC1.md` 不应被批量替换。

## 7. A3：RC2 发布门禁

### 7.1 静态门禁

```bash
./scripts/check-test-method-names.sh
./scripts/check-autoconfiguration-tests.sh
git diff --check
```

预期全部退出码为 0。

### 7.2 全量干净构建

必须使用 `clean`，防止认证模块更名后旧 target 产物污染结果：

```bash
mvn -q clean test
```

预期：

- reactor 全部模块成功；
- failures = 0；
- errors = 0；
- 记录实际 tests/skipped 数量到 RC2 审计文档。

不要把测试日志中的预期异常堆栈误判为失败，以 Maven 最终退出码和 Surefire XML 为准。

### 7.3 真实 Redis 集成测试

使用用户本机 Redis：

```bash
YCR_REDIS_INTEGRATION_TESTS=true \
YCR_TEST_REDIS_ADDRESS=redis://127.0.0.1:6379 \
mvn -q -pl ycr-starter-auth-satoken \
  -Dtest=SaTokenRedisIntegrationTest test
```

预期：2 tests，0 failures，0 errors，0 skipped。

测试必须使用随机 key 并 finally 清理，不允许执行 `FLUSHDB` 或删除非测试 key。

### 7.4 依赖隔离

```bash
mvn -q -pl ycr-starter-auth-satoken dependency:tree \
  -Dscope=compile \
  -DoutputFile=target/auth-satoken-compile-dependencies.txt

rg 'org\.springframework\.security:|cn\.dev33:sa-token-jwt:' \
  ycr-starter-auth-satoken/target/auth-satoken-compile-dependencies.txt
```

预期第二条没有输出。

### 7.5 安装与 flattened POM 验证

```bash
mvn -q -DskipTests install

rg -n '\$\{revision\}' \
  ycr-dependencies/.flattened-pom.xml \
  ycr-framework-bom/.flattened-pom.xml \
  ycr-starter-auth-satoken/.flattened-pom.xml
```

预期 flattened POM 不残留 `${revision}`，本地仓库存在 RC2 BOM 和 Starter。

### 7.6 外部消费 smoke test

在 `/private/tmp/ycr-rc2-smoke` 建立最小 Maven 工程，不加入本仓库：

1. import `com.ycr.framework:ycr-framework-bom:0.9.0-RC2`；
2. 不写版本号引入 `ycr-starter-web` 与 `ycr-starter-auth-satoken`；
3. 启用 memory 模式，验证应用上下文启动；
4. 验证未登录私有端点 401、白名单 200、登录后访问 200；
5. 第二个测试配置 `session-store=redis` 但不配置 `auth-domain`，预期启动失败并包含明确错误；
6. 第三个测试配置 Redis + `auth-domain=smoke-app`，预期启动成功。

该 smoke test 的意义是验证“发布后的 POM 可被外部项目消费”，不能用 reactor 内部模块依赖代替。

## 8. A4：提交、合并、标签与推送

### 8.1 暂存

只能显式暂存：

```bash
git add .mvn/maven.config \
  pom.xml \
  ycr-dependencies/pom.xml \
  ycr-framework-bom/pom.xml \
  CHANGELOG.md \
  docs/security-audit-0.9.0-RC2.md
```

再次执行：

```bash
git status --short
git diff --cached --check
git diff --cached
```

确保没有加入用户未跟踪文件。

### 8.2 发布提交

```bash
git commit -m "release: prepare 0.9.0-RC2"
git push -u origin release/0.9.0-RC2
```

### 8.3 合并 main

只有所有门禁通过后：

```bash
git switch main
git fetch origin --prune
git pull --ff-only origin main
git merge --ff-only release/0.9.0-RC2
git push origin main
```

如果不能 fast-forward，停止并审查分叉，禁止自动创建不明 merge commit。

### 8.4 annotated tag

tag 必须打在已经推送的 `main` release commit 上：

```bash
git tag -a v0.9.0-RC2 -m "YCR Framework 0.9.0-RC2"
git push origin v0.9.0-RC2
```

验证：

```bash
git rev-parse HEAD
git rev-list -n 1 v0.9.0-RC2
git ls-remote origin \
  refs/heads/main \
  refs/tags/v0.9.0-RC2 \
  'refs/tags/v0.9.0-RC2^{}'
git status --short --branch
```

`HEAD`、`origin/main` 与 tag peeled commit 必须一致。

### 8.5 远程 Maven 制品决策门

用户必须明确选择以下之一后才实施：

- Maven Central：适合公开开源，需要 namespace、签名、Central token 和发布工作流；
- 企业 Nexus/Artifactory：适合企业内部，需要仓库 URL、server id 和凭据注入约定；
- GitHub Packages：配置简单，但 Maven 消费认证体验需要提前确认；
- 暂不发布制品：只保留 Git tag 和本地消费验证。

后续模型不得在未选择时自行新增 `distributionManagement`。

---

# Part B：RC2 验证期

## 9. B0：验证矩阵

RC2 至少在以下场景验证：

| 场景 | 配置 | 预期 |
| --- | --- | --- |
| 单实例开发 | Sa-Token + memory | 登录、权限、登出完整可用 |
| 单实例生产模拟 | Sa-Token + Redis + auth-domain | 重启后 token 仍可验证 |
| 双实例 | 两节点共享 Redis 和相同 auth-domain | 节点 A 登录，节点 B 可恢复上下文 |
| 应用隔离 | 两应用共享 Redis、不同 auth-domain | A token 在 B 无效 |
| 显式 SSO 域 | 两服务相同 auth-domain | 只有确认需要共享登录态时才允许互认 |
| CORS | 合法 Origin preflight | OPTIONS 通过，实际无 token 请求仍 401 |
| Redis 故障 | Redis 停止或超时 | 认证失败，不回退 memory |
| 滚动发布 | 新旧节点交替 | Session 序列化兼容，无随机登出 |

## 10. B1：RC2 期间的变更纪律

- 只接受 blocker/high 缺陷和明确的兼容性修复；
- 不在 RC2 分支开发 OAuth2；
- 每个修复独立提交并重新执行 A3；
- 如果 RC2 tag 后出现代码修复，下一标签是 RC3，禁止移动 RC2。

---

# Part C：P1 OAuth2 Resource Server

## 11. C0：架构决策

### 11.1 模块边界

新增：

```text
ycr-starter-auth-oauth2-resource-server
```

它是显式选择的 Servlet Resource Server 适配器，负责：

- 验证 JWT Access Token；
- 调用 Introspection Endpoint 验证 Opaque Token；
- issuer、audience、签名、时间声明校验；
- Claims/attributes 映射为 `UserContext`；
- 把 `UserContext` 安装到 YCR Holder 与 MDC；
- 统一认证 401、授权 403、认证服务不可用 503；
- 自动启用 YCR `AuthorizeAspect`；
- 提供 Claims 映射 SPI。

它不负责：

- Authorization Server；
- OAuth2 Client、OAuth2 Login；
- Token 签发、刷新、撤销存储；
- 用户、角色、租户数据库；
- 登录页面、授权确认页、MFA；
- 多 issuer；
- DPoP、mTLS sender-constrained token；
- Introspection 结果缓存。
- Servlet async dispatch / `WebAsyncTask` 的 YCR ThreadLocal 传播增强。

### 11.2 认证与授权分工

```text
Spring Security Resource Server
    └─ Bearer Token 提取与密码学/Introspection 验证
        └─ OAuth2UserContextFilter
            └─ Claims → UserContext → YCR Holders/MDC
                └─ YCR @Require* + PermissionChecker
```

禁止：

- 在业务示例中推广 `@PreAuthorize`；
- 用 `hasAuthority` 编写业务权限规则；
- 同时维护 Spring `GrantedAuthority` 和 YCR permissions 两套角色权限映射。

Spring Security URL 层只判断：匿名白名单或 `authenticated()`。业务权限由 YCR 完成。

### 11.3 为什么不能实现为普通 UserContextResolver

现有 `ContextFilter` 的 Servlet 注册顺序是 `Ordered.HIGHEST_PRECEDENCE + 10`，早于 Spring Security 的 Bearer Token 认证链。此时 `SecurityContextHolder` 尚未包含 JWT/Opaque 认证结果。

因此不能新增一个从 `SecurityContextHolder` 读取数据的 `UserContextResolver`；它会始终过早执行。

正确顺序：

```text
Servlet ContextFilter（解析可信签名 Header，负责最外层 finally 清理）
  → Spring Security FilterChainProxy
    → BearerTokenAuthenticationFilter（验证 token）
      → OAuth2UserContextFilter（映射并安装 YCR 上下文）
        → AuthorizationFilter（authenticated/permitAll）
          → Controller / YCR AuthorizeAspect
```

`OAuth2UserContextFilter` 必须通过：

```java
http.addFilterAfter(filter, BearerTokenAuthenticationFilter.class);
```

加入 Spring Security 链。不要改动全局 `ContextFilter` 顺序来迁就 OAuth2。

### 11.4 与 Sa-Token 的关系

- 两个模块可以同时存在于 BOM；
- 两个 jar 可以同时位于类路径但默认均关闭；
- `ycr.auth.satoken.enabled=true` 与 `ycr.auth.oauth2.resource-server.enabled=true` 同时出现时必须启动失败；
- 不做“Sa-Token 优先”或“OAuth2 优先”的隐式选择。

## 12. C1：模块、POM 与 BOM

### 12.1 修改文件

```text
pom.xml
ycr-framework-bom/pom.xml
scripts/autoconfiguration-side-effect-contracts.tsv
scripts/check-autoconfiguration-tests.sh
docs/starter-autoconfiguration-matrix.md
README.md
```

新增模块 POM：

```text
ycr-starter-auth-oauth2-resource-server/pom.xml
```

### 12.2 编译依赖

推荐依赖：

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-security</artifactId>
    <version>${revision}</version>
</dependency>
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-json</artifactId>
    <version>${revision}</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

测试依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-web</artifactId>
    <version>${revision}</version>
    <scope>test</scope>
</dependency>
```

不要显式锁 Spring Security 版本；由当前 Spring Boot 3.3.7 BOM 管理。

### 12.3 依赖隔离门禁

扩展 `scripts/check-autoconfiguration-tests.sh`：

1. Sa-Token compile tree 仍不得包含：
   - `org.springframework.security:*`；
   - `cn.dev33:sa-token-jwt`。
2. OAuth2 Resource Server compile tree 必须包含：
   - `spring-security-oauth2-resource-server`；
   - JWT 模式需要的 `spring-security-oauth2-jose`。
3. OAuth2 Resource Server compile tree 不得包含：
   - `spring-security-oauth2-client`；
   - `spring-security-oauth2-authorization-server`。

### 12.4 认证适配器互斥门禁

同时修改两侧，而不是只在新模块中检查：

- `SaTokenAuthAutoConfiguration` 启用时检查
  `ycr.auth.oauth2.resource-server.enabled`；
- `OAuth2ResourceServerAutoConfiguration` 启用时检查
  `ycr.auth.satoken.enabled`。

任一侧发现双方同时启用，都抛出包含两个完整配置键的启动异常。测试必须分别从两个自动配置入口证明 fail-fast，避免用户排除其中一个自动配置后失去门禁。

## 13. C2：配置模型与启动语义

### 13.1 配置前缀

统一使用：

```text
ycr.auth.oauth2.resource-server
```

不要缩写成含义模糊的 `ycr.oauth2`，为未来 Client/Authorization Server 边界留空间。

### 13.2 推荐 YAML

JWT：

```yaml
ycr:
  auth:
    oauth2:
      resource-server:
        enabled: true
        mode: jwt
        endpoint-policy: authenticated
        permit-paths:
          - /error
          - /actuator/health
        jwt:
          issuer-uri: https://idp.example.com/realms/ycr
          jwk-set-uri: https://idp.example.com/realms/ycr/protocol/openid-connect/certs
          audiences:
            - order-api
          allowed-algorithms:
            - RS256
          clock-skew: 60s
        claims:
          user-id: user_id
          username: preferred_username
          nickname: name
          tenant-id: tenant_id
          dept-id: dept_id
          roles: roles
          permissions: permissions
          scopes: scope
          client-id: client_id
```

Opaque：

```yaml
ycr:
  auth:
    oauth2:
      resource-server:
        enabled: true
        mode: opaque
        endpoint-policy: authenticated
        permit-paths:
          - /error
        opaque:
          introspection-uri: https://idp.example.com/oauth2/introspect
          client-id: ${OAUTH2_INTROSPECTION_CLIENT_ID}
          client-secret: ${OAUTH2_INTROSPECTION_CLIENT_SECRET}
          audiences:
            - order-api
          issuer: https://idp.example.com
          connect-timeout: 2s
          read-timeout: 2s
```

### 13.3 属性定义

新增：

```text
OAuth2ResourceServerProperties
├─ enabled: boolean = false
├─ mode: TokenMode = null                 # 启用后必填，JWT/OPAQUE
├─ endpointPolicy = AUTHENTICATED         # AUTHENTICATED/ANNOTATED
├─ permitPaths = [/error]
├─ jwt
│  ├─ issuerUri                           # JWT 必填
│  ├─ jwkSetUri                           # 可选；生产推荐配置
│  ├─ audiences                           # JWT 必填且非空
│  ├─ allowedAlgorithms = [RS256]
│  └─ clockSkew = 60s
├─ opaque
│  ├─ introspectionUri                    # OPAQUE 必填
│  ├─ clientId                            # OPAQUE 必填
│  ├─ clientSecret                        # OPAQUE 必填
│  ├─ audiences                           # OPAQUE 必填且非空
│  ├─ issuer                              # 可选；配置后严格比较
│  ├─ connectTimeout = 2s
│  └─ readTimeout = 2s
└─ claims
   ├─ userId = user_id
   ├─ username = preferred_username       # 缺失时 fallback sub
   ├─ nickname = name
   ├─ tenantId = tenant_id
   ├─ deptId = dept_id
   ├─ roles = roles
   ├─ permissions = permissions
   ├─ scopes = scope                      # 同时兼容 scp
   └─ clientId = client_id                # 缺失时 fallback azp
```

### 13.4 启动门禁

使用显式 `InitializingBean` 或等价校验 Bean，错误信息必须包含完整配置键。

必须失败的情况：

- enabled=true 但 mode 缺失；
- mode=jwt 且 issuer/audiences/algorithm 缺失；
- mode=opaque 且 URI/client-id/client-secret/audiences 缺失；
- timeout 非正数；
- allowed algorithm 包含 `none` 或对称 HMAC 算法；
- Sa-Token 与 OAuth2 同时 enabled；
- `ycr.context.security-mode=GATEWAY_TRUST` 与 OAuth2 同时启用；
- 用户自定义关键 Bean 导致配置声明与实际模式不一致时，应给出明确错误或在文档中声明用户完全接管该边界。

默认关闭测试与显式启用测试必须加入 `autoconfiguration-side-effect-contracts.tsv`。

## 14. C3：Context 的行为保持型重构

OAuth2 代码前先完成，单独提交，禁止与模块骨架混在同一提交。

### 14.1 抽取身份一致性校验

新增：

```text
ycr-starter-context/src/main/java/com/ycr/framework/context/security/UserContextIdentityVerifier.java
```

提供单一公共逻辑：

```java
public final class UserContextIdentityVerifier {
    public static void verifyCompatible(UserContext trusted, UserContext token);
}
```

语义必须与当前 `UserContextResolverChain` 完全一致：

1. 双方都有 userId：必须相等；
2. 否则双方都有非空 username：必须相等；
3. 无法证明同一身份：拒绝；
4. 双方 tenantId 都非空且不同：拒绝。

将 `UserContextResolverChain` 的 private conflict 逻辑替换为调用该工具。测试必须证明行为没有变化。

### 14.2 抽取 Servlet 上下文绑定器

新增：

```text
ycr-starter-context/src/main/java/com/ycr/framework/context/servlet/ServletContextBinder.java
```

职责：

```java
void bind(UserContext userContext, HttpServletRequest request);
void clear();
```

`bind` 必须：

- 先清理旧 User/Tenant/App Holder 与相关 MDC，防止残留；
- 设置 `UserContextHolder`；
- 设置 MDC userId/tenantId/clientId；
- userContext 有 tenantId 时设置 `TenantContextHolder`；
- 只有来源是 `GATEWAY_HEADER` 时才从已验签 Header 恢复 tenantCode/appId；
- TOKEN 来源不得读取裸 tenant/app Header。

`clear` 必须清理：

- `UserContextHolder`；
- `TenantContextHolder`；
- `AppContextHolder`；
- MDC userId/tenantId/clientId。

修改 `ContextFilter` 使用 binder。为减少 RC 阶段 API 破坏，保留原两参数构造器，并增加注入 binder 的构造器，或者证明该类没有外部构造兼容要求后在变更日志明确说明。

### 14.3 重构验收

```bash
mvn -q -pl ycr-starter-context test
mvn -q -pl ycr-starter-security -am test
```

原有 ContextFilter、mixed conflict、MDC、tenant/app 恢复测试全部通过；新增 binder 独立测试。

提交建议：

```text
refactor(context): extract identity verification and servlet binding
```

## 15. C4：Claims 映射 SPI

### 15.1 文件

```text
mapper/OAuth2UserContextMapper.java
mapper/DefaultOAuth2UserContextMapper.java
mapper/OAuth2ClaimsMappingException.java
```

接口保持最小：

```java
public interface OAuth2UserContextMapper {
    UserContext map(Map<String, Object> claims);
}
```

不要把 `Jwt` 或 `BearerTokenAuthentication` 放进 SPI，确保同一个 mapper 同时支持 JWT 与 Opaque。

### 15.2 默认映射语义

- userId/tenantId/deptId：接受 `Number` 或十进制字符串；非空但不可转换时认证失败，不能静默丢弃；
- username：配置 claim，缺失时 fallback `sub`；
- clientId：配置 claim，缺失时 fallback `azp`；
- roles/permissions：接受字符串、数组或 Collection，标准化为去重 `Set<String>`；
- scopes：兼容空格分隔的 `scope` 和 Collection 类型 `scp`，合并到 permissions；
- 空字符串、null、非字符串集合元素按明确规则忽略或拒绝，测试必须锁定；
- 至少存在 userId 或 username，否则认证失败；
- mapper 返回后由桥接过滤器强制设置 `source=TOKEN`，自定义 mapper 不得伪造来源。

不支持 Keycloak `realm_access.roles` 等任意嵌套表达式 DSL；需要时业务方通过自定义 `OAuth2UserContextMapper` 实现，避免在 P1 发明表达式语言。

### 15.3 Back-off

默认 mapper 使用：

```java
@ConditionalOnMissingBean(OAuth2UserContextMapper.class)
```

自定义 mapper 测试必须证明默认 Bean 回退。

## 16. C5：JWT 验证实现

### 16.1 Bean

在 mode=JWT 时创建默认 `JwtDecoder`，允许用户用自定义 `JwtDecoder` 接管：

```java
@ConditionalOnMissingBean(JwtDecoder.class)
```

默认 decoder 使用 Nimbus，并配置：

- issuer；
- JWK Set；
- asymmetric JWS algorithm allowlist，默认只 RS256；
- timestamp（exp/nbf）与 clock skew；
- audience exact match，大小写敏感；
- 签名失败、issuer 错误、audience 错误、过期和未生效统一认证失败。

生产推荐同时配置 issuer-uri 与 jwk-set-uri：

- issuer 用于 claim 校验；
- 显式 jwk-set-uri 避免应用启动强依赖 OIDC discovery 可用性；
- 运行期取 key 失败仍必须 fail-closed。

### 16.2 Audience validator

新增独立类：

```text
validator/JwtAudienceValidator.java
```

规则：token `aud` 与配置集合至少一个交集才成功；配置集合为空属于启动错误，不由 validator 放行。

### 16.3 明确不做

- 不支持 HMAC shared-secret JWT；
- 不支持 `alg=none`；
- 不支持多 issuer 自动发现；
- 不提供 `JwtEncoder`，不签发 token。

## 17. C6：Opaque Token 实现

### 17.1 默认 introspector

mode=OPAQUE 时创建：

```java
@ConditionalOnMissingBean(OpaqueTokenIntrospector.class)
```

使用 Spring Security `SpringOpaqueTokenIntrospector`，配置 Basic Auth、连接超时和读取超时。

默认不重试，避免 IdP 故障时放大流量。

### 17.2 二次校验包装器

新增：

```text
introspection/ValidatingOpaqueTokenIntrospector.java
```

delegate 验证 active 后，再校验：

- audience 与配置至少一个交集；
- 配置 issuer 时 exact match。

身份 Claims 的完整性由桥接过滤器调用 mapper 后校验，不能在 introspector 和 filter 中重复映射。

无效/inactive token：401。
Introspection 网络超时、5xx、解析失败：fail-closed，并映射为 503“认证服务暂不可用”，不得伪装成登录过期，也不得放行。

### 17.3 不缓存

P1 不缓存 introspection 结果，因为缓存会改变撤销时效。性能需求出现后再单独设计带最大 TTL、token exp 上限和撤销语义的缓存。

## 18. C7：Spring Security 链与 YCR 上下文桥接

### 18.1 SecurityFilterChain

新增默认 chain，建议名称：

```text
ycrOAuth2ResourceServerSecurityFilterChain
```

该 chain 使用固定的后置顺序（建议 `@Order(100)`），并只按 Bean 名称 back-off，不得使用
`@ConditionalOnMissingBean(SecurityFilterChain.class)` 让任意业务 chain 意外关闭 YCR 安全链。业务自定义 chain 必须使用更具体的 `securityMatcher`；P1 不支持用另一个 catch-all chain 完全替换 YCR chain。

核心配置：

```java
http
    .csrf(AbstractHttpConfigurer::disable)
    .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .requestCache(AbstractHttpConfigurer::disable)
    .formLogin(AbstractHttpConfigurer::disable)
    .httpBasic(AbstractHttpConfigurer::disable)
    .logout(AbstractHttpConfigurer::disable)
    .cors(Customizer.withDefaults());
```

授权规则：

1. `CorsUtils::isPreFlightRequest` permitAll；
2. permitPaths permitAll；
3. `AUTHENTICATED`：其余 `authenticated()`；
4. `ANNOTATED`：其余 `permitAll()`，由 YCR 方法注解决定业务授权。

JWT/OPAQUE 只启用与 mode 对应的 DSL，禁止同时装配。

不要加 `@EnableMethodSecurity`，不要启用 Spring 方法授权。

### 18.2 OAuth2UserContextFilter

新增：

```text
filter/OAuth2UserContextFilter.java
```

算法：

1. 从 `SecurityContextHolder` 获取已认证 Authentication；
2. JWT 从 `JwtAuthenticationToken#getToken().getClaims()` 取 claims；
3. Opaque 从 `BearerTokenAuthentication#getTokenAttributes()` 取 attributes；
4. mapper 生成 UserContext，强制 source=TOKEN；
   - mapper 异常或无法形成有效身份时调用统一 entry point 返回 401，不能传播为 500；
5. 如果没有已有 YCR context：binder.bind(tokenContext)；
6. 如果已有 `GATEWAY_HEADER` context 且 security-mode=MIXED：
   - 调用 `UserContextIdentityVerifier.verifyCompatible`；
   - 一致时保留已验签 Header context，token 只负责证明身份；
   - 冲突时认证失败；
7. 调用下游 filter chain；
8. 如果本过滤器安装了 context，在 finally 中 binder.clear；
9. 最外层 ContextFilter 仍执行最终兜底清理。

如果已有来源既不是 GATEWAY_HEADER，也不是当前 OAuth2 token，应 fail-closed，避免线程残留或双认证污染。

### 18.3 401/403/503

Spring Security 异常发生在 MVC ControllerAdvice 之前，不能依赖 `@RestControllerAdvice`。

新增：

```text
handler/YcrBearerAuthenticationEntryPoint.java
handler/YcrBearerAccessDeniedHandler.java
```

使用注入的 `ObjectMapper` 直接写 `R`：

| 场景 | HTTP | body |
| --- | --- | --- |
| 缺失/无效/过期 token | 401 | `R.fail(401, "未登录或登录已过期")` |
| 已认证但 URL 层拒绝 | 403 | `R.fail(403, "权限不足")` |
| Introspection 服务不可用 | 503 | `R.fail(503, "认证服务暂不可用")` |

必须保留符合 Bearer 语义的 `WWW-Authenticate` Header，响应 `Content-Type` 为 JSON，不回显底层异常和 token。

### 18.4 YCR AuthorizeAspect

OAuth2 enabled 时像 Sa-Token 一样提供：

```java
@Bean
@ConditionalOnMissingBean
AuthorizeAspect authorizeAspect(PermissionChecker permissionChecker)
```

这样业务项目不需要重复配置 `ycr.security.enabled=true`。

## 19. C8：自动配置拆分

推荐结构：

```text
autoconfigure/
├─ OAuth2ResourceServerProperties.java
├─ OAuth2ResourceServerAutoConfiguration.java       # mapper、handler、aspect、总门禁
├─ OAuth2JwtAutoConfiguration.java                  # JwtDecoder/validator
├─ OAuth2OpaqueAutoConfiguration.java               # introspector
└─ OAuth2ResourceServerWebAutoConfiguration.java    # chain/filter
```

imports 顺序：

```text
OAuth2ResourceServerAutoConfiguration
OAuth2JwtAutoConfiguration
OAuth2OpaqueAutoConfiguration
OAuth2ResourceServerWebAutoConfiguration
```

每一个 imports 中的 AutoConfiguration 都必须存在对应 `*AutoConfigurationTest`，否则现有脚本会失败。

## 20. C9：测试规格

### 20.1 属性与自动配置

必须覆盖：

- 默认关闭时无 SecurityFilterChain、JwtDecoder、OpaqueTokenIntrospector、OAuth filter、AuthorizeAspect；
- enabled + mode 缺失启动失败；
- JWT 必填项缺失分别失败；
- Opaque 必填项缺失分别失败；
- JWT 模式只创建 JWT Bean；
- Opaque 模式只创建 Opaque Bean；
- 自定义 mapper/decoder/introspector 正确 back-off；
- Sa-Token 与 OAuth2 同时 enabled 启动失败；
- 非 Servlet 应用不错误装配 Web Bean；
- authenticated/annotated 策略语义准确。

### 20.2 Claims mapper

必须覆盖：

- Number/string ID；
- 非法数字 claim 失败；
- username fallback sub；
- clientId fallback azp；
- roles/permissions 的 string/array/collection；
- scope 空格分割、scp collection；
- 去重与空值；
- 缺失可证明身份的 claim 失败；
- custom mapper。

### 20.3 JWT Web 集成

测试使用测试内生成 RSA key pair 和 Nimbus decoder/encoder，不连接外部 IdP。

至少覆盖：

- 无 token：401 + R body；
- malformed token：401；
- 错签名：401；
- issuer 错：401；
- audience 错：401；
- expired：401；
- nbf 未到：401；
- 合法 token：200，并能读取 user/tenant/client context；
- `@RequirePermission` 允许/拒绝；
- 请求完成后 Holder/MDC 清理；
- permit path 200；
- CORS preflight 200；
- 普通无 token 请求仍 401；
- `WWW-Authenticate` 存在。

### 20.4 Opaque Web 集成

用自定义测试 `OpaqueTokenIntrospector` 覆盖：

- active token 成功；
- inactive token 401；
- audience 错 401；
- introspection 异常 503；
- claims 映射及权限；
- context finally 清理。

另外对默认 HTTP introspector 使用 `MockRestServiceServer` 或等价 Spring 测试设施验证：

- Basic Auth；
- URI；
- active 响应；
- timeout/5xx fail-closed；
- client secret 不进入日志和异常 body。

### 20.5 MIXED

测试 signed header 与 OAuth token：

- userId、tenantId 一致：成功；
- userId 冲突：拒绝；
- username fallback 一致：成功；
- 无法证明同一身份：拒绝；
- tenantId 冲突：拒绝；
- signed Header 必须先通过 HMAC 与 nonce 校验，不能用裸 Header 构造测试假象。

### 20.6 测试命令

阶段内：

```bash
mvn -q -pl ycr-starter-context test
mvn -q -pl ycr-starter-auth-oauth2-resource-server -am test
./scripts/check-test-method-names.sh
./scripts/check-autoconfiguration-tests.sh
```

最终：

```bash
mvn -q clean test
git diff --check
```

## 21. C10：文档

新增 tracked 文档：

```text
docs/auth-oauth2-resource-server.md
```

内容必须包括：

- 模块职责与明确非目标；
- JWT 和 Opaque 完整配置；
- issuer/audience/algorithm/timeout 安全要求；
- claims 映射表与自定义 mapper 示例；
- endpoint policy 与 permit paths；
- 401/403/503；
- Sa-Token 与 OAuth2 互斥；
- Spring Security 仅存在于该模块；
- 与外部 Keycloak/企业 IdP 的接入边界；
- 不包含 Authorization Server。

同步：

- `README.md` 身份与安全文档索引；
- `docs/auth.md` 职责边界和互斥说明；
- `docs/starter-autoconfiguration-matrix.md`；
- 用户确认后再更新/纳入 `docs/auth-architecture-review.md`，当前不要擅自跟踪。

## 22. C11：建议提交序列

后续模型严格按以下提交拆分：

1. `refactor(context): extract identity verification and servlet binding`
2. `build(auth): add optional OAuth2 resource server module`
3. `feat(auth-oauth2): add claims mapping and configuration gates`
4. `feat(auth-oauth2): add JWT resource server validation`
5. `feat(auth-oauth2): bridge authenticated principal to YCR context`
6. `feat(auth-oauth2): add opaque token introspection`
7. `test(auth-oauth2): cover JWT opaque and mixed authentication`
8. `docs(auth-oauth2): document resource server integration`

每个提交后运行对应模块测试；第 8 个提交后运行全量门禁。

如果某一步同时需要大范围改 Context、Security 与 OAuth2，说明拆分失败，应先停下重新划分，不要提交半完成状态。

## 23. P1 Definition of Done

全部满足才算 P1 完成：

### 功能

- JWT 与 Opaque 两种模式均可运行；
- mode 显式且互斥；
- claims 可映射到完整 UserContext；
- YCR 权限注解可用；
- authenticated/annotated 两种端点策略可用。

### 安全

- JWT 校验签名、issuer、audience、exp、nbf、algorithm allowlist；
- Opaque 校验 active、audience、可选 issuer；
- Introspection 故障 fail-closed；
- 401/403/503 无敏感信息；
- CORS preflight 正常，业务请求不绕过；
- mixed 身份冲突 fail-closed；
- Holder/MDC finally 清理。

### 隔离

- Sa-Token compile tree 无 Spring Security；
- OAuth2 模块不含 Client/Authorization Server；
- 两种 auth adapter 同时启用启动失败；
- 删除 OAuth2 模块后，Sa-Token 应用仍能编译、启动、测试。

### 工程质量

- imports 自动配置均有行为测试；
- 测试方法名契约通过；
- 全量 Maven 测试通过；
- 文档与配置元数据同步；
- 工作区只包含预期 tracked 变更；
- 无 Critical / High 审查发现。

## 24. 明确延后项

以下内容不得被后续模型“顺手实现”：

- 多 issuer / 多租户 IdP 动态解析；
- Keycloak 嵌套 Claims 表达式 DSL；
- OAuth2 Client、登录跳转、SSO UI；
- Spring Authorization Server；
- Refresh Token、撤销端点、JWK 发布；
- DPoP、mTLS；
- Opaque introspection 缓存；
- Spring `@PreAuthorize` 业务授权；
- 自动把所有 GrantedAuthority 复制为业务 permission；
- 框架依赖版本升级。

## 25. 执行中的停止条件

出现任一情况必须停止并向用户报告，不能自行猜测：

1. `origin/main` 与计划基线发生非快进分叉；
2. 需要选择 Maven 发布仓库或处理凭据；
3. 需要移动/覆盖已有 RC tag；
4. JWT/Opaque 目标 IdP 的 claim 结构与本计划不兼容；
5. 用户要求多 issuer；
6. ContextFilter 与 Spring Security 实际顺序不符合集成测试；
7. 自定义 SecurityFilterChain 导致 YCR chain 不生效；
8. 全量测试存在与本次变更无关但真实的失败；
9. 需要加入当前未跟踪用户文件；
10. 出现新的 Critical / High 安全发现。

## 26. 官方技术依据

- Spring Security OAuth2 Resource Server 总览：
  https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html
- JWT Resource Server：
  https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html
- Opaque Token Resource Server：
  https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/opaque-token.html
- Spring Security 6.3.10 API：
  https://docs.spring.io/spring-security/site/docs/6.3.10/api/
- `BearerTokenAuthenticationFilter` 6.3 API：
  https://docs.spring.io/spring-security/site/docs/6.3.10/api/org/springframework/security/oauth2/server/resource/web/authentication/BearerTokenAuthenticationFilter.html

实现时以项目 Spring Boot 3.3.7 管理的 Spring Security 6.3.x API 为准，不复制 Spring Security 7.x 示例中的新 API。

---

## 27. 给后续执行模型的首条指令

可直接把以下内容交给后续模型：

> 阅读 `docs/plans/2026-08-01-rc2-and-oauth2-resource-server-plan.md` 全文。只执行 Part A，不开始 Part C。先检查当前 Git 基线、未跟踪用户文件和 `v0.9.0-RC1` 标签；按 A0-A3 完成 RC2 增量安全复审、版本修正和全部验证。任何 Critical/High、远程发布仓库选择、tag 冲突或 main 分叉都必须停止并向用户报告。不要加入未跟踪的审阅文档和图片，不要 force-push，不要移动 RC1 tag。
