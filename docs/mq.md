# 统一消息（ycr-starter-mq）

`ycr-starter-mq` 提供 broker 无关的消息 SPI，`ycr-starter-mq-rocketmq` 是其 Apache RocketMQ 实现。

## 模块

| 模块 | 职责 |
|---|---|
| `ycr-starter-mq-core` | SPI：`Message` 载体、`MessageProducer`/`TransactionMessageProducer`、消费注解 `@MqMessageListener` + `MessageContext` + `AbstractMessageHandler` |
| `ycr-starter-mq-rocketmq` | RocketMQ 5.x 实现：生产者、注解式消费者注册、自动配置 |

## 生产消息

```java
@Autowired
MessageProducer producer;

producer.send(Message.builder().topic("order-topic").tag("created").key("order-1").body(order).build());
producer.sendOrderly(Message.builder().topic("order-topic").messageGroup("order-1").body(order).build());
producer.sendDelay(Message.builder().topic("order-topic").body(order).build(), Duration.ofMinutes(10));
```

发送时自动把当前 TraceId 与租户 ID 写入消息属性。

## 消费消息

```java
@Component
@MqMessageListener(topic = "order-topic", tag = "created", group = "order-consumer")
public class OrderCreatedHandler extends AbstractMessageHandler {
    @Override
    public void handle(MessageContext context) {
        String body = context.getBody();
        // 处理；抛异常即消费失败触发重试
    }
}
```

消费前自动从消息属性还原 TraceId / 租户上下文，消费后清理。

## 配置

```yaml
ycr:
  mq:
    rocketmq:
      enabled: true          # 默认 false，关闭时不装配任何组件
      endpoints: "rmq.example.com:8080"
      access-key: "ak"
      secret-key: "sk"
      group: "default-consumer-group"
      env: "prod"            # 非空时 topic/group 追加 _prod 后缀
      max-attempts: 3
```

> ⚠️ **关于环境后缀（env）与 `@MqMessageListener(enableSuffix)`**：
> 配置了 `env` 后，**生产端发送时一律追加后缀**（如 `order-topic` → `order-topic_prod`）。
> 消费端默认 `enableSuffix = true`，会订阅同样带后缀的 topic，两端一致。
> 但若某监听器显式设 `@MqMessageListener(enableSuffix = false)` 而全局又配了 `env`，
> 消费端将订阅不带后缀的 `order-topic`，与生产端的 `order-topic_prod` **对不上、收不到消息**。
> 因此：要么全局不配 `env`，要么生产/消费两端都启用后缀（默认），不要在配了 `env` 时单独关掉某监听器的 `enableSuffix`。

## 扩展其它 broker

实现 `MessageProducer`（及可选 `TransactionMessageProducer`）与消费者注册逻辑，作为 `ycr-starter-mq` 下的新子模块（如 `ycr-starter-mq-kafka`），复用同一 SPI。
