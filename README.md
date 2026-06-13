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

**Web 与数据**
- [Web 统一响应与异常处理](docs/web-response.md)
- [MyBatis-Plus 数据访问增强](docs/data-mp.md)
- [通用 CRUD 自动端点](docs/crud.md)
- [缓存与分布式锁](docs/cache.md)

**身份与安全**
- [请求上下文透传](docs/context.md)
- [认证（Sa-Token 集成）](docs/auth.md)
- [数据权限（行级过滤）](docs/data-permission.md)
- [字段加密 MyBatis TypeHandler](docs/encrypt-typehandler.md)

**业务增强**
- [字段翻译](docs/translate.md)
- [Excel 导出](docs/excel.md)

> 其余 starter 的使用文档逐步补充中；可运行的完整用法可参考 `ycr-scaffold-ddd`、`ycr-scaffold-mvc` 两套脚手架示例。

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
