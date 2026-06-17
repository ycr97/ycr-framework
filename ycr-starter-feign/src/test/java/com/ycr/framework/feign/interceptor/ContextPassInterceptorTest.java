package com.ycr.framework.feign.interceptor;

import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.holder.AppContextHolder;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.AppContext;
import com.ycr.framework.context.model.TenantContext;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.trace.util.TraceUtils;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ContextPassInterceptor 真实透传行为测试
 *
 * @author ycr
 */
class ContextPassInterceptorTest {

    private final ContextPassInterceptor interceptor = new ContextPassInterceptor();

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
        TenantContextHolder.clear();
        AppContextHolder.clear();
        TraceUtils.removeTraceId();
    }

    @Test
    void 应透传用户租户应用上下文与TraceId() {
        UserContext user = new UserContext();
        user.setUserId(1001L);
        user.setUsername("zhangsan");
        user.setRoles("admin");
        user.setDeptId(9L);
        UserContextHolder.set(user);

        TenantContext tenant = new TenantContext();
        tenant.setTenantId(100L);
        tenant.setTenantCode("tenant_a");
        TenantContextHolder.set(tenant);

        AppContext app = new AppContext();
        app.setAppId("app-1");
        AppContextHolder.set(app);

        TraceUtils.setTraceId("trace-xyz");

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_USER_ID).contains("1001"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_USERNAME).contains("zhangsan"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_ROLES).contains("admin"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_DEPT_ID).contains("9"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_TENANT_ID).contains("100"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_TENANT_CODE).contains("tenant_a"));
        assertTrue(template.headers().get(ContextHeaderConstants.HEADER_APP_ID).contains("app-1"));
        assertTrue(template.headers().get(TraceUtils.HEADER_TRACE_ID).contains("trace-xyz"));
    }

    @Test
    void 无上下文时不应写入请求头() {
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey(ContextHeaderConstants.HEADER_USER_ID));
        assertFalse(template.headers().containsKey(ContextHeaderConstants.HEADER_TENANT_ID));
        assertFalse(template.headers().containsKey(ContextHeaderConstants.HEADER_APP_ID));
        assertFalse(template.headers().containsKey(TraceUtils.HEADER_TRACE_ID));
    }

    @Test
    void 命中notMatcher时不透传上下文() {
        UserContext user = new UserContext();
        user.setUserId(1001L);
        UserContextHolder.set(user);

        interceptor.addNotMatcher(template -> true);

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey(ContextHeaderConstants.HEADER_USER_ID));
    }
}
