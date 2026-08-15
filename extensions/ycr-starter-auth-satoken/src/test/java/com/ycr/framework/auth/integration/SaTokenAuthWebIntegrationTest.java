package com.ycr.framework.auth.integration;

import cn.dev33.satoken.stp.StpUtil;
import com.ycr.framework.auth.session.SaTokenSessionManager;
import com.ycr.framework.context.holder.AppContextHolder;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.security.annotation.RequirePermission;
import com.ycr.framework.security.util.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = SaTokenAuthWebIntegrationTest.TestApplication.class,
        properties = {
                "ycr.auth.satoken.enabled=true",
                "ycr.auth.satoken.permit-paths[0]=/login",
                "ycr.auth.satoken.permit-paths[1]=/public",
                "ycr.auth.satoken.permit-paths[2]=/error",
                "ycr.auth.satoken.auth-domain=web-integration-test",
                "ycr.web.cors.enabled=true",
                "ycr.web.cors.allowed-origins[0]=https://frontend.example",
                "spring.main.banner-mode=off"
        })
@AutoConfigureMockMvc
class SaTokenAuthWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("默认端点门禁应覆盖登录、上下文恢复、方法鉴权与登出失效")
    void authenticatedEndpointPolicyShouldCompleteTheSessionLifecycle() throws Exception {
        assertThat(StpUtil.getStpLogic().getLoginType()).isEqualTo("web-integration-test");

        mockMvc.perform(get("/private"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/public"))
                .andExpect(status().isOk())
                .andExpect(content().string("public"));

        String token = mockMvc.perform(post("/login"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(get("/private").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("1001"));

        mockMvc.perform(get("/orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("orders"));

        mockMvc.perform(get("/admin").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/private").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        assertThat(UserContextHolder.get()).isNull();
        assertThat(TenantContextHolder.get()).isNull();
        assertThat(AppContextHolder.get()).isNull();
    }

    @Test
    @DisplayName("CORS预检请求应绕过登录门禁")
    void corsPreflightShouldBypassLoginInterceptor() throws Exception {
        mockMvc.perform(options("/private")
                        .header(HttpHeaders.ORIGIN, "https://frontend.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, HttpHeaders.AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "https://frontend.example"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {

        @Bean
        TestController testController(SaTokenSessionManager sessionManager) {
            return new TestController(sessionManager);
        }
    }

    @RestController
    static class TestController {

        private final SaTokenSessionManager sessionManager;

        TestController(SaTokenSessionManager sessionManager) {
            this.sessionManager = sessionManager;
        }

        @PostMapping("/login")
        String login() {
            UserContext userContext = new UserContext();
            userContext.setUserId(1001L);
            userContext.setUsername("alice");
            userContext.setRoles(Set.of("user"));
            userContext.setPermissions(Set.of("order:read"));
            return sessionManager.login(userContext).getTokenValue();
        }

        @PostMapping("/logout")
        String logout() {
            sessionManager.logout();
            return "logout";
        }

        @GetMapping("/public")
        String publicEndpoint() {
            return "public";
        }

        @GetMapping("/private")
        String privateEndpoint() {
            return String.valueOf(SecurityUtils.getUserId());
        }

        @GetMapping("/orders")
        @RequirePermission("order:read")
        String orders() {
            return "orders";
        }

        @GetMapping("/admin")
        @RequirePermission("admin:access")
        String admin() {
            return "admin";
        }
    }
}
