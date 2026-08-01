package com.ycr.framework.auth.autoconfigure;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoForRedisson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SaTokenRedisSessionStoreAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SaTokenRedisSessionStoreAutoConfiguration.class))
            .withPropertyValues(
                    "ycr.auth.satoken.enabled=true",
                    "ycr.auth.satoken.session-store=redis");

    @Test
    @DisplayName("Redis模式应复用现有RedissonClient")
    void redisSessionStoreShouldReuseExistingRedissonClient() {
        RedissonClient redissonClient = mock(RedissonClient.class);
        runner.withBean(RedissonClient.class, () -> redissonClient)
                .run(context -> {
                    assertThat(context).hasSingleBean(SaTokenDao.class);
                    assertThat(context.getBean(SaTokenDao.class)).isInstanceOf(SaTokenDaoForRedisson.class);
                });
    }

    @Test
    @DisplayName("Redis模式缺少RedissonClient时应启动失败")
    void redisSessionStoreShouldFailWithoutRedissonClient() {
        runner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasMessageContaining("RedissonClient");
        });
    }
}
