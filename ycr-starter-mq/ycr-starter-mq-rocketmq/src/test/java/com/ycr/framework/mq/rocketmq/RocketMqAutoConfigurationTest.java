package com.ycr.framework.mq.rocketmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.mq.producer.MessageProducer;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("默认关闭时不装配任何mq组件")
    void shouldMatchExpectedBehavior001() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(MessageProducer.class);
            assertThat(context).doesNotHaveBean(RocketMqListenerProcessor.class);
        });
    }

    @Test
    @DisplayName("关闭开关显式为false时不装配")
    void shouldMatchExpectedBehavior002() {
        runner.withPropertyValues("ycr.mq.rocketmq.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(MessageProducer.class));
    }

    @Test
    @DisplayName("显式开启时应装配mq组件")
    void shouldMatchExpectedBehavior003() {
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
