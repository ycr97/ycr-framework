package com.ycr.framework.auth.autoconfigure;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SaTokenMissingRedisAutoConfigurationTest {

    @Test
    @DisplayName("缺少Redisson依赖时内存模式仍应正常装配")
    void memorySessionStoreShouldWorkWithoutRedissonDependency() {
        new ApplicationContextRunner()
                .withClassLoader(new FilteredClassLoader("org.redisson"))
                .withConfiguration(AutoConfigurations.of(
                        SaTokenSessionStoreAutoConfiguration.class,
                        SaTokenRedisSessionStoreAutoConfiguration.class,
                        SaTokenMissingRedisAutoConfiguration.class))
                .withPropertyValues("ycr.auth.satoken.enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(SaTokenDao.class);
                    assertThat(context.getBean(SaTokenDao.class)).isInstanceOf(SaTokenDaoDefaultImpl.class);
                });
    }

    @Test
    @DisplayName("Redis模式缺少Redisson依赖时应给出明确失败原因")
    void redisSessionStoreShouldFailClearlyWithoutRedissonDependency() {
        new ApplicationContextRunner()
                .withClassLoader(new FilteredClassLoader("org.redisson"))
                .withConfiguration(AutoConfigurations.of(SaTokenMissingRedisAutoConfiguration.class))
                .withPropertyValues(
                        "ycr.auth.satoken.enabled=true",
                        "ycr.auth.satoken.session-store=redis",
                        "ycr.auth.satoken.auth-domain=test-domain")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage(
                                    "ycr.auth.satoken.session-store=redis requires RedissonClient; "
                                            + "add ycr-starter-cache and configure Redis");
                });
    }
}
