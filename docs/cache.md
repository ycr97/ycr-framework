# 缓存与分布式锁

`ycr-starter-cache` 基于 Redisson 提供 Redis 操作工具 `RedisUtils` 与分布式锁工具 `RedisLockUtils`。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-cache</artifactId>
</dependency>
```

> 需配置 Redis 连接（`spring.data.redis.*`）。自动配置会把容器中的 `RedissonClient` 注入静态工具类。

## 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.cache.enabled` | `true` | 是否启用缓存工具自动配置 |

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

## RedisUtils

```java
RedisUtils.set("user:1", user);                       // 永久
RedisUtils.set("captcha:abc", code, Duration.ofMinutes(5));   // 带过期
User user = RedisUtils.get("user:1");
boolean ok  = RedisUtils.delete("user:1");
boolean has = RedisUtils.exists("user:1");
RedisUtils.expire("user:1", Duration.ofHours(1));
String key  = RedisUtils.buildKey("user", "1");       // 拼接 -> "user:1"
```

## RedisLockUtils（分布式锁）

在锁内执行业务，自动获取/释放；`waitTime` 等待获取超时、`leaseTime` 持锁租约（毫秒）：

```java
// 有返回值
Order order = RedisLockUtils.tryLock("lock:order:" + id, 3000, 10000,
        () -> orderService.create(id));

// 无返回值，返回是否成功拿到锁
boolean done = RedisLockUtils.tryLock("lock:job", 0, 30000,
        () -> jobService.run());
```

## 注意

- 工具类为静态封装，依赖自动配置注入的 `RedissonClient`，不要在未引入本 starter 或未配置 Redis 时调用。
- 未抢到锁时 `tryLock` 直接返回（不抛异常）：`Supplier` 重载返回 `null`，`Runnable` 重载返回 `false`，调用方需自行判断。
