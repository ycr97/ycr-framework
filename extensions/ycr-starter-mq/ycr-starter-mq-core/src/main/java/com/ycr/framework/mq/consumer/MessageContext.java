package com.ycr.framework.mq.consumer;

import java.util.Map;

/**
 * 消费侧消息上下文（broker 无关）。各 broker 实现把自身消息视图适配为本结构后交给处理器。
 *
 * @author ycr
 */
public interface MessageContext {

    /** 消息 ID */
    String getMessageId();

    /** topic */
    String getTopic();

    /** tag */
    String getTag();

    /** 消息体（UTF-8 文本） */
    String getBody();

    /** 消息属性 */
    Map<String, String> getProperties();

    /** 当前投递次数（从 1 开始） */
    int getDeliveryAttempt();
}
