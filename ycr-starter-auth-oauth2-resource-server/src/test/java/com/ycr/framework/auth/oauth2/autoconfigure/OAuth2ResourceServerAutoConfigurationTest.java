package com.ycr.framework.auth.oauth2.autoconfigure;

import com.ycr.framework.auth.oauth2.filter.OAuth2UserContextFilter;
import com.ycr.framework.auth.oauth2.mapper.OAuth2UserContextMapper;
import com.ycr.framework.security.aspect.AuthorizeAspect;
import com.ycr.framework.security.autoconfigure.SecurityAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OAuth2ResourceServerAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SecurityAutoConfiguration.class,
                    OAuth2ResourceServerAutoConfiguration.class));

    @Test
    @DisplayName("默认关闭时不应装配OAuth2适配器")
    void disabledOAuth2ShouldNotConfigureAdapterBeans() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(OAuth2UserContextMapper.class);
            assertThat(context).doesNotHaveBean(AuthorizeAspect.class);
            assertThat(context).doesNotHaveBean(JwtDecoder.class);
            assertThat(context).doesNotHaveBean(OpaqueTokenIntrospector.class);
            assertThat(context).doesNotHaveBean(OAuth2UserContextFilter.class);
            assertThat(context).doesNotHaveBean(SecurityFilterChain.class);
        });
    }

    @Test
    @DisplayName("启用JWT时应装配OAuth2 mapper与方法鉴权")
    void enabledJwtShouldConfigureMapperAndAuthorizeAspect() {
        runner.withPropertyValues(
                        "ycr.auth.oauth2.resource-server.enabled=true",
                        "ycr.auth.oauth2.resource-server.mode=jwt",
                        "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com",
                        "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api")
                .run(context -> {
                    assertThat(context).hasSingleBean(OAuth2UserContextMapper.class);
                    assertThat(context).hasSingleBean(AuthorizeAspect.class);
                });
    }

    @Test
    @DisplayName("启用后缺少mode时应启动失败")
    void enabledOAuth2ShouldRequireMode() {
        runner.withPropertyValues("ycr.auth.oauth2.resource-server.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage(
                            "ycr.auth.oauth2.resource-server.mode is required when "
                                    + "ycr.auth.oauth2.resource-server.enabled=true");
                });
    }

    @Test
    @DisplayName("JWT模式应校验issuer、audiences和算法")
    void jwtModeShouldRequireIssuerAudiencesAndAsymmetricAlgorithm() {
        runner.withPropertyValues(
                        "ycr.auth.oauth2.resource-server.enabled=true",
                        "ycr.auth.oauth2.resource-server.mode=jwt",
                        "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage(
                            "ycr.auth.oauth2.resource-server.jwt.issuer-uri is required when mode=jwt");
                });

        runner.withPropertyValues(
                        "ycr.auth.oauth2.resource-server.enabled=true",
                        "ycr.auth.oauth2.resource-server.mode=jwt",
                        "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com",
                        "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api",
                        "ycr.auth.oauth2.resource-server.jwt.allowed-algorithms[0]=HS256")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage(
                            "ycr.auth.oauth2.resource-server.jwt.allowed-algorithms must not contain "
                                    + "symmetric or none algorithms");
                });

        runner.withPropertyValues(
                        "ycr.auth.oauth2.resource-server.enabled=true",
                        "ycr.auth.oauth2.resource-server.mode=jwt",
                        "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage(
                            "ycr.auth.oauth2.resource-server.jwt.audiences must not be empty");
                });

        runner.withPropertyValues(
                        "ycr.auth.oauth2.resource-server.enabled=true",
                        "ycr.auth.oauth2.resource-server.mode=jwt",
                        "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com",
                        "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api",
                        "ycr.auth.oauth2.resource-server.jwt.clock-skew=-1ms")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage(
                            "ycr.auth.oauth2.resource-server.jwt.clock-skew must not be negative");
                });
    }

    @Test
    @DisplayName("Opaque模式应校验endpoint、客户端凭据、audiences和超时")
    void opaqueModeShouldRequireEndpointCredentialsAudiencesAndTimeouts() {
        runner.withPropertyValues(
                        "ycr.auth.oauth2.resource-server.enabled=true",
                        "ycr.auth.oauth2.resource-server.mode=opaque")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage(
                            "ycr.auth.oauth2.resource-server.opaque.introspection-uri "
                                    + "is required when mode=opaque");
                });

        runner.withPropertyValues(
                        "ycr.auth.oauth2.resource-server.enabled=true",
                        "ycr.auth.oauth2.resource-server.mode=opaque",
                        "ycr.auth.oauth2.resource-server.opaque.introspection-uri=https://idp.example.com/introspect",
                        "ycr.auth.oauth2.resource-server.opaque.client-secret=test-secret",
                        "ycr.auth.oauth2.resource-server.opaque.audiences[0]=order-api")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "ycr.auth.oauth2.resource-server.opaque.client-id is required when mode=opaque"));

        runner.withPropertyValues(
                        "ycr.auth.oauth2.resource-server.enabled=true",
                        "ycr.auth.oauth2.resource-server.mode=opaque",
                        "ycr.auth.oauth2.resource-server.opaque.introspection-uri=https://idp.example.com/introspect",
                        "ycr.auth.oauth2.resource-server.opaque.client-id=test-client",
                        "ycr.auth.oauth2.resource-server.opaque.audiences[0]=order-api")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "ycr.auth.oauth2.resource-server.opaque.client-secret is required when mode=opaque"));

        runner.withPropertyValues(
                        "ycr.auth.oauth2.resource-server.enabled=true",
                        "ycr.auth.oauth2.resource-server.mode=opaque",
                        "ycr.auth.oauth2.resource-server.opaque.introspection-uri=https://idp.example.com/introspect",
                        "ycr.auth.oauth2.resource-server.opaque.client-id=test-client",
                        "ycr.auth.oauth2.resource-server.opaque.client-secret=test-secret")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "ycr.auth.oauth2.resource-server.opaque.audiences must not be empty"));

        runner.withPropertyValues(validOpaqueProperties())
                .withPropertyValues("ycr.auth.oauth2.resource-server.opaque.connect-timeout=0ms")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "ycr.auth.oauth2.resource-server.opaque.connect-timeout must be positive"));

        runner.withPropertyValues(validOpaqueProperties())
                .withPropertyValues("ycr.auth.oauth2.resource-server.opaque.read-timeout=-1ms")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "ycr.auth.oauth2.resource-server.opaque.read-timeout must be positive"));
    }

    @Test
    @DisplayName("OAuth2与SaToken同时启用时应启动失败")
    void oauth2AndSaTokenShouldNotBeEnabledTogether() {
        runner.withPropertyValues(
                        "ycr.auth.oauth2.resource-server.enabled=true",
                        "ycr.auth.oauth2.resource-server.mode=jwt",
                        "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com",
                        "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api",
                        "ycr.auth.satoken.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage(
                            "ycr.auth.satoken.enabled and ycr.auth.oauth2.resource-server.enabled "
                                    + "cannot both be true");
                });
    }

    @Test
    @DisplayName("OAuth2与GatewayTrust同时启用时应启动失败")
    void oauth2AndGatewayTrustShouldNotBeEnabledTogether() {
        runner.withPropertyValues(
                        "ycr.auth.oauth2.resource-server.enabled=true",
                        "ycr.auth.oauth2.resource-server.mode=jwt",
                        "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com",
                        "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api",
                        "ycr.context.security-mode=GATEWAY_TRUST")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage(
                            "ycr.context.security-mode=GATEWAY_TRUST cannot be used with "
                                    + "ycr.auth.oauth2.resource-server.enabled");
                });
    }

    @Test
    @DisplayName("自定义mapper应覆盖默认实现")
    void customMapperShouldBackOffDefaultMapper() {
        OAuth2UserContextMapper custom = mock(OAuth2UserContextMapper.class);
        runner.withPropertyValues(
                        "ycr.auth.oauth2.resource-server.enabled=true",
                        "ycr.auth.oauth2.resource-server.mode=jwt",
                        "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com",
                        "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api")
                .withBean(OAuth2UserContextMapper.class, () -> custom)
                .run(context -> assertThat(context.getBean(OAuth2UserContextMapper.class)).isSameAs(custom));
    }

    private String[] validOpaqueProperties() {
        return new String[]{
                "ycr.auth.oauth2.resource-server.enabled=true",
                "ycr.auth.oauth2.resource-server.mode=opaque",
                "ycr.auth.oauth2.resource-server.opaque.introspection-uri=https://idp.example.com/introspect",
                "ycr.auth.oauth2.resource-server.opaque.client-id=test-client",
                "ycr.auth.oauth2.resource-server.opaque.client-secret=test-secret",
                "ycr.auth.oauth2.resource-server.opaque.audiences[0]=order-api"
        };
    }
}
