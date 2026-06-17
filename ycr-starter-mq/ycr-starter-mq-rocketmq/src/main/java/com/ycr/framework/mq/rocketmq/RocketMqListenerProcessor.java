package com.ycr.framework.mq.rocketmq;

import com.ycr.framework.mq.consumer.AbstractMessageHandler;
import com.ycr.framework.mq.consumer.MqMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 扫描带 {@link MqMessageListener} 的 {@link AbstractMessageHandler} bean，为每个处理器注册一个 PushConsumer。
 * 容器销毁时统一关闭。
 *
 * @author ycr
 */
@Slf4j
public class RocketMqListenerProcessor implements BeanPostProcessor, DisposableBean {

    private final RocketMqProperties properties;
    private final RocketMqClientFactory clientFactory;
    private final Environment environment;
    private final List<PushConsumer> consumers = new ArrayList<>();

    public RocketMqListenerProcessor(RocketMqProperties properties, RocketMqClientFactory clientFactory,
                                     Environment environment) {
        this.properties = properties;
        this.clientFactory = clientFactory;
        this.environment = environment;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof AbstractMessageHandler handler)) {
            return bean;
        }
        Class<?> clazz = AopUtils.getTargetClass(bean);
        MqMessageListener annotation = clazz.getAnnotation(MqMessageListener.class);
        if (annotation == null) {
            return bean;
        }
        String topic = resolve(annotation.topic(), annotation.enableSuffix());
        String group = resolveGroup(annotation.group());
        String tag = environment.resolvePlaceholders(annotation.tag());
        PushConsumer consumer = clientFactory.createPushConsumer(handler, group, topic, tag, annotation.consumeThreadCount());
        consumers.add(consumer);
        log.info("注册 RocketMQ 消费者: handler={}, group={}, topic={}, tag={}", clazz.getName(), group, topic, tag);
        return bean;
    }

    private String resolveGroup(String group) {
        String resolved = StringUtils.hasText(group) ? group : properties.getGroup();
        Assert.hasText(resolved, "消费者 group 未配置，请在 @MqMessageListener.group() 或 ycr.mq.rocketmq.group 指定");
        return environment.resolvePlaceholders(resolved);
    }

    private String resolve(String topic, boolean enableSuffix) {
        String resolved = environment.resolvePlaceholders(topic);
        if (enableSuffix && StringUtils.hasText(properties.getEnv())) {
            String suffixed = RocketMqConstants.SUFFIX + properties.getEnv();
            return resolved.endsWith(suffixed) ? resolved : resolved + suffixed;
        }
        return resolved;
    }

    @Override
    public void destroy() {
        for (PushConsumer consumer : consumers) {
            try {
                consumer.close();
            } catch (Exception e) {
                log.error("RocketMQ 消费者关闭失败", e);
            }
        }
        consumers.clear();
    }
}
