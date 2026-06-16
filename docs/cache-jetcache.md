# 声明式缓存（JetCache 注解）

`ycr-starter-cache-jetcache` 基于 [JetCache](https://github.com/alibaba/jetcache) 2.7.8 提供**声明式缓存**能力：在方法上标注 `@Cached` / `@CacheInvalidate` / `@CacheUpdate` 即可自动读写缓存，支持本地（Caffeine）+ 远程（Redis）两级缓存。

与 [`ycr-starter-cache`](cache.md)（编程式 `RedisUtils` / 分布式锁）互补：

| | `ycr-starter-cache` | `ycr-starter-cache-jetcache` |
| --- | --- | --- |
| 范式 | 编程式（手动 `RedisUtils.get/set`） | 声明式（注解，AOP 自动拦截） |
| 适用 | 精细控制、复杂结构、锁 | 方法级结果缓存、读多写少 |
| 远程后端 | Redisson | **复用同一个 RedissonClient** |

> **单一 Redis 客户端**：远程后端选用 `jetcache-redisson`，直接取上下文中 `ycr-starter-cache` 已装配的 `RedissonClient`，**不新增连接池**。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-cache-jetcache</artifactId>
</dependency>
```

该 starter 传递依赖 `ycr-starter-cache`，因此引入后即同时具备编程式与声明式两种能力。需配置 Redis 连接（`spring.data.redis.*`）。

## 开箱即用

无需任何 `jetcache.*` 配置即可使用——模块通过 `EnvironmentPostProcessor` 以**最低优先级**注入了一组可被覆盖的默认值（见文末「默认配置」）。最简用法：

```java
@Service
public class UserService {

    @Cached(name = "user:", key = "#id", expire = 30, timeUnit = TimeUnit.MINUTES)
    public UserDO getById(Long id) {
        return userMapper.selectById(id);
    }

    @CacheInvalidate(name = "user:", key = "#user.id")
    public void update(UserDO user) {
        userMapper.updateById(user);
    }

    @CacheUpdate(name = "user:", key = "#user.id", value = "#user")
    public void refresh(UserDO user) {
        userMapper.updateById(user);
    }
}
```

> ⚠️ 缓存基于 AOP 代理，**自调用（同类内部方法调用）不会触发缓存**，需通过注入的 Bean 引用调用。

## 注解速览

### `@Cached`（缓存方法返回值）

| 属性 | 说明 |
| --- | --- |
| `name` | 缓存名（建议带业务前缀与冒号，如 `user:`），用于隔离与排障 |
| `key` | SpEL 键表达式，如 `#id`、`#user.id`、`#a0` |
| `expire` / `timeUnit` | 过期时间与单位 |
| `cacheType` | `REMOTE`（默认，仅 Redis）/ `LOCAL`（仅本地 Caffeine）/ `BOTH`（两级缓存） |
| `cacheNullValue` | 是否缓存 null（默认 false），开启可防穿透 |
| `condition` / `postCondition` | SpEL 条件，满足才缓存 |

```java
// 两级缓存：本地 Caffeine + 远程 Redis，热点数据降低 Redis 压力
@Cached(name = "dict:", key = "#code", cacheType = CacheType.BOTH, expire = 1, timeUnit = TimeUnit.HOURS)
public DictDO getByCode(String code) { ... }
```

### `@CacheInvalidate`（失效缓存）

写操作后删除对应键，保证一致性。

### `@CacheUpdate`（更新缓存）

写操作后用 `value`（SpEL）直接刷新缓存值，省去下次回源。

## 配置

前缀 `ycr.cache.jetcache`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.cache.jetcache.enabled` | `true` | 是否启用声明式缓存 |
| `ycr.cache.jetcache.base-packages` | `[""]`（全扫描） | 方法缓存扫描的基础包。默认全扫描（除 `java*`/`org.springframework*`/CGLIB）；**生产建议显式收窄为业务根包**（如 `com.acme`）以降低首次调用的注解解析开销 |
| `ycr.cache.jetcache.order` | 最低优先级 | 缓存切面顺序，保证位于事务等切面之内 |

```yaml
ycr:
  cache:
    jetcache:
      base-packages: com.acme.service   # 生产环境收窄扫描范围
```

> JetCache 原生的 `@EnableMethodCache#basePackages` 是编译期常量、无法从配置注入。本模块手动复刻其装配（`CacheAdvisor` + `JetCacheInterceptor`）并改为读取上述属性，从而实现属性驱动、零硬编码业务包。

### 默认配置（`jetcache.*`，均可覆盖）

模块注入的默认值如下，消费方可在 `application.yml` 中设置同名键覆盖：

| 键 | 默认值 | 说明 |
| --- | --- | --- |
| `jetcache.local.default.type` | `caffeine` | 本地一级缓存引擎（W-TinyLFU） |
| `jetcache.local.default.limit` | `100` | 本地缓存条目上限 |
| `jetcache.local.default.keyConvertor` | `jackson` | 键转换 |
| `jetcache.remote.default.type` | `redisson` | 远程后端，复用既有 RedissonClient |
| `jetcache.remote.default.keyConvertor` | `jackson` | 键转换 |
| `jetcache.remote.default.valueEncoder` / `valueDecoder` | `java` | 值序列化（JDK 内置，零额外依赖；如需更高性能可改 `kryo5`/`fastjson2`） |
| `jetcache.remote.default.keyPrefix` | `ycr:cache:` | Redis 键统一前缀 |
| `jetcache.statIntervalMinutes` | `0` | 统计日志间隔（0=关闭，按需开启可观测命中率） |

```yaml
jetcache:
  local:
    default:
      limit: 500              # 调大本地缓存容量
  remote:
    default:
      keyPrefix: "myapp:"     # 自定义键前缀
      valueEncoder: kryo5     # 切换高性能序列化（需引入对应依赖）
      valueDecoder: kryo5
  statIntervalMinutes: 15     # 开启命中率统计日志
```

## 说明与边界

- 本模块**不二次封装** JetCache 原生注解，直接启用 `@Cached` / `@CacheInvalidate` / `@CacheUpdate`，避免无意义的同义包装。
- `@CreateCache` 已被 JetCache 标记 `@Deprecated`，本模块**不启用** `EnableCreateCacheAnnotation`；如需编程式创建缓存实例，请注入 `CacheManager` 并使用 `getOrCreateCache(QuickConfig)`。
- 默认值序列化采用 JDK `java`，被缓存对象需实现 `Serializable`；追求性能或跨语言可切换 `kryo5` / `fastjson2`（自行引入相应依赖）。
