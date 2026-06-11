package com.ycr.framework.security.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 安全模块自动配置测试
 *
 * <p>验证 Servlet Web 环境下默认注册鉴权拦截器配置，且 {@code ycr.security.enabled=false}
 * 时不注册；非 Web 环境亦不注册。</p>
 */
class SecurityAutoConfigurationTest {

    private final WebApplicationContextRunner webRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.class));

    @Test
    void Web环境默认应注册鉴权拦截器配置() {
        webRunner.run(context ->
                assertThat(context).hasBean("ycrSecurityWebMvcConfigurer"));
    }

    @Test
    void 关闭开关时不应注册鉴权拦截器配置() {
        webRunner.withPropertyValues("ycr.security.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(WebMvcConfigurer.class));
    }
}
