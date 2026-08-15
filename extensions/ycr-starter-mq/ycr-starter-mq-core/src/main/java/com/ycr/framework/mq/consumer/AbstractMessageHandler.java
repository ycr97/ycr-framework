package com.ycr.framework.mq.consumer;

/**
 * 业务消费处理器基类：实现 {@link #handle(MessageContext)} 处理消息。配合类级 {@link MqMessageListener} 声明订阅。
 *
 * <p>抛出异常表示消费失败，由 broker 实现捕获并转换为 {@link ConsumeStatus#FAILURE} 触发重试。</p>
 *
 * @author ycr
 */
public abstract class AbstractMessageHandler {

    /**
     * 处理一条消息。
     *
     * @param context 消息上下文
     * @throws Exception 处理失败
     */
    public abstract void handle(MessageContext context) throws Exception;
}
