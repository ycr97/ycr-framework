package com.ycr.framework.mq.producer;

import com.ycr.framework.mq.model.Message;

import java.util.function.Function;

/**
 * 事务消息能力 SPI。支持事务消息的实现额外实现本接口。
 *
 * @author ycr
 */
public interface TransactionMessageProducer {

    /**
     * 发送事务消息：先发半消息，执行本地事务后按返回值提交或回滚。
     *
     * @param message          消息
     * @param localTransaction 本地事务回调，返回 true 提交、false 回滚
     * @return 消息 ID
     */
    String sendInTransaction(Message message, Function<Message, Boolean> localTransaction);
}
