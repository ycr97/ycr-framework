# 限流

`ycr-starter-ratelimiter` 基于 Redisson 令牌桶提供注解式限流。需 Redis（传递依赖 `ycr-starter-cache`）。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-ratelimiter</artifactId>
</dependency>
```

## 配置

前缀 `ycr.ratelimiter`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.ratelimiter.enabled` | `false` | 是否启用限流切面，须显式开启 |
| `ycr.ratelimiter.key-prefix` | `ycr:ratelimiter` | Redis 键前缀 |

使用 `@RateLimiter` 前必须配置 `ycr.ratelimiter.enabled=true`。

## 用法

在方法上标 `@RateLimiter`，超限抛 `RateLimiterException`（由全局异常处理转为 `R`）：

```java
@RateLimiter(rate = 10, interval = 1, unit = TimeUnit.SECONDS)   // 每秒最多 10 次
@PostMapping("/sms")
public R<Void> sendSms(@RequestBody SmsReq req) { ... }

@RateLimiter(type = LimitType.IP, rate = 5, interval = 60, message = "请求过于频繁")
@GetMapping("/query")
public R<?> query() { ... }
```

`@RateLimiter` 属性：

| 属性 | 默认 | 说明 |
| --- | --- | --- |
| `type` | `DEFAULT` | 限流维度，见下表 |
| `name` | `""` | 限流器名称（区分不同限流点） |
| `key` | `""` | 限流键（支持按业务维度细分） |
| `rate` | `Integer.MAX_VALUE` | 时间窗口内许可数 |
| `interval` | `0` | 时间窗口长度 |
| `unit` | `SECONDS` | 时间单位 |
| `message` | `操作过于频繁，请稍后再试` | 超限提示 |

限流维度 `LimitType`：

| 取值 | 说明 |
| --- | --- |
| `DEFAULT` | 全局共享一个令牌桶（Redisson OVERALL） |
| `IP` | 按客户端 IP 各自限流 |
| `CLUSTER` | 按 Redisson 客户端实例限流（PER_CLIENT） |
