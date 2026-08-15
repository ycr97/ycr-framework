package com.ycr.framework.mq.rocketmq;

import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.model.TenantContext;
import com.ycr.framework.mq.consumer.AbstractMessageHandler;
import com.ycr.framework.mq.consumer.MessageContext;
import com.ycr.framework.trace.util.TraceUtils;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageId;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RocketMqMessageListenerAdapterTest {

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
        TraceUtils.removeTraceId();
        TraceUtils.removeRequestId();
    }

    @Test
    @DisplayName("消费时应绑定消息上下文并在结束后清理")
    void shouldBindAndClearMessageContext() {
        AtomicReference<ObservedContext> observed = new AtomicReference<>();
        AbstractMessageHandler handler = new AbstractMessageHandler() {
            @Override
            public void handle(MessageContext context) {
                observed.set(new ObservedContext(
                        TenantContextHolder.getTenantId(), TraceUtils.getTraceId(), context.getBody()));
            }
        };
        RocketMqMessageListenerAdapter adapter = new RocketMqMessageListenerAdapter(handler);

        ConsumeResult result = adapter.consume(message(Map.of(
                RocketMqConstants.PROPERTY_TENANT_ID, "1001",
                TraceUtils.HEADER_TRACE_ID, "trace-message")));

        assertThat(result).isEqualTo(ConsumeResult.SUCCESS);
        assertThat(observed.get()).isEqualTo(new ObservedContext(1001L, "trace-message", "body"));
        assertThat(TenantContextHolder.get()).isNull();
        assertThat(TraceUtils.getTraceId()).isNull();
    }

    @Test
    @DisplayName("租户号非法时应返回消费失败并清理旧上下文")
    void shouldFailAndClearStaleContextWhenTenantIdIsInvalid() {
        TenantContext staleTenant = new TenantContext();
        staleTenant.setTenantId(999L);
        TenantContextHolder.set(staleTenant);
        TraceUtils.setTraceId("stale-trace");
        RocketMqMessageListenerAdapter adapter = new RocketMqMessageListenerAdapter(new AbstractMessageHandler() {
            @Override
            public void handle(MessageContext context) {
                throw new AssertionError("不应执行业务处理器");
            }
        });

        ConsumeResult result = adapter.consume(message(Map.of(
                RocketMqConstants.PROPERTY_TENANT_ID, "invalid",
                TraceUtils.HEADER_TRACE_ID, "new-trace")));

        assertThat(result).isEqualTo(ConsumeResult.FAILURE);
        assertThat(TenantContextHolder.get()).isNull();
        assertThat(TraceUtils.getTraceId()).isNull();
    }

    private MessageView message(Map<String, String> properties) {
        MessageView message = mock(MessageView.class);
        MessageId messageId = mock(MessageId.class);
        when(messageId.toString()).thenReturn("message-1");
        when(message.getMessageId()).thenReturn(messageId);
        when(message.getTopic()).thenReturn("orders");
        when(message.getProperties()).thenReturn(properties);
        when(message.getBody()).thenReturn(ByteBuffer.wrap("body".getBytes(StandardCharsets.UTF_8)));
        when(message.getTag()).thenReturn(Optional.empty());
        when(message.getDeliveryAttempt()).thenReturn(1);
        return message;
    }

    private record ObservedContext(Long tenantId, String traceId, String body) {
    }
}
