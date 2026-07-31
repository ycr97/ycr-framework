package com.ycr.framework.context.filter;

import com.ycr.framework.context.autoconfigure.ContextProperties;
import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.constant.ContextMdcConstants;
import com.ycr.framework.context.enums.SecurityMode;
import com.ycr.framework.context.exception.ContextAuthException;
import com.ycr.framework.context.holder.AppContextHolder;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.context.resolver.SignedHeaderUserContextResolver;
import com.ycr.framework.context.resolver.TokenUserContextResolver;
import com.ycr.framework.context.resolver.UserContextResolver;
import com.ycr.framework.context.resolver.UserContextResolverChain;
import com.ycr.framework.context.sign.ContextHeaderSigner;
import com.ycr.framework.context.sign.ContextHeaderSnapshot;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 上下文过滤器测试
 *
 * <p>用 {@link MockHttpServletRequest} 模拟带身份头的请求，在自定义 {@link FilterChain} 内部
 * 捕获「请求处理期间」的上下文快照，从而验证：签名上下文还原、安全模式隔离、请求结束清理。</p>
 */
class ContextFilterTest {

    private static final String SECRET = "context-secret";

    @AfterEach
    void tearDown() {
        // 防止个别断言失败时上下文外泄影响其它用例
        UserContextHolder.clear();
        TenantContextHolder.clear();
        AppContextHolder.clear();
        MDC.clear();
    }

    private ContextProperties gatewayTrustProperties() {
        ContextProperties properties = new ContextProperties();
        properties.setSecurityMode(SecurityMode.GATEWAY_TRUST);
        properties.getHeaderSign().setSecret(SECRET);
        return properties;
    }

    private ContextFilter filter(ContextProperties properties) {
        List<UserContextResolver> resolvers = List.of(
                new SignedHeaderUserContextResolver(properties, new ContextHeaderSigner(), (nonce, ttl) -> false),
                new TokenUserContextResolver());
        return new ContextFilter(properties, new UserContextResolverChain(resolvers));
    }

    private MockHttpServletRequest signedRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/orders");
        request.addHeader(ContextHeaderConstants.HEADER_USER_ID, "100");
        request.addHeader(ContextHeaderConstants.HEADER_USERNAME, "alice");
        request.addHeader(ContextHeaderConstants.HEADER_NICKNAME, "Alice");
        request.addHeader(ContextHeaderConstants.HEADER_ROLES, "admin,user");
        request.addHeader(ContextHeaderConstants.HEADER_PERMISSIONS, "order:create,order:update");
        request.addHeader(ContextHeaderConstants.HEADER_DEPT_ID, "9");
        request.addHeader(ContextHeaderConstants.HEADER_TENANT_ID, "1");
        request.addHeader(ContextHeaderConstants.HEADER_TENANT_CODE, "tenant-a");
        request.addHeader(ContextHeaderConstants.HEADER_APP_ID, "app-x");
        request.addHeader(ContextHeaderConstants.HEADER_CLIENT_ID, "web");
        request.addHeader(ContextHeaderConstants.HEADER_TRACE_ID, "trace-1");

        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = "nonce-1";
        request.addHeader(ContextHeaderConstants.HEADER_CONTEXT_TIMESTAMP, timestamp);
        request.addHeader(ContextHeaderConstants.HEADER_CONTEXT_NONCE, nonce);

