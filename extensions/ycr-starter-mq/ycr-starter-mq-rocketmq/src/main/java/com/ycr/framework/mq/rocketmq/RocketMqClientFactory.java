package com.ycr.framework.mq.rocketmq;

import com.ycr.framework.mq.consumer.AbstractMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.StaticSessionCredentialsProvider;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.producer.Producer;

import java.util.Collections;

/**
 * RocketMQ 客户端工厂：集中构建连接配置、生产者与 PushConsumer。
 *
 * @author ycr
 */
@Slf4j
public class RocketMqClientFactory {

    private final RocketMqProperties properties;

    public RocketMqClientFactory(RocketMqProperties properties) {
        this.properties = properties;
    }

    private ClientConfiguration clientConfiguration() {
        return ClientConfiguration.newBuilder()
                .setEndpoints(properties.getEndpoints())
                .setCredentialProvider(new StaticSessionCredentialsProvider(
                        properties.getAccessKey(), properties.getSecretKey()))
                .build();
    }

    /**
     * 构建生产者。
     *
     * @return 生产者
     */
    public Producer createProducer() {
        try {
            ClientServiceProvider provider = ClientServiceProvider.loadService();
            Producer producer = provider.newProducerBuilder()
                    .setClientConfiguration(clientConfiguration())
                    .setMaxAttempts(properties.getMaxAttempts())
                    .build();
            log.info("RocketMQ 生产者初始化成功");
            return producer;
        } catch (ClientException e) {
            throw new IllegalStateException("RocketMQ 生产者初始化失败", e);
        }
    }

    /**
     * 为一个处理器构建并启动 PushConsumer。
     *
     * @param handler     消费处理器
     * @param group       消费者组
     * @param topic       订阅 topic
     * @param tag         tag 过滤表达式
     * @param threadCount 消费线程数
     * @return PushConsumer
     */
    public PushConsumer createPushConsumer(AbstractMessageHandler handler, String group, String topic,
                                           String tag, int threadCount) {
        try {
            ClientServiceProvider provider = ClientServiceProvider.loadService();
            FilterExpression filter = new FilterExpression(tag, FilterExpressionType.TAG);
            return provider.newPushConsumerBuilder()
                    .setClientConfiguration(clientConfiguration())
                    .setConsumerGroup(group)
                    .setSubscriptionExpressions(Collections.singletonMap(topic, filter))
                    .setMessageListener(new RocketMqMessageListenerAdapter(handler))
                    .setConsumptionThreadCount(threadCount)
                    .build();
        } catch (ClientException e) {
            throw new IllegalStateException("RocketMQ 消费者初始化失败: topic=" + topic, e);
        }
    }
}
