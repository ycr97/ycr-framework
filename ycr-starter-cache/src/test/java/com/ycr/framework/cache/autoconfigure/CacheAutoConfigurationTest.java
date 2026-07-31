package com.ycr.framework.cache.autoconfigure;

import com.ycr.framework.cache.util.RedisUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 缓存模块自动配置测试。
 *
 * @author ycr
 */
class CacheAutoConfigurationTest {

    @Test
    @DisplayName("有Redisson时初始化工具类且关闭时不装配")
    void shouldMatchExpectedBehavior001() {
        RedissonClient client = (RedissonClient) Proxy.newProxyInstance(
                RedissonClient.class.getClassLoader(),
                new Class<?>[]{RedissonClient.class},
                (proxy, method, args) -> null);
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withBean(RedissonClient.class, () -> client)
                .withConfiguration(AutoConfigurations.of(CacheAutoConfiguration.class));

        runner.run(context -> {
            assertThat(context).hasSingleBean(CacheAutoConfiguration.class);
            assertThat(RedisUtils.getClient()).isSameAs(client);
        });
        runner.withPropertyValues("ycr.cache.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(CacheAutoConfiguration.class));
    }
}
