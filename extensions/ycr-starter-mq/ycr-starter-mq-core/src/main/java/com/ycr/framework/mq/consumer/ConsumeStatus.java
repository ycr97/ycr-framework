package com.ycr.framework.mq.consumer;

/**
 * 消费结果。
 *
 * @author ycr
 */
public enum ConsumeStatus {

    /** 消费成功，提交位点 */
    SUCCESS,

    /** 消费失败，触发重试 */
    FAILURE
}
