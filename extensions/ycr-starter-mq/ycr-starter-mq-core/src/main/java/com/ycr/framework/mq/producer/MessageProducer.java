package com.ycr.framework.mq.producer;

import com.ycr.framework.mq.model.Message;

import java.time.Duration;

/**
 * 统一消息生产者 SPI（broker 无关）。
 *
 * <p>声明普通、顺序、延时三类发送能力；事务消息能力由 {@link TransactionMessageProducer} 单独声明，
 * 由支持事务的实现一并实现。</p>
 *
 * @author ycr
 */
public interface MessageProducer {

    /**
     * 发送普通消息。
     *
     * @param message 消息
     * @return 消息 ID
     */
    String send(Message message);

    /**
     * 发送顺序消息（需设置 {@link Message#getMessageGroup()}）。
     *
     * @param message 消息
     * @return 消息 ID
     */
    String sendOrderly(Message message);

    /**
     * 发送延时消息。
     *
     * @param message 消息
     * @param delay   相对当前时间的延迟时长
     * @return 消息 ID
     */
    String sendDelay(Message message, Duration delay);
}
