package com.ycr.framework.mq.rocketmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.mq.model.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RocketMqMessageProducer 消息构建测试（不连接真实 broker）
 *
 * @author ycr
 */
class RocketMqMessageProducerTest {

    private final Producer producer = Mockito.mock(Producer.class);

    private RocketMqMessageProducer newProducer(String env) {
        RocketMqProperties properties = new RocketMqProperties();
        properties.setEnv(env);
        return new RocketMqMessageProducer(producer, properties, new ObjectMapper());
    }

    @Test
    @DisplayName("resolveTopic按环境追加后缀")
    void shouldMatchExpectedBehavior001() {
        RocketMqMessageProducer p = newProducer("prod");
        assertEquals("order-topic_prod", p.resolveTopic("order-topic"));
    }

    @Test
    @DisplayName("resolveTopic无环境时原样返回且不重复追加")
    void shouldMatchExpectedBehavior002() {
        assertEquals("order-topic", newProducer(null).resolveTopic("order-topic"));
        assertEquals("order-topic_prod", newProducer("prod").resolveTopic("order-topic_prod"));
    }

    @Test
    @DisplayName("buildMessage序列化消息体并写入tag与key")
    void shouldMatchExpectedBehavior003() {
        RocketMqMessageProducer p = newProducer(null);
        Message message = Message.builder()
                .topic("order-topic")
                .tag("created")
                .key("order-1")
                .body(new Order("o-1", 99))
                .build();

        org.apache.rocketmq.client.apis.message.Message built = p.buildMessage(message, false, null);

        assertEquals("order-topic", built.getTopic());
        assertTrue(built.getTag().isPresent());
        assertEquals("created", built.getTag().get());
        String body = StandardCharsets.UTF_8.decode(built.getBody()).toString();
        assertTrue(body.contains("o-1"));
        assertTrue(body.contains("99"));
    }

    @Test
    @DisplayName("buildMessage透传自定义属性")
    void shouldMatchExpectedBehavior004() {
        RocketMqMessageProducer p = newProducer(null);
        Message message = Message.builder().topic("t").body("x").build()
                .addProperty("bizKey", "v1");

        org.apache.rocketmq.client.apis.message.Message built = p.buildMessage(message, false, null);
        assertEquals("v1", built.getProperties().get("bizKey"));
    }

    record Order(String id, int amount) {
    }
}
