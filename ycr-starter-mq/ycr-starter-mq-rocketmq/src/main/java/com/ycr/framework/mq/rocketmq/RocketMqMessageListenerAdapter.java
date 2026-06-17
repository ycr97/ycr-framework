package com.ycr.framework.mq.rocketmq;

import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.model.TenantContext;
import com.ycr.framework.mq.consumer.AbstractMessageHandler;
import com.ycr.framework.mq.consumer.MessageContext;
import com.ycr.framework.trace.util.TraceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 把 rocketmq {@link MessageView} 适配为统一 {@link MessageContext} 并交给处理器，
 * 消费前从消息属性还原 TraceId / 租户上下文，消费后清理。
 *
 * @author ycr
 */
@Slf4j
public class RocketMqMessageListenerAdapter implements MessageListener {

    private final AbstractMessageHandler handler;

    public RocketMqMessageListenerAdapter(AbstractMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public ConsumeResult consume(MessageView messageView) {
        Map<String, String> properties = new HashMap<>(messageView.getProperties());
        bindContext(properties);
        try {
            handler.handle(adapt(messageView, properties));
            return ConsumeResult.SUCCESS;
        } catch (Exception e) {
            log.error("消息消费失败: topic={}, messageId={}", messageView.getTopic(), messageView.getMessageId(), e);
            return ConsumeResult.FAILURE;
        } finally {
            TenantContextHolder.clear();
            TraceUtils.removeTraceId();
        }
    }

    private void bindContext(Map<String, String> properties) {
        String traceId = properties.get(TraceUtils.HEADER_TRACE_ID);
        if (StringUtils.hasText(traceId)) {
            TraceUtils.setTraceId(traceId);
        }
        String tenantId = properties.get(RocketMqConstants.PROPERTY_TENANT_ID);
        if (StringUtils.hasText(tenantId)) {
            TenantContext tenantContext = new TenantContext();
            tenantContext.setTenantId(Long.valueOf(tenantId));
            TenantContextHolder.set(tenantContext);
        }
    }

    private MessageContext adapt(MessageView messageView, Map<String, String> properties) {
        String body = StandardCharsets.UTF_8.decode(messageView.getBody()).toString();
        String tag = messageView.getTag().orElse(null);
        return new MessageContext() {
            @Override public String getMessageId() { return messageView.getMessageId().toString(); }
            @Override public String getTopic() { return messageView.getTopic(); }
            @Override public String getTag() { return tag; }
            @Override public String getBody() { return body; }
            @Override public Map<String, String> getProperties() { return properties; }
            @Override public int getDeliveryAttempt() { return messageView.getDeliveryAttempt(); }
        };
    }
}
