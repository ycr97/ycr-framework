package com.ycr.framework.mq.rocketmq;

/**
 * RocketMQ 实现内部共享常量。
 *
 * <p>集中生产端与消费端必须保持一致的约定：环境后缀分隔符、上下文透传属性键，
 * 避免在多个类中各写一份字面量导致改一处漏一处。</p>
 *
 * @author ycr
 */
final class RocketMqConstants {

    private RocketMqConstants() {
    }

    /** topic/group 环境后缀分隔符，最终形如 {@code topic_env} */
    static final String SUFFIX = "_";

    /** 租户 ID 透传属性键：生产端写入、消费端还原，两端必须一致 */
    static final String PROPERTY_TENANT_ID = "tenantId";
}
