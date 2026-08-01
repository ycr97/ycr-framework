package com.ycr.framework.auth.oauth2.integration;

import com.ycr.framework.context.constant.ContextMdcConstants;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OAuth2JwtWebIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "ycr.auth.oauth2.resource-server.enabled=true",
        "ycr.auth.oauth2.resource-server.mode=jwt",
        "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com",
        "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api",
        "ycr.auth.oauth2.resource-server.permit-paths[0]=/api/public",
        "ycr.web.cors.enabled=true",
        "ycr.web.cors.allowed-origins[0]=https://client.example",
        "ycr.web.cors.allowed-methods[0]=GET",
        "ycr.web.cors.allowed-headers[0]=Authorization"
})
class OAuth2JwtWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void clearRequestContext() {
        UserContextHolder.clear();
        TenantContextHolder.clear();
        MDC.clear();
    }

    @Test
    @DisplayName("无token应返回YCR 401和Bearer响应头")
    void missingTokenReturnsYcrUnauthorizedResponse() throws Exception {
        mockMvc.perform(get("/api/context"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.msg").value("未登录或登录已过期"));
    }

    @Test
    @DisplayName("malformed、错签名、issuer、audience、过期和nbf未到token均应返回401")
    void invalidJwtTokensReturnUnauthorizedResponse() throws Exception {
        Instant now = Instant.now();
        List<String> invalidTokens = List.of(
                "not-a-jwt",
                OAuth2WebTestSupport.token(OAuth2WebTestSupport.OTHER_SIGNING_KEY, "alice",
                        OAuth2WebTestSupport.ISSUER, List.of(OAuth2WebTestSupport.AUDIENCE),
                        now.minusSeconds(5), now.plusSeconds(300), Map.of("user_id", 1001L)),
                OAuth2WebTestSupport.token(OAuth2WebTestSupport.SIGNING_KEY, "alice",
                        "https://other-idp.example.com", List.of(OAuth2WebTestSupport.AUDIENCE),
                        now.minusSeconds(5), now.plusSeconds(300), Map.of("user_id", 1001L)),
                OAuth2WebTestSupport.token(OAuth2WebTestSupport.SIGNING_KEY, "alice",
                        OAuth2WebTestSupport.ISSUER, List.of("other-api"),
                        now.minusSeconds(5), now.plusSeconds(300), Map.of("user_id", 1001L)),
                OAuth2WebTestSupport.token(OAuth2WebTestSupport.SIGNING_KEY, "alice",
                        OAuth2WebTestSupport.ISSUER, List.of(OAuth2WebTestSupport.AUDIENCE),
                        now.minusSeconds(600), now.minusSeconds(300), Map.of("user_id", 1001L)),
                OAuth2WebTestSupport.tokenWithNotBefore(now.plusSeconds(300)));

        for (String token : invalidTokens) {
            mockMvc.perform(get("/api/context").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate", containsString("Bearer")))
                    .andExpect(jsonPath("$.code").value("401"));
        }
    }

    @Test
    @DisplayName("合法JWT应写入完整UserContext并在请求后清理Holder和MDC")
    void validJwtBindsContextAndCleansThreadState() throws Exception {
        mockMvc.perform(get("/api/context")
                        .header("Authorization", "Bearer " + OAuth2WebTestSupport.validToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1001))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.tenantId").value(42))
                .andExpect(jsonPath("$.clientId").value("web"))
                .andExpect(jsonPath("$.source").value("TOKEN"))
                .andExpect(jsonPath("$.tenantContextId").value(42))
                .andExpect(jsonPath("$.mdcUserId").value("1001"));

        org.assertj.core.api.Assertions.assertThat(UserContextHolder.get()).isNull();
        org.assertj.core.api.Assertions.assertThat(TenantContextHolder.get()).isNull();
        org.assertj.core.api.Assertions.assertThat(MDC.get(ContextMdcConstants.USER_ID)).isNull();
    }

    @Test
    @DisplayName("RequirePermission应允许有权限JWT并拒绝无权限JWT")
    void requirePermissionAllowsAndRejectsByMappedClaims() throws Exception {
        mockMvc.perform(get("/api/permission")
                        .header("Authorization", "Bearer " + OAuth2WebTestSupport.validToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("permission-granted"));

        mockMvc.perform(get("/api/permission")
                        .header("Authorization", "Bearer "
                                + OAuth2WebTestSupport.tokenWithClaims(Map.of(
                                "permissions", List.of("order:write")))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("permit path应在无token时放行")
    void permitPathAllowsAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("public"));
    }

    @Test
    @DisplayName("CORS预检请求应放行并返回允许来源")
    void corsPreflightRequestIsPermitted() throws Exception {
        mockMvc.perform(options("/api/context")
                        .header("Origin", "https://client.example")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://client.example"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({OAuth2WebTestController.class, JwtTestConfiguration.class})
    static class TestApplication {
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class JwtTestConfiguration {

        @Bean
        JwtDecoder jwtDecoder() {
            return OAuth2WebTestSupport.jwtDecoder();
        }
    }
}
