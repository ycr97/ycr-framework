package com.ycr.framework.mq.producer;

/**
 * 消息发送异常。
 *
 * @author ycr
 */
public class MessageSendException extends RuntimeException {

    public MessageSendException(String message) {
        super(message);
    }

    public MessageSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
