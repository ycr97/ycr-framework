# YCR Framework

YCR Framework 是基于 Spring Boot 3.x 的企业级 starter 集合。当前重点能力包括统一响应、全局异常、上下文透传、认证、安全工具、MyBatis-Plus 增强和字段加密。

## 快速接入

业务服务按需引入 starter：

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-web</artifactId>
    <version>${ycr-framework.version}</version>
</dependency>

<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-encrypt</artifactId>
    <version>${ycr-framework.version}</version>
</dependency>
```

建议通过 BOM 统一版本：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.ycr.framework</groupId>
            <artifactId>ycr-framework-bom</artifactId>
            <version>${ycr-framework.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 使用文档

**基础**
- [Starter 生产级装配基线](docs/starter-production-baseline.md)
- [核心基础（响应 / 异常 / 枚举 / 工具）](docs/core.md)
- [通用纯类库（树工具 / 校验分组 / 通用 DTO）](docs/common.md)
- [JSON（Jackson 扩展）](docs/json.md)
- [校验约束扩展](docs/validation.md)
- [Web 统一响应与异常处理](docs/web-response.md)
- [Web CORS 跨域配置](docs/web-cors.md)
- [接口文档（Knife4j / SpringDoc）](docs/api-doc.md)

**数据**
- [MyBatis-Plus 数据访问增强](docs/data-mp.md)
- [通用 CRUD 自动端点](docs/crud.md)
- [数据权限（行级过滤）](docs/data-permission.md)
- [多租户](docs/tenant.md)
- [分布式 ID 生成](docs/id-generate.md)

**缓存与防护**
- [缓存与分布式锁](docs/cache.md)
- [声明式缓存（JetCache 注解）](docs/cache-jetcache.md)
- [限流](docs/ratelimiter.md)
- [幂等](docs/idempotent.md)

**身份与安全**
- [请求上下文透传](docs/context.md)
- [认证（Sa-Token 集成）](docs/auth.md)
- [认证（OAuth2 Resource Server：JWT / Opaque Token）](docs/auth-oauth2-resource-server.md)
- [OAuth2 Resource Server P1 安全审查](docs/security-audit-oauth2-p1.md)
- [安全（注解鉴权 + 端点放行）](docs/security.md)
- [数据防护（脱敏 + XSS）](docs/protect.md)
- [字段加密 MyBatis TypeHandler](docs/encrypt-typehandler.md)

**可观测**
- [可观测性标准](docs/observability.md)
- [操作日志](docs/log.md)
- [链路追踪（TraceId）](docs/trace.md)

**业务增强**
- [字段翻译](docs/translate.md)
- [Excel 导出](docs/excel.md)
- [图形验证码](docs/captcha.md)
- [国际化（i18n）](docs/i18n.md)
- [业务接入点（拦截器链）](docs/business.md)

**微服务与集成**
- [Feign 增强](docs/feign.md)
- [SDK 发布](docs/sdk.md)
- [消息（邮件）](docs/messaging.md)
- [统一消息（MQ / RocketMQ）](docs/mq.md)
- [文件存储](docs/storage.md)

**DDD**
- [DDD 核心](docs/ddd-core.md)
- [DDD 聚合持久化（变更检测）](docs/ddd-aggregate.md)
- [DDD 扩展点](docs/ddd-extension.md)
- [DDD 状态机](docs/ddd-statemachine.md)

> 可运行的完整用法可参考 `ycr-scaffold-ddd`、`ycr-scaffold-mvc` 两套脚手架示例。

## 验证

本仓库使用 Maven reactor 验证全部 starter：

```bash
mvn test
```

## Maven 配置

仓库内置项目级 [settings.xml](settings.xml)，并通过 [.mvn/maven.config](.mvn/maven.config) 自动启用：

```text
--settings=settings.xml
```

当前 settings 只配置 Java 17 编译属性和源码/Javadoc 下载属性，不包含 Maven 私服、mirror 或凭据。后续需要接入私服时，在该文件中补充 `servers`、`mirrors` 和对应 profile。
