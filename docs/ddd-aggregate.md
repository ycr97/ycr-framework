# DDD 聚合持久化（变更检测）

`ycr-starter-ddd-core` 的 `com.ycr.framework.ddd.aggregate` 包提供聚合持久化辅助：把聚合根包进 `Aggregate`，构造时对其做**深拷贝快照**；业务修改后，通过**深比较**自动识别新增 / 变更 / 删除的子实体，供仓储层精准落库。

> 移植自开源项目 meixuesong/aggregate-persistence 与 cedarsoftware/java-util（Apache-2.0），仅做包名适配。

## 用法

聚合根需实现 `Versionable`（用 version 管理乐观锁与「是否新建」），默认深拷贝器 `SerializableDeepCopier` 要求聚合可序列化（实现 `Serializable`）。

```java
Aggregate<Order> agg = AggregateFactory.createAggregate(order);
Order root = agg.getRoot();
root.doSomething();

if (agg.isNew()) { /* 全量插入 */ }
Collection<Item> created = agg.findNewEntitiesById(Order::getItems, Item::getId);
Collection<Item> changed = agg.findChangedEntities(Order::getItems, Item::getId);
Collection<Item> removed = agg.findRemovedEntities(Order::getItems, Item::getId);
```

## 字段级 delta

`DataObjectUtils` 比较两个对象，产出「仅含变更字段、未变更字段为 null」的 delta 对象，或变更字段名集合：

```java
SampleDO delta = DataObjectUtils.getDelta(old, current);
Set<String> fields = DataObjectUtils.getChangedFields(old, current);
```

## 深拷贝器选择

- `SerializableDeepCopier`（默认）：基于 commons-lang3，聚合需 `Serializable`；适合没有默认构造器或 setter 的实体模型。
- `JsonDeepCopier`：基于 Jackson，聚合需满足 Jackson 序列化 / 反序列化约束；可经 `AggregateFactory.setCopier(...)` 全局替换。
