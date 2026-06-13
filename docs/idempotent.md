# 幂等

`ycr-starter-idempotent` 基于 Redis 提供注解式幂等控制：在时间窗口内，相同请求只允许执行一次，拦截重复提交。需 Redis（传递依赖 `ycr-starter-cache`）。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-idempotent</artifactId>
</dependency>
```

## 配置

前缀 `ycr.idempotent`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.idempotent.enabled` | `true` | 是否启用幂等切面 |
| `ycr.idempotent.key-prefix` | `ycr:idempotent` | Redis 键前缀 |

## 用法

在方法上标 `@Idempotent`，窗口内重复请求抛 `IdempotentException`（由全局异常处理转为 `R`）：

```java
@Idempotent(timeout = 5, unit = TimeUnit.SECONDS, message = "请勿重复提交")
@PostMapping("/order")
public R<Long> create(@RequestBody @Valid OrderCreateReq req) { ... }
```

`@Idempotent` 属性：

| 属性 | 默认 | 说明 |
| --- | --- | --- |
| `name` | `""` | 幂等点名称 |
| `key` | `""` | 幂等键（标识"同一请求"的依据，如订单号、token） |
| `timeout` | `1` | 幂等窗口时长 |
| `unit` | `SECONDS` | 时间单位 |
| `message` | `请勿重复操作` | 重复时提示 |

## 与限流的区别

- **限流**（`@RateLimiter`）：控制频率（每窗口 N 次），保护后端不被压垮。
- **幂等**（`@Idempotent`）：同一操作窗口内只生效一次，防重复提交/重复下单。
