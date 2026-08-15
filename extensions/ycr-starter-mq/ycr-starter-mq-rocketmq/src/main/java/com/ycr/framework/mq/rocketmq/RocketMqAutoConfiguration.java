package com.ycr.framework.mq.rocketmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * RocketMQ 自动配置：仅当 {@code ycr.mq.rocketmq.enabled=true} 且类路径存在 rocketmq 客户端时装配。
 *
 * @author ycr
 */
@AutoConfiguration
@EnableConfigurationProperties(RocketMqProperties.class)
@ConditionalOnClass(name = "org.apache.rocketmq.client.apis.producer.Producer")
@ConditionalOnProperty(prefix = "ycr.mq.rocketmq", name = "enabled", havingValue = "true")
public class RocketMqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RocketMqClientFactory rocketMqClientFactory(RocketMqProperties properties) {
        return new RocketMqClientFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public Producer rocketMqProducer(RocketMqClientFactory clientFactory) {
        return clientFactory.createProducer();
    }

    @Bean
    @ConditionalOnMissingBean
    public RocketMqMessageProducer rocketMqMessageProducer(Producer producer, RocketMqProperties properties,
                                                           ObjectMapper objectMapper) {
        return new RocketMqMessageProducer(producer, properties, objectMapper);
    }

    @Bean
    public static RocketMqListenerProcessor rocketMqListenerProcessor(
            ObjectProvider<RocketMqProperties> properties,
            ObjectProvider<RocketMqClientFactory> clientFactory,
            Environment environment) {
        return new RocketMqListenerProcessor(properties, clientFactory, environment);
    }
}
