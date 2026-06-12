package com.ycr.framework.i18n.autoconfigure;

import com.ycr.framework.i18n.resolver.HeaderLocaleResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.web.servlet.LocaleResolver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * I18nAutoConfiguration 装配与开关测试
 *
 * @author ycr
 */
class I18nAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MessageSourceAutoConfiguration.class, I18nAutoConfiguration.class));

    private final WebApplicationContextRunner webRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MessageSourceAutoConfiguration.class, I18nAutoConfiguration.class));

    @Test
    void 非web环境不注册LocaleResolver但应成功装配() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(LocaleResolver.class);
        });
    }

    @Test
    void web环境应注册HeaderLocaleResolver() {
        webRunner.run(context -> {
            assertThat(context).hasSingleBean(LocaleResolver.class);
            assertThat(context.getBean(LocaleResolver.class)).isInstanceOf(HeaderLocaleResolver.class);
        });
    }

    @Test
    void 关闭开关时不注册LocaleResolver() {
        webRunner.withPropertyValues("ycr.i18n.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(LocaleResolver.class));
    }
}
