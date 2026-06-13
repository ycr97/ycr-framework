# DDD 状态机

`ycr-starter-ddd-statemachine` 提供 COLA 风格的轻量状态机：流式 DSL 声明状态转换，`build` 一次成型为**不可变、线程安全**的状态机，适合为聚合的状态流转建模。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-ddd-statemachine</artifactId>
</dependency>
```

无配置项。三个泛型：`S` 状态、`E` 事件、`C` 上下文。

## 构建

```java
StateMachineBuilder<OrderStatus, OrderEvent, OrderContext> builder =
        StateMachineBuilderFactory.create();

builder.externalTransition()
        .from(OrderStatus.INIT).to(OrderStatus.PAID).on(OrderEvent.PAY)
        .when(ctx -> ctx.isPaid())          // 可选条件
        .perform((from, to, event, ctx) -> log.info("支付完成"));   // 可选动作

StateMachine<OrderStatus, OrderEvent, OrderContext> sm = builder.build("orderSM");
```

DSL：`externalTransition().from(S).to(S).on(E).when(Condition).perform(Action)`。`when`/`perform` 可省略（无条件转换 / 无副作用）。

## 触发

```java
OrderStatus next = sm.fireEvent(OrderStatus.INIT, OrderEvent.PAY, ctx);
boolean ok       = sm.verify(OrderStatus.INIT, OrderEvent.PAY);   // 是否存在该转换
String id        = sm.getMachineId();
```

`fireEvent` 在源状态未定义该事件转换、或条件不满足时抛 `BizException`（状态流转被拒，HTTP 400）。

## 在聚合中使用

把状态机声明为聚合可见的静态不可变实例，聚合方法委托其推进状态：

```java
public class Product extends AbstractAggregateRoot<Long> {
    private ProductStatus status;
    public void putaway() {
        this.status = ProductStateMachine.SM.fireEvent(status, ProductEvent.PUTAWAY, null);
    }
}
```

## 注意

- `build` 校验同「(源状态, 事件)」不得存在多条无条件转换，否则抛 `SysException`（配置错误）。
- `build` **不会**自动注册到 `StateMachineFactory`；需要全局共享时显式 `StateMachineFactory.register`。

## 关联示例

`ycr-scaffold-ddd` Example 的 `ProductStateMachine`（DRAFT→ON_SALE→OFF_SALE）。
