package com.ycr.framework.idempotent.autoconfigure;

import com.ycr.framework.idempotent.aop.IdempotentAspect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * IdempotentAutoConfiguration 装配与开关测试
 *
 * @author ycr
 */
class IdempotentAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(IdempotentAutoConfiguration.class));

    @Test
    @DisplayName("默认不应装配切面")
    void shouldMatchExpectedBehavior001() {
        runner.withUserConfiguration(RedissonConfig.class)
                .run(context -> assertThat(context).doesNotHaveBean(IdempotentAspect.class));
    }

    @Test
    @DisplayName("显式开启且存在Redisson时应装配切面")
    void shouldMatchExpectedBehavior002() {
        runner.withUserConfiguration(RedissonConfig.class)
                .withPropertyValues("ycr.idempotent.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(IdempotentAspect.class));
    }

    @Test
    @DisplayName("显式开启但无Redisson时不装配切面")
    void shouldMatchExpectedBehavior003() {
        runner.withPropertyValues("ycr.idempotent.enabled=true")
                .run(context -> assertThat(context).doesNotHaveBean(IdempotentAspect.class));
    }

    @Configuration
    static class RedissonConfig {
        @Bean
        RedissonClient redissonClient() {
            return mock(RedissonClient.class);
        }
    }
}
