# Feign 增强

`ycr-starter-feign` 在类路径存在 Feign 时增强 OpenFeign 调用：透传上下文/TraceId、统一解码下游错误。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-feign</artifactId>
</dependency>
```

传递引入 `spring-cloud-starter-openfeign`。

## 配置

前缀 `ycr.feign`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.feign.context-pass-enabled` | `true` | 是否透传上下文/TraceId 到下游 |
| `ycr.feign.error-decoder-enabled` | `true` | 是否启用统一错误解码 |

## 透传（ContextPassInterceptor）

调用下游服务时，自动把当前用户/租户/应用上下文与 TraceId 写入 HTTP Header（`X-User-Id`、`X-Tenant-Id`、`X-Trace-Id` 等，见 [context 文档](context.md)）。下游服务开启 `ycr.context.trust-headers` 后即可还原，实现身份与链路的跨服务延续。

## 错误解码（FeignErrorDecoder）

下游返回非 2xx 时，解析其 `R` 响应体，将业务错误码/消息还原为异常抛出，避免上游只拿到无意义的 `FeignException`。

## 启用 Feign 客户端

本 starter **不**提供 `@EnableFeignClients`。消费方需自行在启动类开启，并定义/依赖 `@FeignClient` 接口：

```java
@EnableFeignClients(basePackages = "com.example.client")
@SpringBootApplication
public class Application { ... }
```

```java
@FeignClient(name = "user-service", path = "/api/users")
public interface UserApi {
    @GetMapping("/{id}")
    R<UserDTO> getById(@PathVariable("id") Long id);
}
```

> 约定：服务把对外 `@FeignClient` 契约 + DTO 放在独立的 `*-client` 模块，供下游依赖（参见 `ycr-scaffold-mvc` 的 client 模块）。服务通常不调用自身的 client。

## 透传增强

`ycr-starter-feign` 通过一组实现 feign `RequestInterceptor` 的拦截器，向下游透传跨服务上下文：

| 拦截器 | 透传内容 | 来源 | 开关 | 默认 |
|---|---|---|---|---|
| `ContextPassInterceptor` | user/tenant/app 上下文 + TraceId（分解身份） | 线程上下文持有器 | `ycr.feign.context-pass-enabled` | 开 |
| `LocalePassInterceptor` | 语言头（默认 `Accept-Language`） | 当前请求头 | `ycr.feign.locale-pass-enabled` | 开 |
| `TokenPassInterceptor` | `Authorization` 原始 token | 当前请求头 | `ycr.feign.token-pass-enabled` | 关 |

语言头名可由 `ycr.feign.language-header` 配置。原始 token 透传默认关闭——ycr 默认走分解身份，仅在确需向下游传递原始凭证时开启。

### 选择性匹配（Matchable）

三个拦截器均继承 `AbstractMatchableFeignInterceptor`，可限定「只对部分下游 client/路径生效」。通过 `RequestTemplateMatchers` 构造规则：

```java
contextPassInterceptor
    .addNotMatcher(RequestTemplateMatchers.clientName("third-party-svc")); // 不向第三方泄露内部身份头

localePassInterceptor
    .addMatcher(RequestTemplateMatchers.requestPath("/api/**"));            // 仅 /api/** 透传语言
```

匹配语义：命中任一 notMatcher 直接跳过；否则若配置了 matcher 需命中其一；未配置时按默认匹配器（全匹配）。
