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
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OAuth2OpaqueWebIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "ycr.auth.oauth2.resource-server.enabled=true",
        "ycr.auth.oauth2.resource-server.mode=opaque",
        "ycr.auth.oauth2.resource-server.opaque.introspection-uri=https://idp.example.com/introspect",
        "ycr.auth.oauth2.resource-server.opaque.client-id=test-client",
        "ycr.auth.oauth2.resource-server.opaque.client-secret=test-secret",
        "ycr.auth.oauth2.resource-server.opaque.audiences[0]=order-api",
        "ycr.auth.oauth2.resource-server.opaque.issuer=https://idp.example.com",
        "ycr.auth.oauth2.resource-server.permit-paths[0]=/api/public"
})
class OAuth2OpaqueWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void clearRequestContext() {
        UserContextHolder.clear();
        TenantContextHolder.clear();
        MDC.clear();
    }

    @Test
    @DisplayName("active opaque token应完成claims映射并清理请求上下文")
    void activeOpaqueTokenBindsContextAndCleansThreadState() throws Exception {
        mockMvc.perform(get("/api/context").header("Authorization", "Bearer opaque-active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1001))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.tenantId").value(42))
                .andExpect(jsonPath("$.clientId").value("web"))
                .andExpect(jsonPath("$.source").value("TOKEN"))
                .andExpect(jsonPath("$.mdcUserId").value("1001"));

        org.assertj.core.api.Assertions.assertThat(UserContextHolder.get()).isNull();
        org.assertj.core.api.Assertions.assertThat(TenantContextHolder.get()).isNull();
        org.assertj.core.api.Assertions.assertThat(MDC.get(ContextMdcConstants.USER_ID)).isNull();
    }

    @Test
    @DisplayName("inactive和audience错误opaque token应返回401")
    void invalidOpaqueTokensReturnUnauthorizedResponse() throws Exception {
        for (String token : List.of("opaque-inactive", "opaque-audience-wrong")) {
            mockMvc.perform(get("/api/context").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate", "Bearer"))
                    .andExpect(jsonPath("$.code").value("401"));
        }
    }

    @Test
    @DisplayName("introspection服务异常应映射为503且不泄露底层信息")
    void introspectionFailureReturnsServiceUnavailableResponse() throws Exception {
        mockMvc.perform(get("/api/context").header("Authorization", "Bearer opaque-unavailable"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(header().string("WWW-Authenticate", "Bearer error=\"temporarily_unavailable\""))
                .andExpect(jsonPath("$.code").value("503"))
                .andExpect(jsonPath("$.msg").value("认证服务暂不可用"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(
                        "opaque backend secret"))));
    }

    @Test
    @DisplayName("Opaque claims中的权限应驱动RequirePermission")
    void opaqueClaimsDrivePermissionAuthorization() throws Exception {
        mockMvc.perform(get("/api/permission").header("Authorization", "Bearer opaque-active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("permission-granted"));

        mockMvc.perform(get("/api/permission").header("Authorization", "Bearer opaque-no-permission"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_FORBIDDEN"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({OAuth2WebTestController.class, OpaqueTestConfiguration.class})
    static class TestApplication {
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class OpaqueTestConfiguration {

        @Bean
        OpaqueTokenIntrospector opaqueTokenIntrospector() {
            return token -> {
                if ("opaque-inactive".equals(token)) {
                    throw new BadOpaqueTokenException("inactive opaque token");
                }
                if ("opaque-audience-wrong".equals(token)) {
                    throw new BadOpaqueTokenException("opaque audience mismatch");
                }
                if ("opaque-unavailable".equals(token)) {
                    throw new OAuth2IntrospectionException("opaque backend secret");
                }
                return principal("opaque-no-permission".equals(token) ? List.of("profile")
                        : List.of("order:read"));
            };
        }

        private OAuth2IntrospectionAuthenticatedPrincipal principal(List<String> permissions) {
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("active", true);
            claims.put("aud", List.of("order-api"));
            claims.put("iss", OAuth2WebTestSupport.ISSUER);
            claims.put("user_id", 1001L);
            claims.put("preferred_username", "alice");
            claims.put("tenant_id", 42L);
            claims.put("client_id", "web");
            claims.put("permissions", permissions);
            claims.put("scope", "profile");
            return new OAuth2IntrospectionAuthenticatedPrincipal("alice", claims, List.of());
        }
    }
}
