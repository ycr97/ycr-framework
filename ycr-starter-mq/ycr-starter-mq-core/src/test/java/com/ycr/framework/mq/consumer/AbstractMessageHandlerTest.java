package com.ycr.framework.mq.consumer;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * AbstractMessageHandler / 注解 / 上下文契约测试
 *
 * @author ycr
 */
class AbstractMessageHandlerTest {

    /** 简单的测试消息上下文 */
    static class SimpleContext implements MessageContext {
        private final String body;

        SimpleContext(String body) {
            this.body = body;
        }

        @Override public String getMessageId() { return "id-1"; }
        @Override public String getTopic() { return "t"; }
        @Override public String getTag() { return "tag"; }
        @Override public String getBody() { return body; }
        @Override public Map<String, String> getProperties() { return Map.of(); }
        @Override public int getDeliveryAttempt() { return 1; }
    }

    @MqMessageListener(topic = "order-topic", tag = "created", group = "g1")
    static class RecordingHandler extends AbstractMessageHandler {
        String received;

        @Override
        public void handle(MessageContext context) {
            received = context.getBody();
        }
    }

    @Test
    void 处理器接收消息体() throws Exception {
        RecordingHandler handler = new RecordingHandler();
        handler.handle(new SimpleContext("hello"));
        assertEquals("hello", handler.received);
    }

    @Test
    void 注解可解析订阅信息() {
        MqMessageListener annotation = RecordingHandler.class.getAnnotation(MqMessageListener.class);
        assertEquals("order-topic", annotation.topic());
        assertEquals("created", annotation.tag());
        assertEquals("g1", annotation.group());
        assertEquals(true, annotation.enableSuffix());
    }

    @Test
    void 处理异常向外抛出() {
        AbstractMessageHandler failing = new AbstractMessageHandler() {
            @Override
            public void handle(MessageContext context) {
                throw new IllegalStateException("boom");
            }
        };
        assertThrows(IllegalStateException.class, () -> failing.handle(new SimpleContext("x")));
    }
}
