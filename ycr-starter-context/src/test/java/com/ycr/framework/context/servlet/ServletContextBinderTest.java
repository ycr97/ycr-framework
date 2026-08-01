package com.ycr.framework.context.servlet;

import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.constant.ContextMdcConstants;
import com.ycr.framework.context.enums.UserContextSource;
import com.ycr.framework.context.holder.AppContextHolder;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServletContextBinderTest {

    private final ServletContextBinder binder = new ServletContextBinder();

    @AfterEach
    void clearContext() {
        binder.clear();
    }

    @Test
    @DisplayName("签名上下文应绑定Holder、MDC以及可信租户和应用头")
    void bindsGatewayContextAndTrustedHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ContextHeaderConstants.HEADER_TENANT_CODE, "tenant-a");
        request.addHeader(ContextHeaderConstants.HEADER_APP_ID, "app-a");
        UserContext context = context(UserContextSource.GATEWAY_HEADER, 100L, 10L, "web");

        binder.bind(context, request);

        assertEquals(context, UserContextHolder.get());
        assertEquals(10L, TenantContextHolder.getTenantId());
        assertEquals("tenant-a", TenantContextHolder.get().getTenantCode());
        assertEquals("app-a", AppContextHolder.get().getAppId());
        assertEquals("100", MDC.get(ContextMdcConstants.USER_ID));
        assertEquals("10", MDC.get(ContextMdcConstants.TENANT_ID));
        assertEquals("web", MDC.get(ContextMdcConstants.CLIENT_ID));
    }

    @Test
    @DisplayName("token上下文不得读取裸租户和应用头")
    void ignoresUntrustedHeadersForTokenContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ContextHeaderConstants.HEADER_TENANT_CODE, "tenant-evil");
        request.addHeader(ContextHeaderConstants.HEADER_APP_ID, "app-evil");
        UserContext context = context(UserContextSource.TOKEN, 100L, 10L, null);

        binder.bind(context, request);

        assertEquals(10L, TenantContextHolder.getTenantId());
        assertNull(TenantContextHolder.get().getTenantCode());
        assertNull(AppContextHolder.get());
    }

    @Test
    @DisplayName("重新绑定前应清理旧Holder和MDC")
    void clearsPreviousContextBeforeBinding() {
        UserContextHolder.set(context(UserContextSource.GATEWAY_HEADER, 1L, 1L, "old"));
        MDC.put(ContextMdcConstants.USER_ID, "1");
        MDC.put(ContextMdcConstants.TENANT_ID, "1");
        MDC.put(ContextMdcConstants.CLIENT_ID, "old");
        TenantContextHolder.set(new com.ycr.framework.context.model.TenantContext());
        AppContextHolder.set(new com.ycr.framework.context.model.AppContext());

        binder.bind(context(UserContextSource.TOKEN, 2L, null, "new"), new MockHttpServletRequest());

        assertEquals(2L, UserContextHolder.getUserId());
        assertNull(TenantContextHolder.get());
        assertNull(AppContextHolder.get());
        assertEquals("2", MDC.get(ContextMdcConstants.USER_ID));
        assertNull(MDC.get(ContextMdcConstants.TENANT_ID));
        assertEquals("new", MDC.get(ContextMdcConstants.CLIENT_ID));
    }

    private UserContext context(UserContextSource source, Long userId, Long tenantId, String clientId) {
        UserContext context = new UserContext();
        context.setSource(source.name());
        context.setUserId(userId);
        context.setTenantId(tenantId);
        context.setClientId(clientId);
        return context;
    }
}