        ContextHeaderSnapshot snapshot = new ContextHeaderSnapshot();
        snapshot.setMethod("GET");
        snapshot.setPath("/api/orders");
        snapshot.setTimestamp(timestamp);
        snapshot.setNonce(nonce);
        snapshot.setUserId("100");
        snapshot.setUsername("alice");
        snapshot.setNickname("Alice");
        snapshot.setTenantId("1");
        snapshot.setTenantCode("tenant-a");
        snapshot.setDeptId("9");
        snapshot.setRoles("admin,user");
        snapshot.setPermissions("order:create,order:update");
        snapshot.setClientId("web");
        snapshot.setAppId("app-x");
        snapshot.setTraceId("trace-1");
        request.addHeader(ContextHeaderConstants.HEADER_CONTEXT_SIGNATURE,
                new ContextHeaderSigner().sign(snapshot, SECRET));
        return request;
    }

    @Test
    @DisplayName("gatewayTrust签名正确时应从请求头还原上下文")
    void shouldMatchExpectedBehavior001() throws Exception {
        ContextFilter filter = filter(gatewayTrustProperties());

        AtomicReference<UserContext> seen = new AtomicReference<>();
        AtomicReference<String> seenTenant = new AtomicReference<>();
        AtomicReference<String> seenApp = new AtomicReference<>();
        FilterChain chain = (req, resp) -> {
            seen.set(UserContextHolder.get());
            seenTenant.set(TenantContextHolder.get() != null ? TenantContextHolder.get().getTenantCode() : null);
            seenApp.set(AppContextHolder.get() != null ? AppContextHolder.get().getAppId() : null);
        };

        filter.doFilter(signedRequest(), new MockHttpServletResponse(), chain);

        UserContext user = seen.get();
        assertNotNull(user, "请求处理期间应已还原用户上下文");
        assertEquals(100L, user.getUserId());
        assertEquals("alice", user.getUsername());
        assertEquals("Alice", user.getNickname());
        assertEquals(Set.of("admin", "user"), user.getRoles());
        assertEquals(Set.of("order:create", "order:update"), user.getPermissions());
        assertEquals(9L, user.getDeptId());
        assertEquals(1L, user.getTenantId());
        assertEquals("web", user.getClientId());
        assertEquals("tenant-a", seenTenant.get());
        assertEquals("app-x", seenApp.get());
    }

    @Test
    @DisplayName("tokenVerify模式应忽略裸身份头")
    void shouldMatchExpectedBehavior002() throws Exception {
        ContextProperties properties = new ContextProperties();
        properties.setSecurityMode(SecurityMode.TOKEN_VERIFY);
        ContextFilter filter = filter(properties);

        AtomicReference<UserContext> seen = new AtomicReference<>();
        FilterChain chain = (req, resp) -> seen.set(UserContextHolder.get());

        filter.doFilter(signedRequest(), new MockHttpServletResponse(), chain);

        assertNull(seen.get(), "token-verify 不应信任请求头，上下文应为空");
    }

    @Test
    @DisplayName("gatewayTrust签名缺失时应拒绝请求")
    void shouldMatchExpectedBehavior003() {
        ContextFilter filter = filter(gatewayTrustProperties());
        MockHttpServletRequest request = signedRequest();
        request.removeHeader(ContextHeaderConstants.HEADER_CONTEXT_SIGNATURE);

        assertThrows(ContextAuthException.class,
                () -> filter.doFilter(request, new MockHttpServletResponse(), (req, resp) -> {
                }));
    }

    @Test
    @DisplayName("已签名附加上下文被篡改时应拒绝请求")
    void shouldMatchExpectedBehavior004() {
        ContextFilter filter = filter(gatewayTrustProperties());
        MockHttpServletRequest request = signedRequest();
        request.removeHeader(ContextHeaderConstants.HEADER_APP_ID);
        request.addHeader(ContextHeaderConstants.HEADER_APP_ID, "app-evil");

        assertThrows(ContextAuthException.class,
                () -> filter.doFilter(request, new MockHttpServletResponse(), (req, resp) -> {
                }));
    }

    @Test
    @DisplayName("验签失败时不得占用nonce")
    void shouldMatchExpectedBehavior005() {
        ContextProperties properties = gatewayTrustProperties();
        AtomicInteger replayChecks = new AtomicInteger();
        SignedHeaderUserContextResolver resolver = new SignedHeaderUserContextResolver(
                properties,
                new ContextHeaderSigner(),
                (nonce, ttl) -> {
                    replayChecks.incrementAndGet();
                    return false;
                });
        MockHttpServletRequest request = signedRequest();
        request.removeHeader(ContextHeaderConstants.HEADER_CONTEXT_SIGNATURE);
        request.addHeader(ContextHeaderConstants.HEADER_CONTEXT_SIGNATURE, "invalid");

        assertThrows(ContextAuthException.class,
                () -> resolver.resolve(new com.ycr.framework.context.resolver.UserContextResolveRequest(
                        request, SecurityMode.GATEWAY_TRUST, "trace-1")));
        assertEquals(0, replayChecks.get());
    }

    @Test
    @DisplayName("请求结束后应清理上下文")
    void shouldMatchExpectedBehavior006() throws Exception {
        ContextFilter filter = filter(gatewayTrustProperties());

        // 链内确认确实被还原过
        AtomicReference<UserContext> seen = new AtomicReference<>();
        filter.doFilter(signedRequest(), new MockHttpServletResponse(),
                (req, resp) -> seen.set(UserContextHolder.get()));

        assertNotNull(seen.get(), "前置条件：请求处理期间应已还原");
        assertNull(UserContextHolder.get(), "请求结束后用户上下文应被清理");
        assertNull(TenantContextHolder.get(), "请求结束后租户上下文应被清理");
        assertNull(AppContextHolder.get(), "请求结束后应用上下文应被清理");
    }

    @Test
    @DisplayName("链路抛异常时仍应清理上下文")
    void shouldMatchExpectedBehavior007() {
        ContextFilter filter = filter(gatewayTrustProperties());

        FilterChain chain = (req, resp) -> {
            throw new RuntimeException("业务异常");
        };

        assertThrows(RuntimeException.class,
                () -> filter.doFilter(signedRequest(), new MockHttpServletResponse(), chain));
        assertNull(UserContextHolder.get(), "异常路径下用户上下文也必须被清理");
        assertNull(TenantContextHolder.get(), "异常路径下租户上下文也必须被清理");
        assertNull(AppContextHolder.get(), "异常路径下应用上下文也必须被清理");
    }

    @Test
    @DisplayName("用户租户客户端应在链内写入Mdc并在链外清理")
    void shouldMatchExpectedBehavior008() throws Exception {
        ContextFilter filter = filter(gatewayTrustProperties());

        filter.doFilter(signedRequest(), new MockHttpServletResponse(), (req, resp) -> {
            assertEquals("100", MDC.get(ContextMdcConstants.USER_ID));
            assertEquals("1", MDC.get(ContextMdcConstants.TENANT_ID));
            assertEquals("web", MDC.get(ContextMdcConstants.CLIENT_ID));
        });

        assertNull(MDC.get(ContextMdcConstants.USER_ID));
        assertNull(MDC.get(ContextMdcConstants.TENANT_ID));
        assertNull(MDC.get(ContextMdcConstants.CLIENT_ID));
    }
}
