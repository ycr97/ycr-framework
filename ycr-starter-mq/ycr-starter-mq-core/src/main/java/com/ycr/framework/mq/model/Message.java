package com.ycr.framework.mq.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Broker 无关的统一消息载体。
 *
 * <p>生产者实现负责把 {@link #body} 序列化为字节、把 {@link #properties} 作为消息属性透传。
 * 发送顺序消息时必须设置 {@link #messageGroup}。</p>
 *
 * @author ycr
 */
@Data
@Builder
public class Message {

    /** 目标 topic（必填） */
    private String topic;

    /** 消息 tag，消费端按 tag 过滤 */
    private String tag;

    /** 业务消息键，用于精确查找；可空 */
    private String key;

    /** 消息体对象，发送前由实现序列化 */
    private Object body;

    /** 顺序消息分组：发送顺序消息时必填，同组保证有序 */
    private String messageGroup;

    /** 附加属性，随消息透传（如 trace/tenant） */
    @Builder.Default
    private Map<String, String> properties = new HashMap<>();

    /**
     * 追加一个透传属性。
     *
     * @param name  属性名
     * @param value 属性值
     * @return this，便于链式调用
     */
    public Message addProperty(String name, String value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, value);
        return this;
    }
}
