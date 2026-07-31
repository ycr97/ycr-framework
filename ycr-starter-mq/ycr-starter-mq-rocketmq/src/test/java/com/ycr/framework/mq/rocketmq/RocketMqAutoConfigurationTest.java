package com.ycr.framework.mq.rocketmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.mq.producer.MessageProducer;
import org.junit.jupiter.api.Test;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * RocketMqAutoConfiguration 装配开关测试（不连接真实 broker）
 *
 * @author ycr
 */
class RocketMqAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(ObjectMapperConfig.class)
            .withConfiguration(AutoConfigurations.of(RocketMqAutoConfiguration.class));

    @Test
    void 默认关闭时不装配任何mq组件() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(MessageProducer.class);
            assertThat(context).doesNotHaveBean(RocketMqListenerProcessor.class);
        });
    }

    @Test
    void 关闭开关显式为false时不装配() {
        runner.withPropertyValues("ycr.mq.rocketmq.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(MessageProducer.class));
    }

    @Test
    void 显式开启时应装配mq组件() {
        runner.withBean(RocketMqClientFactory.class, () -> mock(RocketMqClientFactory.class))
                .withBean(Producer.class, () -> mock(Producer.class))
                .withPropertyValues("ycr.mq.rocketmq.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(MessageProducer.class);
                    assertThat(context).hasSingleBean(RocketMqListenerProcessor.class);
                });
    }

    @Configuration
    static class ObjectMapperConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
