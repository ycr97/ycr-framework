package com.ycr.framework.auth.oauth2.integration;

import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.enums.UserContextSource;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.sign.ContextHeaderSigner;
import com.ycr.framework.context.sign.ContextHeaderSnapshot;
import com.ycr.framework.context.sign.ContextReplayGuard;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OAuth2MixedWebIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "ycr.auth.oauth2.resource-server.enabled=true",
        "ycr.auth.oauth2.resource-server.mode=jwt",
        "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com",
        "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api",
        "ycr.context.security-mode=MIXED",
        "ycr.context.header-sign.secret=mixed-header-secret",
        "ycr.context.header-sign.audience=order-api"
})
class OAuth2MixedWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void clearRequestContext() {
        UserContextHolder.clear();
        MDC.clear();
        ReplayGuardConfiguration.REPLAY_CHECKS.set(0);
    }

    @Test
    @DisplayName("签名网关上下文与JWT身份一致时应成功并保留网关来源")
    void compatibleSignedGatewayContextIsRetained() throws Exception {
        mockMvc.perform(signedRequest(1001L, "alice", 42L, OAuth2WebTestSupport.validToken(), false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1001))
                .andExpect(jsonPath("$.tenantId").value(42))
                .andExpect(jsonPath("$.source").value(UserContextSource.GATEWAY_HEADER.name()));

        assertThat(ReplayGuardConfiguration.REPLAY_CHECKS).hasValue(1);
        assertThat(UserContextHolder.get()).isNull();
    }

    @Test
    @DisplayName("MIXED模式应拒绝userId、username和tenantId冲突")
    void mixedModeRejectsIdentityAndTenantConflicts() throws Exception {
        mockMvc.perform(signedRequest(2002L, "alice", 42L, OAuth2WebTestSupport.validToken(), false))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"));

        mockMvc.perform(signedRequest(null, "alice", 42L,
                        OAuth2WebTestSupport.token(OAuth2WebTestSupport.SIGNING_KEY, "bob",
                                OAuth2WebTestSupport.ISSUER, List.of(OAuth2WebTestSupport.AUDIENCE),
                                Instant.now().minusSeconds(5), Instant.now().plusSeconds(300),
                                Map.of("tenant_id", 42L)), false))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(signedRequest(null, "alice", 42L,
                        OAuth2WebTestSupport.token(OAuth2WebTestSupport.SIGNING_KEY, null,
                                OAuth2WebTestSupport.ISSUER, List.of(OAuth2WebTestSupport.AUDIENCE),
                                Instant.now().minusSeconds(5), Instant.now().plusSeconds(300),
                                Map.of("tenant_id", 42L)), false))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(signedRequest(1001L, "alice", 99L, OAuth2WebTestSupport.validToken(), false))
                .andExpect(status().isUnauthorized());

        assertThat(UserContextHolder.get()).isNull();
    }

    @Test
    @DisplayName("username fallback一致时MIXED模式应通过")
    void mixedModeAcceptsMatchingUsernameFallback() throws Exception {
        String token = OAuth2WebTestSupport.token(OAuth2WebTestSupport.SIGNING_KEY, "alice",
                OAuth2WebTestSupport.ISSUER, List.of(OAuth2WebTestSupport.AUDIENCE),
                Instant.now().minusSeconds(5), Instant.now().plusSeconds(300),
                Map.of("tenant_id", 42L, "azp", "web"));

        mockMvc.perform(signedRequest(null, "alice", 42L, token, false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.source").value(UserContextSource.GATEWAY_HEADER.name()));
    }

    @Test
    @DisplayName("裸身份Header不得绕过HMAC和nonce校验")
    void unsignedGatewayHeadersAreRejected() throws Exception {
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        mockMvc.perform(signedRequest(1001L, "alice", 42L,
                                OAuth2WebTestSupport.validToken(), true)).andReturn())
                .hasMessageContaining("上下文签名校验失败");
    }

    private MockHttpServletRequestBuilder signedRequest(Long userId, String username, Long tenantId,
                                                        String token, boolean tamperSignature) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = UUID.randomUUID().toString();
        ContextHeaderSnapshot snapshot = new ContextHeaderSnapshot();
        snapshot.setMethod("GET");
        snapshot.setPath("/api/context");
        snapshot.setAudience("order-api");
        snapshot.setTimestamp(timestamp);
        snapshot.setNonce(nonce);
        snapshot.setUserId(userId == null ? null : String.valueOf(userId));
        snapshot.setUsername(username);
        snapshot.setTenantId(tenantId == null ? null : String.valueOf(tenantId));
        snapshot.setRoles("user");
        snapshot.setPermissions("order:read");
        snapshot.setClientId("web");

        MockHttpServletRequestBuilder request = get("/api/context")
                .header("Authorization", "Bearer " + token)
                .header(ContextHeaderConstants.HEADER_CONTEXT_AUDIENCE, "order-api")
                .header(ContextHeaderConstants.HEADER_CONTEXT_TIMESTAMP, timestamp)
                .header(ContextHeaderConstants.HEADER_CONTEXT_NONCE, nonce)
                .header(ContextHeaderConstants.HEADER_USERNAME, username)
                .header(ContextHeaderConstants.HEADER_TENANT_ID, String.valueOf(tenantId))
                .header(ContextHeaderConstants.HEADER_ROLES, "user")
                .header(ContextHeaderConstants.HEADER_PERMISSIONS, "order:read")
                .header(ContextHeaderConstants.HEADER_CLIENT_ID, "web");
        if (userId != null) {
            request.header(ContextHeaderConstants.HEADER_USER_ID, String.valueOf(userId));
        }
        String signature = new ContextHeaderSigner().sign(snapshot, OAuth2WebTestSupport.HEADER_SECRET);
        request.header(ContextHeaderConstants.HEADER_CONTEXT_SIGNATURE,
                tamperSignature ? signature + "tampered" : signature);
        return request;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({OAuth2WebTestController.class, JwtTestConfiguration.class, ReplayGuardConfiguration.class})
    static class TestApplication {
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class JwtTestConfiguration {

        @Bean
        JwtDecoder jwtDecoder() {
            return OAuth2WebTestSupport.jwtDecoder();
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ReplayGuardConfiguration {

        private static final AtomicInteger REPLAY_CHECKS = new AtomicInteger();

        @Bean
        ContextReplayGuard contextReplayGuard() {
            return (nonce, ttl) -> {
                REPLAY_CHECKS.incrementAndGet();
                return false;
            };
        }
    }
}
