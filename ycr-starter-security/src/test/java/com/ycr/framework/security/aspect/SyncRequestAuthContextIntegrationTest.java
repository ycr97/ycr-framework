package com.ycr.framework.security.aspect;

import com.ycr.framework.context.autoconfigure.ContextProperties;
import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.enums.SecurityMode;
import com.ycr.framework.context.holder.AppContextHolder;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.resolver.SignedHeaderUserContextResolver;
import com.ycr.framework.context.resolver.UserContextResolver;
import com.ycr.framework.context.resolver.UserContextResolverChain;
import com.ycr.framework.context.sign.ContextHeaderSigner;
import com.ycr.framework.context.sign.ContextHeaderSnapshot;
import com.ycr.framework.context.filter.ContextFilter;
import com.ycr.framework.feign.interceptor.ContextPassInterceptor;
import com.ycr.framework.feign.interceptor.RequestTemplateMatchers;
import com.ycr.framework.security.annotation.RequirePermission;
import com.ycr.framework.security.autoconfigure.SecurityAutoConfiguration;
import com.ycr.framework.trace.filter.TraceFilter;
import com.ycr.framework.trace.util.TraceUtils;
import feign.Request;
import feign.RequestTemplate;
import feign.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 同步请求认证上下文闭环集成测试。
 *
 * @author ycr
 */
class SyncRequestAuthContextIntegrationTest {

    private static final String SECRET = "integration-secret";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class, SecurityAutoConfiguration.class))
            .withPropertyValues("ycr.security.enabled=true")
            .withUserConfiguration(TestConfig.class);

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
        TenantContextHolder.clear();
        AppContextHolder.clear();
        TraceUtils.removeTraceId();
    }

    @Test
    @DisplayName("请求链路应还原上下文鉴权并向Feign传播签名上下文")
    void shouldMatchExpectedBehavior001() throws Exception {
        runner.run(context -> {
            ContextProperties properties = contextProperties();
            TraceFilter traceFilter = new TraceFilter(
                    ContextHeaderConstants.HEADER_TRACE_ID,
                    TraceUtils.HEADER_REQUEST_ID);
            ContextFilter contextFilter = new ContextFilter(properties, resolverChain(properties));
            ContextPassInterceptor feignInterceptor = new ContextPassInterceptor(properties, new ContextHeaderSigner());
            feignInterceptor.addMatcher(RequestTemplateMatchers.clientName("order-service"));

            AtomicReference<RequestTemplate> capturedTemplate = new AtomicReference<>();
            MockHttpServletRequest request = signedRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            traceFilter.doFilter(request, response, (traceReq, traceResp) ->
                    contextFilter.doFilter(traceReq, traceResp, (contextReq, contextResp) -> {
                        assertEquals(1001L, UserContextHolder.getUserId());
                        assertEquals(10L, TenantContextHolder.get().getTenantId());
                        assertEquals("app-1", AppContextHolder.get().getAppId());
                        assertEquals("created", context.getBean(TestService.class).create());

                        RequestTemplate template = new RequestTemplate()
                                .method(Request.HttpMethod.POST)
                                .uri("/downstream/orders")
                                .feignTarget(new Target.HardCodedTarget<>(
                                        Object.class, "order-service", "http://order-service"));
                        feignInterceptor.apply(template);
                        capturedTemplate.set(template);
                    }));

            assertNull(UserContextHolder.get());
            assertNull(TenantContextHolder.get());
            assertNull(AppContextHolder.get());
            assertNull(TraceUtils.getTraceId());

            RequestTemplate template = capturedTemplate.get();
            assertNotNull(template);
            assertTrue(template.headers().get(ContextHeaderConstants.HEADER_USER_ID).contains("1001"));
            assertTrue(template.headers().get(ContextHeaderConstants.HEADER_TENANT_ID).contains("10"));
            assertTrue(template.headers().get(ContextHeaderConstants.HEADER_CONTEXT_SIGNATURE).stream().findFirst().isPresent());
        });
    }

    private ContextProperties contextProperties() {
        ContextProperties properties = new ContextProperties();
        properties.setSecurityMode(SecurityMode.GATEWAY_TRUST);
        properties.getHeaderSign().setSecret(SECRET);
        properties.getHeaderSign().setAudience("current-service");
        return properties;
    }

    private UserContextResolverChain resolverChain(ContextProperties properties) {
        List<UserContextResolver> resolvers = List.of(
                new SignedHeaderUserContextResolver(properties, new ContextHeaderSigner(), (nonce, ttl) -> false));
        return new UserContextResolverChain(resolvers);
    }

    private MockHttpServletRequest signedRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/orders");
        request.addHeader(ContextHeaderConstants.HEADER_TRACE_ID, "trace-integration");
        request.addHeader(ContextHeaderConstants.HEADER_USER_ID, "1001");
        request.addHeader(ContextHeaderConstants.HEADER_USERNAME, "alice");
        request.addHeader(ContextHeaderConstants.HEADER_ROLES, "admin");
        request.addHeader(ContextHeaderConstants.HEADER_PERMISSIONS, "order:create");
        request.addHeader(ContextHeaderConstants.HEADER_TENANT_ID, "10");
        request.addHeader(ContextHeaderConstants.HEADER_TENANT_CODE, "tenant-a");
        request.addHeader(ContextHeaderConstants.HEADER_APP_ID, "app-1");

        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = "integration-nonce";
        request.addHeader(ContextHeaderConstants.HEADER_CONTEXT_TIMESTAMP, timestamp);
        request.addHeader(ContextHeaderConstants.HEADER_CONTEXT_NONCE, nonce);
        request.addHeader(ContextHeaderConstants.HEADER_CONTEXT_AUDIENCE, "current-service");

        ContextHeaderSnapshot snapshot = new ContextHeaderSnapshot();
        snapshot.setMethod("GET");
        snapshot.setPath("/api/orders");
        snapshot.setAudience("current-service");
        snapshot.setTimestamp(timestamp);
        snapshot.setNonce(nonce);
        snapshot.setUserId("1001");
        snapshot.setUsername("alice");
        snapshot.setTenantId("10");
        snapshot.setTenantCode("tenant-a");
        snapshot.setRoles("admin");
        snapshot.setPermissions("order:create");
        snapshot.setAppId("app-1");
        snapshot.setTraceId("trace-integration");
        request.addHeader(ContextHeaderConstants.HEADER_CONTEXT_SIGNATURE,
                new ContextHeaderSigner().sign(snapshot, SECRET));
        return request;
    }

    @Configuration
    static class TestConfig {

        @Bean
        TestService testService() {
            return new TestService();
        }
    }

    static class TestService {

        @RequirePermission("order:create")
        public String create() {
            return "created";
        }
    }
}
