package com.ycr.framework.context.filter;

import com.ycr.framework.context.autoconfigure.ContextProperties;
import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.holder.AppContextHolder;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 上下文过滤器测试
 *
 * <p>用 {@link MockHttpServletRequest} 模拟带身份头的请求，在自定义 {@link FilterChain} 内部
 * 捕获「请求处理期间」的上下文快照，从而验证：信任开关下的还原行为、请求结束的清理行为、
 * 以及非法数值头的容错。</p>
 */
class ContextFilterTest {

    @AfterEach
    void tearDown() {
        // 防止个别断言失败时上下文外泄影响其它用例
        UserContextHolder.clear();
        TenantContextHolder.clear();
        AppContextHolder.clear();
    }

    private MockHttpServletRequest requestWithHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ContextHeaderConstants.HEADER_USER_ID, "100");
        request.addHeader(ContextHeaderConstants.HEADER_USERNAME, "alice");
        request.addHeader(ContextHeaderConstants.HEADER_ROLES, "admin,user");
        request.addHeader(ContextHeaderConstants.HEADER_DEPT_ID, "9");
        request.addHeader(ContextHeaderConstants.HEADER_TENANT_ID, "1");
        request.addHeader(ContextHeaderConstants.HEADER_TENANT_CODE, "tenant-a");
        request.addHeader(ContextHeaderConstants.HEADER_APP_ID, "app-x");
        return request;
    }

    @Test
    void 信任开关开启时应从请求头还原上下文() throws Exception {
        ContextProperties properties = new ContextProperties();
        properties.setTrustHeaders(true);
        ContextFilter filter = new ContextFilter(properties);

        AtomicReference<UserContext> seen = new AtomicReference<>();
        AtomicReference<String> seenTenant = new AtomicReference<>();
        AtomicReference<String> seenApp = new AtomicReference<>();
        FilterChain chain = (req, resp) -> {
            seen.set(UserContextHolder.get());
            seenTenant.set(TenantContextHolder.get() != null ? TenantContextHolder.get().getTenantCode() : null);
            seenApp.set(AppContextHolder.get() != null ? AppContextHolder.get().getAppId() : null);
        };

        filter.doFilter(requestWithHeaders(), new MockHttpServletResponse(), chain);

        UserContext user = seen.get();
        assertNotNull(user, "请求处理期间应已还原用户上下文");
        assertEquals(100L, user.getUserId());
        assertEquals("alice", user.getUsername());
        assertEquals("admin,user", user.getRoles());
        assertEquals(9L, user.getDeptId());
        assertEquals("tenant-a", seenTenant.get());
        assertEquals("app-x", seenApp.get());
    }

    @Test
    void 信任开关关闭时不应还原上下文() throws Exception {
        ContextProperties properties = new ContextProperties();
        properties.setTrustHeaders(false);
        ContextFilter filter = new ContextFilter(properties);

        AtomicReference<UserContext> seen = new AtomicReference<>();
        FilterChain chain = (req, resp) -> seen.set(UserContextHolder.get());

        filter.doFilter(requestWithHeaders(), new MockHttpServletResponse(), chain);

        assertNull(seen.get(), "信任开关关闭时不应信任请求头，上下文应为空");
    }

    @Test
    void 请求结束后应清理上下文() throws Exception {
        ContextProperties properties = new ContextProperties();
        properties.setTrustHeaders(true);
        ContextFilter filter = new ContextFilter(properties);

        // 链内确认确实被还原过
        AtomicReference<UserContext> seen = new AtomicReference<>();
        filter.doFilter(requestWithHeaders(), new MockHttpServletResponse(),
                (req, resp) -> seen.set(UserContextHolder.get()));

        assertNotNull(seen.get(), "前置条件：请求处理期间应已还原");
        assertNull(UserContextHolder.get(), "请求结束后用户上下文应被清理");
        assertNull(TenantContextHolder.get(), "请求结束后租户上下文应被清理");
        assertNull(AppContextHolder.get(), "请求结束后应用上下文应被清理");
    }

    @Test
    void 链路抛异常时仍应清理上下文() {
        ContextProperties properties = new ContextProperties();
        properties.setTrustHeaders(true);
        ContextFilter filter = new ContextFilter(properties);

        FilterChain chain = (req, resp) -> {
            throw new RuntimeException("业务异常");
        };

        assertThrows(RuntimeException.class,
                () -> filter.doFilter(requestWithHeaders(), new MockHttpServletResponse(), chain));
        assertNull(UserContextHolder.get(), "异常路径下用户上下文也必须被清理");
        assertNull(TenantContextHolder.get(), "异常路径下租户上下文也必须被清理");
        assertNull(AppContextHolder.get(), "异常路径下应用上下文也必须被清理");
    }

    @Test
    void 非法数值头应被忽略且不影响其它字段() throws Exception {
        ContextProperties properties = new ContextProperties();
        properties.setTrustHeaders(true);
        ContextFilter filter = new ContextFilter(properties);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ContextHeaderConstants.HEADER_USER_ID, "not-a-number");
        request.addHeader(ContextHeaderConstants.HEADER_USERNAME, "bob");

        AtomicReference<UserContext> seen = new AtomicReference<>();
        filter.doFilter(request, new MockHttpServletResponse(),
                (req, resp) -> seen.set(UserContextHolder.get()));

        UserContext user = seen.get();
        assertNotNull(user, "用户名存在时仍应建立上下文");
        assertNull(user.getUserId(), "非法的 userId 头应被忽略为 null");
        assertEquals("bob", user.getUsername());
    }
}
