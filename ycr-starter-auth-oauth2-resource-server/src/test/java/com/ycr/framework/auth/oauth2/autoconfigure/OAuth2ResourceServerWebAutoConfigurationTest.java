package com.ycr.framework.auth.oauth2.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.auth.oauth2.filter.OAuth2UserContextFilter;
import com.ycr.framework.auth.oauth2.handler.YcrBearerAccessDeniedHandler;
import com.ycr.framework.auth.oauth2.handler.YcrBearerAuthenticationEntryPoint;
import com.ycr.framework.context.autoconfigure.ContextAutoConfiguration;
import com.ycr.framework.context.autoconfigure.ContextServletAutoConfiguration;
import com.ycr.framework.security.checker.PermissionChecker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OAuth2ResourceServerWebAutoConfigurationTest {

    private final WebApplicationContextRunner webRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                    ContextAutoConfiguration.class,
                    ContextServletAutoConfiguration.class,
                    OAuth2ResourceServerAutoConfiguration.class,
                    OAuth2JwtAutoConfiguration.class,
                    OAuth2ResourceServerWebAutoConfiguration.class))
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .withBean(CorsConfigurationSource.class, UrlBasedCorsConfigurationSource::new)
            .withBean("mvcHandlerMappingIntrospector", HandlerMappingIntrospector.class,
                    HandlerMappingIntrospector::new)
            .withBean(PermissionChecker.class, () -> mock(PermissionChecker.class))
            .withBean(JwtDecoder.class, () -> mock(JwtDecoder.class));

    @Test
    @DisplayName("非Servlet或默认关闭时不应装配Web安全Bean")
    void nonServletOrDisabledShouldNotConfigureWebBeans() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(OAuth2ResourceServerWebAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(SecurityFilterChain.class);
                    assertThat(context).doesNotHaveBean(OAuth2UserContextFilter.class);
                });

        webRunner.run(context -> {
            assertThat(context).doesNotHaveBean("ycrOAuth2ResourceServerSecurityFilterChain");
            assertThat(context).doesNotHaveBean(OAuth2UserContextFilter.class);
        });
    }

    @Test
    @DisplayName("JWT启用时应创建固定名称和顺序的YCR安全链")
    void enabledJwtCreatesYcrSecurityChain() {
        webRunner.withPropertyValues(validJwtProperties())
                .run(context -> {
                    assertThat(context).hasBean("ycrOAuth2ResourceServerSecurityFilterChain");
                    assertThat(context).hasSingleBean(SecurityFilterChain.class);
                    assertThat(context).hasSingleBean(OAuth2UserContextFilter.class);
                    assertThat(context).hasSingleBean(YcrBearerAuthenticationEntryPoint.class);
                    assertThat(context).hasSingleBean(YcrBearerAccessDeniedHandler.class);
                });
    }

    @Test
    @DisplayName("业务自定义其他安全链时不应关闭YCR安全链")
    void unrelatedSecurityChainDoesNotBackOffYcrChain() {
        webRunner.withPropertyValues(validJwtProperties())
                .withBean("businessSecurityFilterChain", SecurityFilterChain.class, () -> mock(SecurityFilterChain.class))
                .run(context -> assertThat(context).hasBean("ycrOAuth2ResourceServerSecurityFilterChain"));
    }

    @Test
    @DisplayName("无关认证处理器不应替换YCR Bearer响应语义")
    void unrelatedHandlersDoNotBackOffYcrHandlers() {
        webRunner.withPropertyValues(validJwtProperties())
                .withBean("businessAuthenticationEntryPoint", AuthenticationEntryPoint.class,
                        () -> mock(AuthenticationEntryPoint.class))
                .withBean("businessAccessDeniedHandler", AccessDeniedHandler.class,
                        () -> mock(AccessDeniedHandler.class))
                .run(context -> {
                    assertThat(context).hasBean("ycrBearerAuthenticationEntryPoint");
                    assertThat(context).hasBean("ycrBearerAccessDeniedHandler");
                    assertThat(context.getBean("ycrBearerAuthenticationEntryPoint"))
                            .isInstanceOf(YcrBearerAuthenticationEntryPoint.class);
                    assertThat(context.getBean("ycrBearerAccessDeniedHandler"))
                            .isInstanceOf(YcrBearerAccessDeniedHandler.class);
                });
    }

    private String[] validJwtProperties() {
        return new String[]{
                "ycr.auth.oauth2.resource-server.enabled=true",
                "ycr.auth.oauth2.resource-server.mode=jwt",
                "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com",
                "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api"
        };
    }
}
