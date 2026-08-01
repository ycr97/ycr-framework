package com.ycr.framework.auth.oauth2.introspection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidatingOpaqueTokenIntrospectorTest {

    @Test
    @DisplayName("active introspection结果应通过audience和issuer校验")
    void acceptsMatchingAudienceAndIssuer() {
        OAuth2AuthenticatedPrincipal principal = principal(Map.of(
                "aud", List.of("profile", "order-api"),
                "iss", "https://idp.example.com"));
        OpaqueTokenIntrospector delegate = delegateReturning(principal);

        OAuth2AuthenticatedPrincipal result = new ValidatingOpaqueTokenIntrospector(
                delegate, List.of("order-api"), "https://idp.example.com").introspect("token");

        assertThat(result).isSameAs(principal);
    }

    @Test
    @DisplayName("audience必须至少命中一个且大小写敏感")
    void rejectsInvalidAudience() {
        OAuth2AuthenticatedPrincipal principal = principal(Map.of("aud", "Order-API"));

        assertThatThrownBy(() -> new ValidatingOpaqueTokenIntrospector(
                delegateReturning(principal), List.of("order-api"), null).introspect("token"))
                .isInstanceOf(BadOpaqueTokenException.class);
    }

    @Test
    @DisplayName("配置issuer时必须精确匹配")
    void rejectsInvalidIssuer() {
        OAuth2AuthenticatedPrincipal principal = principal(Map.of(
                "aud", List.of("order-api"),
                "iss", "https://idp.example.com/") );

        assertThatThrownBy(() -> new ValidatingOpaqueTokenIntrospector(
                delegateReturning(principal), List.of("order-api"), "https://idp.example.com").introspect("token"))
                .isInstanceOf(BadOpaqueTokenException.class);
    }

    @Test
    @DisplayName("introspection服务异常应原样保留以便上层映射503")
    void preservesIntrospectionServiceException() {
        OpaqueTokenIntrospector delegate = mock(OpaqueTokenIntrospector.class);
        OAuth2IntrospectionException failure = new OAuth2IntrospectionException("introspection unavailable");
        when(delegate.introspect("token")).thenThrow(failure);

        assertThatThrownBy(() -> new ValidatingOpaqueTokenIntrospector(
                delegate, List.of("order-api"), null).introspect("token"))
                .isSameAs(failure);
    }

    private OpaqueTokenIntrospector delegateReturning(OAuth2AuthenticatedPrincipal principal) {
        OpaqueTokenIntrospector delegate = mock(OpaqueTokenIntrospector.class);
        when(delegate.introspect("token")).thenReturn(principal);
        return delegate;
    }

    private OAuth2AuthenticatedPrincipal principal(Map<String, Object> attributes) {
        return new OAuth2IntrospectionAuthenticatedPrincipal(attributes, List.of());
    }
}
