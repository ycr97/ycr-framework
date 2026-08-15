package com.ycr.framework.ratelimiter.autoconfigure;

import com.ycr.framework.ratelimiter.aop.RateLimiterAspect;
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
 * RateLimiterAutoConfiguration 装配与开关测试
 *
 * @author ycr
 */
class RateLimiterAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RateLimiterAutoConfiguration.class));

    @Test
    @DisplayName("默认不应装配切面")
    void shouldMatchExpectedBehavior001() {
        runner.withUserConfiguration(RedissonConfig.class)
                .run(context -> assertThat(context).doesNotHaveBean(RateLimiterAspect.class));
    }

    @Test
    @DisplayName("显式开启且存在Redisson时应装配切面")
    void shouldMatchExpectedBehavior002() {
        runner.withUserConfiguration(RedissonConfig.class)
                .withPropertyValues("ycr.ratelimiter.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(RateLimiterAspect.class));
    }

    @Test
    @DisplayName("显式开启但无Redisson时应启动失败")
    void shouldFailWhenEnabledWithoutRedissonClient() {
        runner.withPropertyValues("ycr.ratelimiter.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage(
                            "ycr.ratelimiter.enabled=true requires a RedissonClient; "
                                    + "configure ycr-starter-cache and Redis");
                });
    }

    @Configuration
    static class RedissonConfig {
        @Bean
        RedissonClient redissonClient() {
            return mock(RedissonClient.class);
        }
    }
}
