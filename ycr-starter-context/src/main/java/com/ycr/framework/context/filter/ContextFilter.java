package com.ycr.framework.context.filter;

import com.ycr.framework.context.autoconfigure.ContextProperties;
import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.constant.ContextMdcConstants;
import com.ycr.framework.context.enums.UserContextSource;
import com.ycr.framework.context.holder.AppContextHolder;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.AppContext;
import com.ycr.framework.context.model.TenantContext;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.context.resolver.UserContextResolveRequest;
import com.ycr.framework.context.resolver.UserContextResolverChain;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 上下文过滤器
 *
 * <p>职责有二：</p>
 * <ol>
 *     <li><b>还原</b>：根据 {@link ContextProperties#effectiveSecurityMode()} 通过解析链还原用户上下文。</li>
 *     <li><b>清理</b>：无论是否还原，请求结束都会清空所有上下文 Holder，避免线程池复用导致的身份串号。</li>
 * </ol>
 *
 * <p>过滤器排在最前（最外层），确保清理动作覆盖整个请求处理链。</p>
 *
 * @author ycr
 */
public class ContextFilter implements Filter {

    private final ContextProperties properties;

    private final UserContextResolverChain resolverChain;

    public ContextFilter(ContextProperties properties, UserContextResolverChain resolverChain) {
        this.properties = properties;
        this.resolverChain = resolverChain;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (request instanceof HttpServletRequest httpRequest) {
                restore(httpRequest);
            }
            chain.doFilter(request, response);
        } finally {
            // 必须在 finally 中清理，防止异常路径下上下文残留并被后续请求复用
            UserContextHolder.clear();
            TenantContextHolder.clear();
            AppContextHolder.clear();
            clearMdc();
        }
    }

    /**
     * 从解析链还原上下文。
     */
    private void restore(HttpServletRequest request) {
        UserContext userContext = resolverChain.resolve(new UserContextResolveRequest(
                request,
                properties.effectiveSecurityMode(),
                request.getHeader(ContextHeaderConstants.HEADER_TRACE_ID)));
        if (userContext == null) {
            return;
        }
        UserContextHolder.set(userContext);
        restoreMdc(userContext);
        restoreTenantContext(userContext, request);
        restoreAppContext(userContext, request);
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

    private void clearMdc() {
        MDC.remove(ContextMdcConstants.USER_ID);
        MDC.remove(ContextMdcConstants.TENANT_ID);
        MDC.remove(ContextMdcConstants.CLIENT_ID);
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

    /**
     * 只有签名上下文来源可以继续读取上下文 Header 中的附加信息。
     */
    private String trustedHeader(UserContext userContext, HttpServletRequest request, String headerName) {
        if (!UserContextSource.GATEWAY_HEADER.name().equals(userContext.getSource())) {
            return null;
        }
        return request.getHeader(headerName);
    }
}
