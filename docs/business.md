# 业务接入点（拦截器链）

> 成熟度：**Experimental**。本模块不进入默认生产底座依赖，公开契约在 1.0 前可能调整。

`ycr-starter-business` 为标注 `@BizApi` 的业务方法提供统一的拦截器链：在方法执行前后及异常时插入横切逻辑（审计、风控、埋点、统一回退等），与 Web 层 AOP 解耦。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-business</artifactId>
</dependency>
```

## 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.business.enabled` | `true` | 是否启用业务接入点切面 |

## 用法

### 1. 标记业务方法

```java
@BizApi("下单")
public Order createOrder(OrderCmd cmd) { ... }
```

`BizApiAspect` 环绕该方法，构造 `BizContext` 交由 `BizInterceptorChain` 管控前置/后置/异常。

### 2. 实现拦截器

注册 `BizInterceptor` Bean（`getOrder()` 定序，方法均为 `default` 可按需重写）：

```java
@Component
public class AuditBizInterceptor implements BizInterceptor {
    @Override public int getOrder() { return 100; }

    @Override public void before(BizContext ctx) {
        // ctx.getName() / getMethod() / getArgs() / getBizApi() / 自定义 attributes
    }
    @Override public void after(BizContext ctx)  { /* ctx.getResult() */ }
    @Override public void onError(BizContext ctx, Throwable error) { /* 统一异常处理 */ }
}
```

`BizContext` 贯穿一次调用：`getName()`（取自 `@BizApi.value`）、`getMethod()`、`getTarget()`、`getArgs()/setArgs()`、`getResult()/setResult()`、`getError()`、以及 `getAttribute/setAttribute` 供拦截器间传值。

## 与其他切面的区别

- `@Log`（[log](log.md)）：聚焦操作日志采集，面向 Controller。
- `@BizApi`：通用业务横切链，面向**领域/应用层**业务方法，多个拦截器按序组合，可读写参数与结果。
