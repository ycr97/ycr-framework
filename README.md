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

- [Web 统一响应与异常处理](docs/web-response.md)
- [字段加密 MyBatis TypeHandler](docs/encrypt-typehandler.md)

## 验证

本仓库使用 Maven reactor 验证全部 starter：

```bash
mvn test
```
