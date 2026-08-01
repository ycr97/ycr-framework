package com.ycr.framework.auth.oauth2.autoconfigure;

import com.ycr.framework.auth.oauth2.mapper.OAuth2UserContextMapper;
import com.ycr.framework.security.aspect.AuthorizeAspect;
import com.ycr.framework.security.autoconfigure.SecurityAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

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
}
