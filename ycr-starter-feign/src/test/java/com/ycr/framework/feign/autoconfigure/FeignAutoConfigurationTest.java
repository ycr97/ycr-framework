package com.ycr.framework.feign.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.context.autoconfigure.ContextAutoConfiguration;
import com.ycr.framework.feign.decoder.FeignErrorDecoder;
import com.ycr.framework.feign.interceptor.ContextPassInterceptor;
import com.ycr.framework.feign.interceptor.LocalePassInterceptor;
import com.ycr.framework.feign.interceptor.TokenPassInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FeignAutoConfiguration 装配与开关测试
 *
 * @author ycr
 */
class FeignAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(ObjectMapperConfig.class)
            .withPropertyValues("ycr.context.header-sign.secret=test-secret")
            .withConfiguration(AutoConfigurations.of(ContextAutoConfiguration.class, FeignAutoConfiguration.class));

    @Test
    @DisplayName("默认应关闭身份透传并保留错误解码器")
    void shouldMatchExpectedBehavior001() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(ContextPassInterceptor.class);
            assertThat(context).hasSingleBean(FeignErrorDecoder.class);
            assertThat(context).hasSingleBean(LocalePassInterceptor.class);
        });
    }

    @Test
    @DisplayName("关闭locale开关时不装配语言透传拦截器")
    void shouldMatchExpectedBehavior002() {
        runner.withPropertyValues("ycr.feign.locale-pass-enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(LocalePassInterceptor.class));
    }

    @Test
    @DisplayName("关闭透传开关时不装配拦截器")
    void shouldMatchExpectedBehavior003() {
        runner.withPropertyValues("ycr.feign.context-pass-enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(ContextPassInterceptor.class));
    }

    @Test
    @DisplayName("开启上下文透传并配置内部服务时应装配拦截器")
    void shouldConfigureContextPassForExplicitInternalClients() {
        runner.withPropertyValues(
                        "ycr.feign.context-pass-enabled=true",
                        "ycr.feign.internal-clients=user-service")
                .run(context -> assertThat(context).hasSingleBean(ContextPassInterceptor.class));
    }

    @Test
    @DisplayName("开启上下文透传但未配置内部服务时应启动失败")
    void shouldFailWhenContextPassHasNoInternalClients() {
        runner.withPropertyValues("ycr.feign.context-pass-enabled=true")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "ycr.feign.internal-clients 必须在启用上下文或 Token 透传时显式配置"));
    }

    @Test
    @DisplayName("关闭解码开关时不装配解码器")
    void shouldMatchExpectedBehavior004() {
        runner.withPropertyValues("ycr.feign.error-decoder-enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(FeignErrorDecoder.class));
    }

    @Test
    @DisplayName("默认不装配token透传拦截器")
    void shouldMatchExpectedBehavior005() {
        runner.run(context -> assertThat(context).doesNotHaveBean(TokenPassInterceptor.class));
    }

    @Test
    @DisplayName("开启token开关后装配token透传拦截器")
    void shouldMatchExpectedBehavior006() {
        runner.withPropertyValues(
                        "ycr.feign.token-pass-enabled=true",
                        "ycr.feign.internal-clients=user-service")
                .run(context -> assertThat(context).hasSingleBean(TokenPassInterceptor.class));
    }

    @Test
    @DisplayName("无servletApi时不装配语言与token透传拦截器但其余仍装配")
    void shouldMatchExpectedBehavior007() {
        runner.withClassLoader(new FilteredClassLoader(HttpServletRequest.class))
                .withPropertyValues(
                        "ycr.feign.context-pass-enabled=true",
                        "ycr.feign.token-pass-enabled=true",
                        "ycr.feign.internal-clients=user-service")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(LocalePassInterceptor.class);
                    assertThat(context).doesNotHaveBean(TokenPassInterceptor.class);
                    assertThat(context).hasSingleBean(ContextPassInterceptor.class);
                    assertThat(context).hasSingleBean(FeignErrorDecoder.class);
                });
    }

    @Configuration
    static class ObjectMapperConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
