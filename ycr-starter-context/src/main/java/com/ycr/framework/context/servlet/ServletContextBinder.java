package com.ycr.framework.context.servlet;

import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.constant.ContextMdcConstants;
import com.ycr.framework.context.enums.UserContextSource;
import com.ycr.framework.context.holder.AppContextHolder;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.AppContext;
import com.ycr.framework.context.model.TenantContext;
import com.ycr.framework.context.model.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

/**
 * Servlet 请求中的 YCR 上下文绑定器。
 *
 * @author ycr
 */
public class ServletContextBinder {

    /**
     * 清理旧上下文后绑定当前用户上下文。
     */
    public void bind(UserContext userContext, HttpServletRequest request) {
        clear();
        if (userContext == null) {
            return;
        }
        UserContextHolder.set(userContext);
        restoreMdc(userContext);
        restoreTenantContext(userContext, request);
        restoreAppContext(userContext, request);
    }

    /**
     * 清理当前线程上的全部 YCR 请求上下文。
     */
    public void clear() {
        UserContextHolder.clear();
        TenantContextHolder.clear();
        AppContextHolder.clear();
        MDC.remove(ContextMdcConstants.USER_ID);
        MDC.remove(ContextMdcConstants.TENANT_ID);
        MDC.remove(ContextMdcConstants.CLIENT_ID);
    }

    private void restoreMdc(UserContext userContext) {
        putMdc(ContextMdcConstants.USER_ID, userContext.getUserId());
        putMdc(ContextMdcConstants.TENANT_ID, userContext.getTenantId());
        putMdc(ContextMdcConstants.CLIENT_ID, userContext.getClientId());
    }

    private void putMdc(String key, Object value) {
        if (value != null) {
            MDC.put(key, String.valueOf(value));
        }
    }

    private void restoreTenantContext(UserContext userContext, HttpServletRequest request) {
        Long tenantId = userContext.getTenantId();
        String tenantCode = trustedHeader(userContext, request, ContextHeaderConstants.HEADER_TENANT_CODE);
        if (tenantId == null && !StringUtils.hasText(tenantCode)) {
            return;
        }
        TenantContext tenantContext = new TenantContext();
        tenantContext.setTenantId(tenantId);
        tenantContext.setTenantCode(tenantCode);
        TenantContextHolder.set(tenantContext);
    }

    private void restoreAppContext(UserContext userContext, HttpServletRequest request) {
        String appId = trustedHeader(userContext, request, ContextHeaderConstants.HEADER_APP_ID);
        if (!StringUtils.hasText(appId)) {
            return;
        }
        AppContext appContext = new AppContext();
        appContext.setAppId(appId);
        AppContextHolder.set(appContext);
    }

    private String trustedHeader(UserContext userContext, HttpServletRequest request, String headerName) {
        if (!UserContextSource.GATEWAY_HEADER.name().equals(userContext.getSource())) {
            return null;
        }
        return request.getHeader(headerName);
    }
}
