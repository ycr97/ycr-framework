package com.ycr.framework.auth.autoconfigure;

import cn.dev33.satoken.interceptor.SaInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SaTokenWebAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SaTokenWebAutoConfiguration.class));

    @Test
    @DisplayName("未启用Auth时不应注册登录拦截器")
    void disabledAuthShouldNotRegisterLoginInterceptor() {
        runner.run(context -> assertThat(context).doesNotHaveBean(SaInterceptor.class));
    }

    @Test
    @DisplayName("Auth启用后默认应注册登录拦截器")
    void enabledAuthShouldRegisterLoginInterceptorByDefault() {
        runner.withPropertyValues("ycr.auth.satoken.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(SaInterceptor.class));
    }

    @Test
    @DisplayName("注解策略不应注册全局登录拦截器")
    void annotatedPolicyShouldNotRegisterGlobalLoginInterceptor() {
        runner.withPropertyValues(
                        "ycr.auth.satoken.enabled=true",
                        "ycr.auth.satoken.endpoint-policy=annotated")
                .run(context -> assertThat(context).doesNotHaveBean(SaInterceptor.class));
    }
}
