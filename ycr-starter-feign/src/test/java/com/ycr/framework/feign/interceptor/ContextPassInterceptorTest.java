package com.ycr.framework.feign.interceptor;

import com.ycr.framework.context.autoconfigure.ContextProperties;
import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.enums.SecurityMode;
import com.ycr.framework.context.enums.UserContextSource;
import com.ycr.framework.context.holder.AppContextHolder;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.AppContext;
import com.ycr.framework.context.model.TenantContext;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.context.resolver.SignedHeaderUserContextResolver;
import com.ycr.framework.context.resolver.UserContextResolveRequest;
import com.ycr.framework.context.sign.ContextHeaderSigner;
import com.ycr.framework.trace.util.TraceUtils;
import feign.Request;
import feign.RequestTemplate;
import feign.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ContextPassInterceptor 真实透传行为测试
 *
 * @author ycr
 */
class ContextPassInterceptorTest {

    private static final String SECRET = "feign-context-secret";

    private ContextProperties contextProperties;

    private ContextPassInterceptor interceptor;

    @BeforeEach
    void setUp() {
        contextProperties = new ContextProperties();
        contextProperties.setSecurityMode(SecurityMode.GATEWAY_TRUST);
        contextProperties.getHeaderSign().setSecret(SECRET);
        contextProperties.getHeaderSign().setAudience("order-service");
        interceptor = new ContextPassInterceptor(contextProperties, new ContextHeaderSigner());
        interceptor.addMatcher(RequestTemplateMatchers.clientName("order-service"));
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
        TenantContextHolder.clear();
        AppContextHolder.clear();
        TraceUtils.removeTraceId();
    }

    @Test
    @DisplayName("应透传用户租户应用上下文与TraceId")
    void shouldMatchExpectedBehavior001() {
        UserContext user = new UserContext();
        user.setUserId(1001L);
        user.setUsername("zhangsan");
        user.setNickname("张三");
        user.setRoles(Set.of("admin"));
        user.setPermissions(Set.of("order:create"));
        user.setDeptId(9L);
        user.setTenantId(100L);
        user.setClientId("web");
        user.setSource(UserContextSource.TOKEN.name());
        UserContextHolder.set(user);

        TenantContext tenant = new TenantContext();
        tenant.setTenantId(100L);
        tenant.setTenantCode("tenant_a");
        TenantContextHolder.set(tenant);

        AppContext app = new AppContext();
        app.setAppId("app-1");
        AppContextHolder.set(app);

        TraceUtils.setTraceId("trace-xyz");

        RequestTemplate template = new RequestTemplate()
                .method(Request.HttpMethod.GET)
                .uri("/api/orders")
                .feignTarget(new Target.HardCodedTarget<>(Object.class, "order-service", "http://order-service"));
        interceptor.apply(template);

        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_USER_ID).contains("1001"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_USERNAME).contains("zhangsan"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_NICKNAME).contains("张三"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_ROLES).contains("admin"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_PERMISSIONS).contains("order:create"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_DEPT_ID).contains("9"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_TENANT_ID).contains("100"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_TENANT_CODE).contains("tenant_a"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_APP_ID).contains("app-1"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_CLIENT_ID).contains("web"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_USER_SOURCE).contains(UserContextSource.TOKEN.name()));
        assertTrue(template.headers().get(TraceUtils.HEADER_TRACE_ID).contains("trace-xyz"));
        assertTrue(template.headers().containsKey(ContextHeaderConstants.HEADER_CONTEXT_TIMESTAMP));
        assertTrue(template.headers().containsKey(ContextHeaderConstants.HEADER_CONTEXT_NONCE));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_CONTEXT_AUDIENCE).contains("order-service"));
        assertTrue(template.headers().containsKey(ContextHeaderConstants.HEADER_CONTEXT_SIGNATURE));

        MockHttpServletRequest request = toRequest(template, "GET", "/api/orders");
        SignedHeaderUserContextResolver resolver = new SignedHeaderUserContextResolver(
                contextProperties,
                new ContextHeaderSigner(),
                (nonce, ttl) -> false);
        UserContext resolved = resolver.resolve(new UserContextResolveRequest(
                request,
                SecurityMode.GATEWAY_TRUST,
                "trace-xyz"));

        assertNotNull(resolved);
        assertEquals(1001L, resolved.getUserId());
        assertEquals(Set.of("admin"), resolved.getRoles());
        assertEquals(Set.of("order:create"), resolved.getPermissions());
    }

    @Test
    @DisplayName("无上下文时不应写入请求头")
    void shouldMatchExpectedBehavior002() {
        RequestTemplate template = new RequestTemplate();
        template.feignTarget(new Target.HardCodedTarget<>(Object.class, "order-service", "http://order-service"));
        interceptor.apply(template);

        assertFalse(template.headers().containsKey(ContextHeaderConstants.HEADER_USER_ID));
        assertFalse(template.headers().containsKey(ContextHeaderConstants.HEADER_TENANT_ID));
        assertFalse(template.headers().containsKey(ContextHeaderConstants.HEADER_APP_ID));
        assertFalse(template.headers().containsKey(TraceUtils.HEADER_TRACE_ID));
    }

    @Test
    @DisplayName("命中notMatcher时不透传上下文")
    void shouldMatchExpectedBehavior003() {
        UserContext user = new UserContext();
        user.setUserId(1001L);
        UserContextHolder.set(user);

        interceptor.addNotMatcher(template -> true);

        RequestTemplate template = new RequestTemplate();
        template.feignTarget(new Target.HardCodedTarget<>(Object.class, "order-service", "http://order-service"));
        interceptor.apply(template);

        assertFalse(template.headers().containsKey(ContextHeaderConstants.HEADER_USER_ID));
    }

    @Test
    @DisplayName("非允许的Feign client不应收到身份上下文")
    void shouldNotPassIdentityToUnlistedClient() {
        UserContext user = new UserContext();
        user.setUserId(1001L);
        UserContextHolder.set(user);
        RequestTemplate template = new RequestTemplate()
                .method(Request.HttpMethod.GET)
                .uri("/external")
                .feignTarget(new Target.HardCodedTarget<>(Object.class, "external-api", "https://example.com"));

        interceptor.apply(template);

        assertFalse(template.headers().containsKey(ContextHeaderConstants.HEADER_USER_ID));
        assertFalse(template.headers().containsKey(ContextHeaderConstants.HEADER_CONTEXT_SIGNATURE));
    }

    private MockHttpServletRequest toRequest(RequestTemplate template, String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(path);
        template.headers().forEach((name, values) -> addHeaders(request, name, values));
        return request;
    }

    private void addHeaders(MockHttpServletRequest request, String name, Collection<String> values) {
        values.forEach(value -> request.addHeader(name, value));
    }
}
