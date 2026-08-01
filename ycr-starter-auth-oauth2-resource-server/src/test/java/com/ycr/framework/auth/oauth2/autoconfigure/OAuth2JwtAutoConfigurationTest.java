package com.ycr.framework.auth.oauth2.autoconfigure;

import com.ycr.framework.security.autoconfigure.SecurityAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OAuth2JwtAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SecurityAutoConfiguration.class,
                    OAuth2ResourceServerAutoConfiguration.class,
                    OAuth2JwtAutoConfiguration.class));

    @Test
    @DisplayName("JWT模式应创建Nimbus decoder并使用显式JWK Set URI")
    void jwtModeCreatesNimbusDecoder() {
        runner.withPropertyValues(validJwtProperties())
                .run(context -> {
                    assertThat(context).hasSingleBean(JwtDecoder.class);
                    assertThat(context).doesNotHaveBean(OpaqueTokenIntrospector.class);
                    assertThat(context.getBean(JwtDecoder.class)).isInstanceOf(NimbusJwtDecoder.class);
                });
    }

    @Test
    @DisplayName("自定义JwtDecoder应覆盖默认实现")
    void customJwtDecoderShouldBackOffDefaultDecoder() {
        JwtDecoder custom = mock(JwtDecoder.class);

        runner.withPropertyValues(validJwtProperties())
                .withBean(JwtDecoder.class, () -> custom)
                .run(context -> assertThat(context.getBean(JwtDecoder.class)).isSameAs(custom));
    }

    @Test
    @DisplayName("默认关闭或Opaque模式不应创建JwtDecoder")
    void disabledOrOpaqueModeShouldNotCreateJwtDecoder() {
        runner.run(context -> assertThat(context).doesNotHaveBean(JwtDecoder.class));

        runner.withPropertyValues(
                        "ycr.auth.oauth2.resource-server.enabled=true",
                        "ycr.auth.oauth2.resource-server.mode=opaque",
                        "ycr.auth.oauth2.resource-server.opaque.introspection-uri=https://idp.example.com/introspect",
                        "ycr.auth.oauth2.resource-server.opaque.client-id=test-client",
                        "ycr.auth.oauth2.resource-server.opaque.client-secret=test-secret",
                        "ycr.auth.oauth2.resource-server.opaque.audiences[0]=order-api")
                .run(context -> assertThat(context).doesNotHaveBean(JwtDecoder.class));
    }

    private String[] validJwtProperties() {
        return new String[]{
                "ycr.auth.oauth2.resource-server.enabled=true",
                "ycr.auth.oauth2.resource-server.mode=jwt",
                "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com",
                "ycr.auth.oauth2.resource-server.jwt.jwk-set-uri=https://idp.example.com/jwks",
                "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api"
        };
    }
}
