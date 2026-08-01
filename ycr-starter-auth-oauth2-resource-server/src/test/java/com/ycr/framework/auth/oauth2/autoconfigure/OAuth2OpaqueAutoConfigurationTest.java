package com.ycr.framework.auth.oauth2.autoconfigure;

import com.ycr.framework.auth.oauth2.introspection.ValidatingOpaqueTokenIntrospector;
import com.ycr.framework.security.autoconfigure.SecurityAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OAuth2OpaqueAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SecurityAutoConfiguration.class,
                    OAuth2ResourceServerAutoConfiguration.class,
                    OAuth2OpaqueAutoConfiguration.class));

    @Test
    @DisplayName("Opaque模式应创建带二次校验的默认introspector")
    void opaqueModeCreatesValidatingIntrospector() {
        runner.withPropertyValues(validOpaqueProperties())
                .run(context -> {
                    assertThat(context).hasSingleBean(OpaqueTokenIntrospector.class);
                    assertThat(context.getBean(OpaqueTokenIntrospector.class))
                            .isInstanceOf(ValidatingOpaqueTokenIntrospector.class);
                });
    }

    @Test
    @DisplayName("自定义OpaqueTokenIntrospector应覆盖默认实现")
    void customIntrospectorShouldBackOffDefaultIntrospector() {
        OpaqueTokenIntrospector custom = mock(OpaqueTokenIntrospector.class);

        runner.withPropertyValues(validOpaqueProperties())
                .withBean(OpaqueTokenIntrospector.class, () -> custom)
                .run(context -> assertThat(context.getBean(OpaqueTokenIntrospector.class)).isSameAs(custom));
    }

    @Test
    @DisplayName("默认关闭或JWT模式不应创建Opaque introspector")
    void disabledOrJwtModeShouldNotCreateOpaqueIntrospector() {
        runner.run(context -> assertThat(context).doesNotHaveBean(OpaqueTokenIntrospector.class));

        runner.withPropertyValues(
                        "ycr.auth.oauth2.resource-server.enabled=true",
                        "ycr.auth.oauth2.resource-server.mode=jwt",
                        "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com",
                        "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api")
                .run(context -> assertThat(context).doesNotHaveBean(OpaqueTokenIntrospector.class));
    }

    private String[] validOpaqueProperties() {
        return new String[]{
                "ycr.auth.oauth2.resource-server.enabled=true",
                "ycr.auth.oauth2.resource-server.mode=opaque",
                "ycr.auth.oauth2.resource-server.opaque.introspection-uri=https://idp.example.com/introspect",
                "ycr.auth.oauth2.resource-server.opaque.client-id=test-client",
                "ycr.auth.oauth2.resource-server.opaque.client-secret=test-secret",
                "ycr.auth.oauth2.resource-server.opaque.audiences[0]=order-api",
                "ycr.auth.oauth2.resource-server.opaque.issuer=https://idp.example.com"
        };
    }
}
