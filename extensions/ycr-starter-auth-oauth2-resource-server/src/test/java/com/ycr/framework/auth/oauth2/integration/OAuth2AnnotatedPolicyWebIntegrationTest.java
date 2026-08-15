package com.ycr.framework.auth.oauth2.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OAuth2AnnotatedPolicyWebIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "ycr.auth.oauth2.resource-server.enabled=true",
        "ycr.auth.oauth2.resource-server.mode=jwt",
        "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com",
        "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api",
        "ycr.auth.oauth2.resource-server.endpoint-policy=annotated"
})
class OAuth2AnnotatedPolicyWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("annotated策略应放行无注解端点但仍由RequirePermission保护注解端点")
    void annotatedPolicySeparatesEndpointAndMethodAuthorization() throws Exception {
        mockMvc.perform(get("/api/context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").doesNotExist());

        mockMvc.perform(get("/api/permission"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));

        mockMvc.perform(get("/api/permission")
                        .header("Authorization", "Bearer " + OAuth2WebTestSupport.validToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("permission-granted"));
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
