package com.ycr.framework.auth.autoconfigure;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.ycr.framework.auth.handler.SaTokenExceptionHandler;
import com.ycr.framework.auth.resolver.SaTokenUserContextResolver;
import com.ycr.framework.auth.session.SaTokenSessionManager;
import com.ycr.framework.security.aspect.AuthorizeAspect;
import com.ycr.framework.security.autoconfigure.SecurityAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SaTokenAuthAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SecurityAutoConfiguration.class,
                    SaTokenAuthAutoConfiguration.class,
                    SaTokenSessionStoreAutoConfiguration.class))
            .withBean(SaTokenConfig.class, SaTokenConfig::new);

    @Test
    @DisplayName("未启用Auth时不应装配认证适配器")
    void disabledAuthShouldNotConfigureAdapterBeans() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(SaTokenSessionManager.class);
            assertThat(context).doesNotHaveBean(SaTokenUserContextResolver.class);
            assertThat(context).doesNotHaveBean(SaTokenExceptionHandler.class);
            assertThat(context).doesNotHaveBean(AuthorizeAspect.class);
            assertThat(context).doesNotHaveBean(SaTokenDao.class);
        });
    }

    @Test
    @DisplayName("启用Auth时默认装配方法鉴权与内存会话")
    void enabledAuthShouldConfigureSecurityAndMemorySessionStore() {
        runner.withPropertyValues("ycr.auth.satoken.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(SaTokenSessionManager.class);
                    assertThat(context).hasSingleBean(SaTokenUserContextResolver.class);
                    assertThat(context).hasSingleBean(SaTokenExceptionHandler.class);
                    assertThat(context).hasSingleBean(AuthorizeAspect.class);
                    assertThat(context).hasSingleBean(StpLogic.class);
                    assertThat(context.getBean(StpLogic.class).getLoginType()).isEqualTo(StpUtil.TYPE);
                    assertThat(context).hasSingleBean(SaTokenDao.class);
                    assertThat(context.getBean(SaTokenDao.class)).isInstanceOf(SaTokenDaoDefaultImpl.class);
                });
    }

    @Test
    @DisplayName("配置认证域时应绑定为Sa-Token登录类型")
    void configuredAuthDomainShouldBecomeSaTokenLoginType() {
        runner.withPropertyValues(
                        "ycr.auth.satoken.enabled=true",
                        "ycr.auth.satoken.auth-domain=order-platform")
                .run(context -> {
                    StpLogic stpLogic = context.getBean(StpLogic.class);
                    assertThat(stpLogic.getLoginType()).isEqualTo("order-platform");
                    assertThat(stpLogic.splicingKeyTokenValue("sample-token"))
                            .contains(":order-platform:token:sample-token");
                });
    }

    @Test
    @DisplayName("SaToken与OAuth2同时启用时应启动失败")
    void saTokenAndOAuth2ShouldNotBeEnabledTogether() {
        runner.withPropertyValues(
                        "ycr.auth.satoken.enabled=true",
                        "ycr.auth.oauth2.resource-server.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage(
                                    "ycr.auth.satoken.enabled and ycr.auth.oauth2.resource-server.enabled "
                                            + "cannot both be true");
                });
    }

    @Test
    @DisplayName("用户自定义会话管理器应覆盖默认实现")
    void customSessionManagerShouldBackOffDefaultBean() {
        SaTokenSessionManager custom = mock(SaTokenSessionManager.class);
        runner.withPropertyValues("ycr.auth.satoken.enabled=true")
                .withBean(SaTokenSessionManager.class, () -> custom)
                .run(context -> assertThat(context.getBean(SaTokenSessionManager.class)).isSameAs(custom));
    }

    @Test
    @DisplayName("SaToken不在类路径时不应发生错误装配")
    void missingSaTokenClasspathShouldBackOffCleanly() {
        new ApplicationContextRunner()
                .withClassLoader(new FilteredClassLoader("cn.dev33.satoken"))
                .withConfiguration(AutoConfigurations.of(SaTokenAuthAutoConfiguration.class))
                .withPropertyValues("ycr.auth.satoken.enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(SaTokenSessionManager.class);
                });
    }

}
