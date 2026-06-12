package com.ycr.framework.ratelimiter.autoconfigure;

import com.ycr.framework.ratelimiter.aop.RateLimiterAspect;
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
    void 存在Redisson时应装配切面() {
        runner.withUserConfiguration(RedissonConfig.class)
                .run(context -> assertThat(context).hasSingleBean(RateLimiterAspect.class));
    }

    @Test
    void 关闭开关时不装配() {
        runner.withUserConfiguration(RedissonConfig.class)
                .withPropertyValues("ycr.ratelimiter.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(RateLimiterAspect.class));
    }

    @Test
    void 无Redisson时不装配切面() {
        runner.run(context -> assertThat(context).doesNotHaveBean(RateLimiterAspect.class));
    }

    @Configuration
    static class RedissonConfig {
        @Bean
        RedissonClient redissonClient() {
            return mock(RedissonClient.class);
        }
    }
}
