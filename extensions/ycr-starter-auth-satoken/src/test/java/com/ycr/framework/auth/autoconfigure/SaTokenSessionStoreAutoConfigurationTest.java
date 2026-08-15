package com.ycr.framework.auth.autoconfigure;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import cn.dev33.satoken.stp.StpLogic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SaTokenSessionStoreAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SaTokenSessionStoreAutoConfiguration.class));

    @Test
    @DisplayName("启用Auth时默认应使用内存会话存储")
    void enabledAuthShouldUseMemorySessionStoreByDefault() {
        runner.withPropertyValues("ycr.auth.satoken.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(SaTokenDao.class);
                    assertThat(context.getBean(SaTokenDao.class)).isInstanceOf(SaTokenDaoDefaultImpl.class);
                });
    }

    @Test
    @DisplayName("未启用Auth时不应创建会话存储")
    void disabledAuthShouldNotCreateSessionStore() {
        runner.run(context -> assertThat(context).doesNotHaveBean(SaTokenDao.class));
    }

    @Test
    @DisplayName("自定义SaTokenDao应覆盖内存会话存储")
    void customSaTokenDaoShouldBackOffMemoryStore() {
        SaTokenDao custom = mock(SaTokenDao.class);
        runner.withPropertyValues("ycr.auth.satoken.enabled=true")
                .withBean(SaTokenDao.class, () -> custom)
                .run(context -> assertThat(context.getBean(SaTokenDao.class)).isSameAs(custom));
    }

    @Test
    @DisplayName("Redis模式缺少认证域时应启动失败")
    void redisSessionStoreShouldRequireAuthDomain() {
        runner.withPropertyValues(
                        "ycr.auth.satoken.enabled=true",
                        "ycr.auth.satoken.session-store=redis")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage(
                                    "ycr.auth.satoken.auth-domain is required when session-store=redis");
                });
    }

    @Test
    @DisplayName("Redis模式配置认证域后应通过认证域门禁")
    void redisSessionStoreShouldAcceptExplicitAuthDomain() {
        runner.withPropertyValues(
                        "ycr.auth.satoken.enabled=true",
                        "ycr.auth.satoken.session-store=redis",
                        "ycr.auth.satoken.auth-domain=order-platform")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    @DisplayName("自定义登录类型必须与Redis认证域一致")
    void customLoginTypeShouldMatchRedisAuthDomain() {
        runner.withPropertyValues(
                        "ycr.auth.satoken.enabled=true",
                        "ycr.auth.satoken.session-store=redis",
                        "ycr.auth.satoken.auth-domain=order-platform")
                .withBean(StpLogic.class, () -> new StpLogic("another-domain"))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage(
                            "custom StpLogic loginType must match ycr.auth.satoken.auth-domain");
                });
    }
}
