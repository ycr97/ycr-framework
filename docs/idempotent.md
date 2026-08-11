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
| `ycr.idempotent.enabled` | `false` | 是否启用幂等切面，须显式开启 |
| `ycr.idempotent.key-prefix` | `ycr:idempotent` | Redis 键前缀 |

使用 `@Idempotent` 前必须配置 `ycr.idempotent.enabled=true`。

## 用法

在方法上标 `@Idempotent`，窗口内重复请求抛 `IdempotentException`（由全局异常处理转为 `R`）：

```java
@Idempotent(key = "#req.requestId", timeout = 5, unit = TimeUnit.SECONDS, message = "请勿重复提交")
@PostMapping("/order")
public R<Long> create(@RequestBody @Valid OrderCreateReq req) { ... }
```

`@Idempotent` 属性：

| 属性 | 默认 | 说明 |
| --- | --- | --- |
| `name` | `""` | 幂等点名称 |
| `key` | `""` | 请求键（标识"同一请求"的依据），必须显式配置且求值非空 |
| `timeout` | `1` | 幂等窗口时长 |
| `unit` | `SECONDS` | 时间单位 |
| `message` | `请勿重复操作` | 重复时提示 |

## 与限流的区别

- **限流**（`@RateLimiter`）：控制频率（每窗口 N 次），保护后端不被压垮。
- **防重复提交**（当前 `@Idempotent`）：同一操作窗口内只放行一次，不保存或重放业务结果。

占位值使用每次请求的唯一 owner token；业务失败时只会原子删除自己的占位，不会误删 TTL 到期后新请求创建的占位。

## 从 middle-common NoRepeatSubmit 迁移

middle-common 的 `@NoRepeatSubmit(lockTime)` 防重复提交能力已由本模块的 `@Idempotent` 完全覆盖且更强：

| NoRepeatSubmit | @Idempotent |
|---|---|
| `lockTime` 固定锁定秒数 | `timeout` + `unit` 时间窗口 |
| 无自定义键 | `key` 支持 SpEL（如按订单 `#orderId` 防重） |
| 无 | `name` 幂等名、`message` 自定义提示 |

迁移：`@NoRepeatSubmit(lockTime = 10)` → `@Idempotent(key = "#requestId", timeout = 10)`。
