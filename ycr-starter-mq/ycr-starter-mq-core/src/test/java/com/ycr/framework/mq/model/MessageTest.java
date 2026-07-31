package com.ycr.framework.mq.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Message 载体构建测试
 *
 * @author ycr
 */
class MessageTest {

    @Test
    @DisplayName("builder构建并默认空属性表")
    void shouldMatchExpectedBehavior001() {
        Message message = Message.builder()
                .topic("order-topic")
                .tag("created")
                .key("order-1")
                .body("payload")
                .build();

        assertEquals("order-topic", message.getTopic());
        assertEquals("created", message.getTag());
        assertEquals("order-1", message.getKey());
        assertEquals("payload", message.getBody());
        assertTrue(message.getProperties().isEmpty());
    }

    @Test
    @DisplayName("addProperty追加属性并支持链式")
    void shouldMatchExpectedBehavior002() {
        Message message = Message.builder().topic("t").build()
                .addProperty("tenantId", "100")
                .addProperty("traceId", "abc");

        assertEquals("100", message.getProperties().get("tenantId"));
        assertEquals("abc", message.getProperties().get("traceId"));
    }
}
