package com.ycr.framework.auth.oauth2.integration;

import com.nimbusds.jose.jwk.JWKSet;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OAuth2DefaultJwtDecoderIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "ycr.auth.oauth2.resource-server.enabled=true",
        "ycr.auth.oauth2.resource-server.mode=jwt",
        "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com",
        "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api",
        "ycr.auth.oauth2.resource-server.jwt.allowed-algorithms[0]=RS256",
        "ycr.auth.oauth2.resource-server.jwt.clock-skew=60s"
})
class OAuth2DefaultJwtDecoderIntegrationTest {

    private static final HttpServer JWK_SERVER = startJwkServer();

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerJwkSetUri(DynamicPropertyRegistry registry) {
        registry.add("ycr.auth.oauth2.resource-server.jwt.jwk-set-uri",
                () -> "http://127.0.0.1:" + JWK_SERVER.getAddress().getPort() + "/jwks");
    }

    @AfterAll
    static void stopJwkServer() {
        JWK_SERVER.stop(0);
    }

    @Test
    @DisplayName("默认Nimbus decoder应验证JWKS、issuer、audience和clockSkew")
    void defaultDecoderEnforcesConfiguredJwtContract() throws Exception {
        String withinClockSkew = OAuth2WebTestSupport.token(
                OAuth2WebTestSupport.SIGNING_KEY,
                "alice",
                OAuth2WebTestSupport.ISSUER,
                List.of(OAuth2WebTestSupport.AUDIENCE),
                Instant.now().minusSeconds(120),
                Instant.now().minusSeconds(30),
                Map.of("user_id", 1001L));
        String outsideClockSkew = OAuth2WebTestSupport.token(
                OAuth2WebTestSupport.SIGNING_KEY,
                "alice",
                OAuth2WebTestSupport.ISSUER,
                List.of(OAuth2WebTestSupport.AUDIENCE),
                Instant.now().minusSeconds(180),
                Instant.now().minusSeconds(120),
                Map.of("user_id", 1001L));
        String wrongAudience = OAuth2WebTestSupport.token(
                OAuth2WebTestSupport.SIGNING_KEY,
                "alice",
                OAuth2WebTestSupport.ISSUER,
                List.of("other-api"),
                Instant.now().minusSeconds(5),
                Instant.now().plusSeconds(300),
                Map.of("user_id", 1001L));

        mockMvc.perform(get("/api/context").header("Authorization", "Bearer " + withinClockSkew))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/context").header("Authorization", "Bearer " + outsideClockSkew))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/context").header("Authorization", "Bearer " + wrongAudience))
                .andExpect(status().isUnauthorized());
    }

    private static HttpServer startJwkServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/jwks", exchange -> {
                byte[] body = new JWKSet(OAuth2WebTestSupport.SIGNING_KEY.toPublicJWK())
                        .toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(OAuth2WebTestController.class)
    static class TestApplication {
    }
}
