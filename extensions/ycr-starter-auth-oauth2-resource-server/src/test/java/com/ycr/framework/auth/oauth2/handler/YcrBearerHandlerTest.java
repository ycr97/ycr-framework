package com.ycr.framework.auth.oauth2.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;

import static org.assertj.core.api.Assertions.assertThat;

class YcrBearerHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("认证失败应返回401的R和Bearer挑战")
    void authenticationFailureReturns401() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new YcrBearerAuthenticationEntryPoint(objectMapper).commence(
                new MockHttpServletRequest(), response,
                new AuthenticationServiceException("invalid token"));

        JsonNode body = body(response);
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Bearer");
        assertThat(body.get("code").asText()).isEqualTo("401");
        assertThat(body.get("msg").asText()).isEqualTo("未登录或登录已过期");
    }

    @Test
    @DisplayName("introspection服务异常应返回503而不是伪装成401")
    void introspectionFailureReturns503() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException failure = new AuthenticationServiceException(
                "introspection failed", new OAuth2IntrospectionException("service unavailable"));

        new YcrBearerAuthenticationEntryPoint(objectMapper).commence(
                new MockHttpServletRequest(), response, failure);

        JsonNode body = body(response);
        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getHeader("WWW-Authenticate"))
                .isEqualTo("Bearer error=\"temporarily_unavailable\"");
        assertThat(body.get("code").asText()).isEqualTo("503");
        assertThat(body.get("msg").asText()).isEqualTo("认证服务暂不可用");
    }

    @Test
    @DisplayName("授权失败应返回403和insufficient_scope挑战")
    void accessDeniedReturns403() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new YcrBearerAccessDeniedHandler(objectMapper).handle(
                new MockHttpServletRequest(), response, new AccessDeniedException("denied"));

        JsonNode body = body(response);
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getHeader("WWW-Authenticate"))
                .isEqualTo("Bearer error=\"insufficient_scope\"");
        assertThat(body.get("code").asText()).isEqualTo("403");
        assertThat(body.get("msg").asText()).isEqualTo("权限不足");
    }

    private JsonNode body(MockHttpServletResponse response) throws Exception {
        assertThat(response.getContentType()).startsWith("application/json");
        return objectMapper.readTree(response.getContentAsString());
    }
}
