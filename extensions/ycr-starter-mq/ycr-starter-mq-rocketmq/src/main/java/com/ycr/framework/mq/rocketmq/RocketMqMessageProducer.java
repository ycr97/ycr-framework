package com.ycr.framework.mq.rocketmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.mq.model.Message;
import com.ycr.framework.mq.producer.MessageProducer;
import com.ycr.framework.mq.producer.MessageSendException;
import com.ycr.framework.trace.util.TraceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.message.MessageBuilder;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.java.message.MessageBuilderImpl;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * RocketMQ 统一生产者实现：把 {@link Message} 构建为 rocketmq 消息并发送。
 *
 * <p>topic 按环境后缀解析；消息体经注入的 {@link ObjectMapper} 序列化为 JSON；自定义属性、当前
 * TraceId 与租户 ID 一并写入消息属性，便于消费端还原上下文。</p>
 *
 * @author ycr
 */
@Slf4j
public class RocketMqMessageProducer implements MessageProducer {

    private final Producer producer;
    private final RocketMqProperties properties;
    private final ObjectMapper objectMapper;

    public RocketMqMessageProducer(Producer producer, RocketMqProperties properties, ObjectMapper objectMapper) {
        this.producer = producer;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String send(Message message) {
        return doSend(buildMessage(message, false, null));
    }

    @Override
    public String sendOrderly(Message message) {
        if (!StringUtils.hasText(message.getMessageGroup())) {
            throw new MessageSendException("顺序消息必须设置 messageGroup");
        }
        return doSend(buildMessage(message, true, null));
    }

    @Override
    public String sendDelay(Message message, Duration delay) {
        long deliverTimestamp = System.currentTimeMillis() + delay.toMillis();
        return doSend(buildMessage(message, false, deliverTimestamp));
    }

    private String doSend(org.apache.rocketmq.client.apis.message.Message rocketMessage) {
        try {
            SendReceipt receipt = producer.send(rocketMessage);
            return receipt.getMessageId().toString();
        } catch (ClientException e) {
            throw new MessageSendException("消息发送失败", e);
        }
    }

    /**
     * 把统一消息构建为 rocketmq 消息。
     *
     * @param message          统一消息
     * @param orderly          是否顺序消息
     * @param deliverTimestamp 延时投递时间戳；非延时传 null
     * @return rocketmq 消息
     */
    org.apache.rocketmq.client.apis.message.Message buildMessage(Message message, boolean orderly, Long deliverTimestamp) {
        if (message == null || !StringUtils.hasText(message.getTopic())) {
            throw new MessageSendException("消息或 topic 为空，无法发送");
        }
        MessageBuilder builder = new MessageBuilderImpl()
                .setTopic(resolveTopic(message.getTopic()))
                .setBody(serialize(message.getBody()));
        if (StringUtils.hasText(message.getTag())) {
            builder.setTag(message.getTag());
        }
        if (StringUtils.hasText(message.getKey())) {
            builder.setKeys(message.getKey());
        }
        if (orderly) {
            builder.setMessageGroup(message.getMessageGroup());
        }
        if (deliverTimestamp != null) {
            builder.setDeliveryTimestamp(deliverTimestamp);
        }
        for (Map.Entry<String, String> entry : message.getProperties().entrySet()) {
            builder.addProperty(entry.getKey(), entry.getValue());
        }
        String traceId = TraceUtils.getTraceId();
        if (StringUtils.hasText(traceId)) {
            builder.addProperty(TraceUtils.HEADER_TRACE_ID, traceId);
        }
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            builder.addProperty(RocketMqConstants.PROPERTY_TENANT_ID, String.valueOf(tenantId));
        }
        return builder.build();
    }

    /** topic 按环境后缀解析；已带后缀或无环境时不重复追加。 */
    String resolveTopic(String topic) {
        String env = properties.getEnv();
        if (!StringUtils.hasText(env)) {
            return topic;
        }
        String suffixed = RocketMqConstants.SUFFIX + env;
        return topic.endsWith(suffixed) ? topic : topic + suffixed;
    }

    private byte[] serialize(Object body) {
        if (body instanceof String str) {
            return str.getBytes(StandardCharsets.UTF_8);
        }
        try {
            return objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            throw new MessageSendException("消息体序列化失败", e);
        }
    }
}
