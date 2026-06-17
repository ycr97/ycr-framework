package com.ycr.framework.mq.consumer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注消费处理器，声明订阅信息。配合 {@link AbstractMessageHandler} 使用，由 broker 实现扫描并注册消费者。
 *
 * @author ycr
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqMessageListener {

    /** 订阅 topic，支持 ${} 占位符 */
    String topic();

    /** tag 过滤表达式，默认全部 */
    String tag() default "*";

    /** 消费者组，留空则取实现的全局默认组 */
    String group() default "";

    /** topic/group 是否追加环境后缀 */
    boolean enableSuffix() default true;

    /** 消费线程数 */
    int consumeThreadCount() default 20;
}
