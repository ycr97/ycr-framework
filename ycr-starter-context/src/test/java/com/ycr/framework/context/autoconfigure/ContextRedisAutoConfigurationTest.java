package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.sign.ContextReplayGuard;
import com.ycr.framework.context.sign.FailClosedContextReplayGuard;
import com.ycr.framework.context.sign.RedissonContextReplayGuard;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ContextRedisAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ContextRedisAutoConfiguration.class,
                    ContextAutoConfiguration.class));

    @Test
    void 存在RedissonClient时应装配原子防重放实现() {
        runner.withBean(RedissonClient.class, () -> mock(RedissonClient.class))
                .run(context -> assertThat(context).hasSingleBean(RedissonContextReplayGuard.class));
    }

    @Test
    void 无Redisson类路径时应正常启动并使用failClosed实现() {
        runner.withClassLoader(new FilteredClassLoader("org.redisson"))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(FailClosedContextReplayGuard.class);
                });
    }

    @Test
    void 业务自定义防重放实现应覆盖默认实现() {
        ContextReplayGuard custom = (nonce, ttl) -> false;
        runner.withBean(ContextReplayGuard.class, () -> custom)
                .withBean(RedissonClient.class, () -> mock(RedissonClient.class))
                .run(context -> assertThat(context.getBean(ContextReplayGuard.class)).isSameAs(custom));
    }
}
