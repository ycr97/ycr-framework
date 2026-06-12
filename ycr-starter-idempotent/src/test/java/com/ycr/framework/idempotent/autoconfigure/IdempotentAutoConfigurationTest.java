package com.ycr.framework.idempotent.autoconfigure;

import com.ycr.framework.idempotent.aop.IdempotentAspect;
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
    void 存在Redisson时应装配切面() {
        runner.withUserConfiguration(RedissonConfig.class)
                .run(context -> assertThat(context).hasSingleBean(IdempotentAspect.class));
    }

    @Test
    void 关闭开关时不装配() {
        runner.withUserConfiguration(RedissonConfig.class)
                .withPropertyValues("ycr.idempotent.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(IdempotentAspect.class));
    }

    @Test
    void 无Redisson时不装配切面() {
        runner.run(context -> assertThat(context).doesNotHaveBean(IdempotentAspect.class));
    }

    @Configuration
    static class RedissonConfig {
        @Bean
        RedissonClient redissonClient() {
            return mock(RedissonClient.class);
        }
    }
}
