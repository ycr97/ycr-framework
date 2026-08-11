# DDD 核心

> 成熟度：**Experimental**。作为可选建模工具使用，不进入默认生产底座依赖。

`ycr-starter-ddd-core` 提供 DDD 战术建模的基础构件：聚合根基类、领域事件、仓储抽象与一组语义注解。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-ddd-core</artifactId>
</dependency>
```

无配置项。`DddCoreAutoConfiguration` 以 `@ConditionalOnMissingBean` 注册 `DomainEventPublisher`。

## 语义注解

标注分层角色（均 `@Target(TYPE)`）：`@AggregateRoot`、`@Entity`、`@ValueObject`、`@DomainService`、`@ApplicationService`。其中 `@ApplicationService` 是 `@Service` 的语义化别名（被组件扫描注册为 Bean）。

## 聚合根 `AbstractAggregateRoot<ID>`

```java
public class Order extends AbstractAggregateRoot<Long> {
    private Long id;
    @Override public Long getId() { return id; }

    public static Order create(...) {
        Order o = new Order(...);
        o.registerEvent(new OrderCreatedEvent(o));   // 注册领域事件
        return o;
    }
}
```

| 方法 | 说明 |
| --- | --- |
| `getId()` | 抽象，子类返回主键 |
| `registerEvent(DomainEvent)` | `protected`，登记待发布事件 |
| `getDomainEvents()` | 只读事件列表 |
| `clearDomainEvents()` | 清空 |
| `publishEvents(DomainEventPublisher)` | 逐个发布并清空 |

## 领域事件

`DomainEvent` 继承 Spring `ApplicationEvent`（构造需传非空 `source`）。`DomainEventPublisher`：

```java
publisher.publish(event);              // 立即发布
publisher.publishAfterCommit(event);   // 事务提交后发布（无事务时回退为立即）
```

应用服务在事务方法内编排，提交后发布，`@EventListener` 消费：

```java
@ApplicationService
@RequiredArgsConstructor
public class OrderAppService {
    private final OrderRepository repository;
    private final DomainEventPublisher publisher;

    @Transactional
    public Long create(...) {
        Order order = Order.create(...);
        repository.save(order);
        order.getDomainEvents().forEach(publisher::publishAfterCommit);
        order.clearDomainEvents();
        return order.getId();
    }
}
```

## 仓储抽象 `Repository<A, ID>`

```java
public interface OrderRepository extends Repository<Order, Long> { }
// save(A) / findById(ID)->Optional / remove(A) / removeById(ID)
```

领域层只依赖该接口，实现放基础设施层（DO ↔ 聚合映射）。

## 关联示例

`ycr-scaffold-ddd` Example 的 `Product` 聚合 + `ProductRepository` + `ProductAppService` 完整演示本模块。
