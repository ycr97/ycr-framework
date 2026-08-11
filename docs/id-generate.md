# 分布式 ID 生成

`ycr-starter-id-generate` 提供基于雪花算法的 `IdGenerator` Bean，生成全局唯一、趋势递增的 64 位 ID。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-id-generate</artifactId>
</dependency>
```

## 配置

前缀 `ycr.id`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.id.enabled` | `false` | 是否启用默认雪花 ID 生成器 |
| `ycr.id.worker-id` | 无 | 工作机器 ID（0~31），启用时必填 |
| `ycr.id.datacenter-id` | 无 | 数据中心 ID（0~31），启用时必填 |

> 多实例部署时必须为每个实例分配**不同**的 `worker-id`/`datacenter-id` 组合。启用后未显式配置节点号时，应用将拒绝启动。

```yaml
ycr:
  id:
    enabled: true
    worker-id: 2
    datacenter-id: 3
```

## 用法

注入 `IdGenerator`：

```java
@RequiredArgsConstructor
@Service
public class OrderService {
    private final IdGenerator idGenerator;

    public void create() {
        long id = idGenerator.nextId();
        String idStr = idGenerator.nextIdStr();   // 字符串形式
    }
}
```

自动配置以 `@ConditionalOnMissingBean` 注册 `SnowflakeIdGenerator`，应用可注册自己的 `IdGenerator` 覆盖。

## 与 MyBatis-Plus 主键的关系

实体用 `@TableId(type = IdType.ASSIGN_ID)` 时，由 MyBatis-Plus 内置雪花生成主键，**无需**本 starter。本 starter 适用于需要在业务代码中**显式**获取 ID 的场景（如先生成 ID 再做关联、消息幂等键等）。
