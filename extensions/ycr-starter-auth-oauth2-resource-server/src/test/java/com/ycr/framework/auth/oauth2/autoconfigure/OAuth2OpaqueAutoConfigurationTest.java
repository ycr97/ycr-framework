package com.ycr.framework.auth.oauth2.autoconfigure;

import com.ycr.framework.auth.oauth2.introspection.ValidatingOpaqueTokenIntrospector;
import com.ycr.framework.security.autoconfigure.SecurityAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

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
                    assertThat(context).doesNotHaveBean(JwtDecoder.class);
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

    @Test
    @DisplayName("默认HTTP introspector应发送Basic Auth并对服务端故障fail-closed")
    void defaultHttpIntrospectorUsesBasicAuthAndFailsClosed() {
        AtomicReference<RestTemplate> restTemplateRef = new AtomicReference<>();
        RestTemplateBuilder builder = new RestTemplateBuilder(restTemplateRef::set);
        String authorization = "Basic " + Base64.getEncoder().encodeToString(
                "test-client:test-secret".getBytes(StandardCharsets.UTF_8));

        runner.withPropertyValues(validOpaqueProperties())
                .withBean(RestTemplateBuilder.class, () -> builder)
                .run(context -> {
                    RestTemplate restTemplate = restTemplateRef.get();
                    assertThat(restTemplate).isNotNull();
                    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
                    server.expect(requestTo("https://idp.example.com/introspect"))
                            .andExpect(method(HttpMethod.POST))
                            .andExpect(header(HttpHeaders.AUTHORIZATION, authorization))
                            .andExpect(content().string("token=opaque-token"))
                            .andRespond(withSuccess(
                                    "{\"active\":true,\"aud\":[\"order-api\"],"
                                            + "\"iss\":\"https://idp.example.com\",\"user_id\":\"1001\"}",
                                    MediaType.APPLICATION_JSON));
                    server.expect(requestTo("https://idp.example.com/introspect"))
                            .andRespond(withServerError().body("upstream-secret-response"));
                    server.expect(requestTo("https://idp.example.com/introspect"))
                            .andRespond(request -> {
                                throw new ResourceAccessException("read timed out with test-secret");
                            });

                    assertThat(context.getBean(OpaqueTokenIntrospector.class).introspect("opaque-token")
                            .getAttributes()).containsEntry("user_id", "1001");
                    assertThatThrownBy(() -> context.getBean(OpaqueTokenIntrospector.class)
                            .introspect("opaque-token"))
                            .isInstanceOf(org.springframework.security.oauth2.server.resource.introspection
                                    .OAuth2IntrospectionException.class)
                            .hasMessageNotContaining("upstream-secret-response")
                            .hasMessageNotContaining("test-secret");
                    assertThatThrownBy(() -> context.getBean(OpaqueTokenIntrospector.class)
                            .introspect("opaque-token"))
                            .isInstanceOf(org.springframework.security.oauth2.server.resource.introspection
                                    .OAuth2IntrospectionException.class)
                            .hasMessageNotContaining("read timed out")
                            .hasMessageNotContaining("test-secret");
                    server.verify();
                });
    }

    @Test
    @DisplayName("默认HTTP introspector应把连接和读取超时传给RestTemplate")
    void defaultHttpIntrospectorPropagatesTimeouts() {
        AtomicReference<ClientHttpRequestFactorySettings> settingsRef = new AtomicReference<>();
        RestTemplateBuilder builder = new RestTemplateBuilder()
                .requestFactory(settings -> {
                    settingsRef.set(settings);
                    return new SimpleClientHttpRequestFactory();
                });

        runner.withPropertyValues(validOpaqueProperties())
                .withPropertyValues(
                        "ycr.auth.oauth2.resource-server.opaque.connect-timeout=125ms",
                        "ycr.auth.oauth2.resource-server.opaque.read-timeout=250ms")
                .withBean(RestTemplateBuilder.class, () -> builder)
                .run(context -> {
                    assertThat(context).hasSingleBean(OpaqueTokenIntrospector.class);
                    assertThat(settingsRef).hasValueSatisfying(settings -> {
                        assertThat(settings.connectTimeout()).isEqualTo(Duration.ofMillis(125));
                        assertThat(settings.readTimeout()).isEqualTo(Duration.ofMillis(250));
                    });
                });
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
