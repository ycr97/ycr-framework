package com.ycr.framework.context.filter;

import com.ycr.framework.context.autoconfigure.ContextProperties;
import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.holder.AppContextHolder;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.AppContext;
import com.ycr.framework.context.model.TenantContext;
import com.ycr.framework.context.model.UserContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 上下文过滤器
 *
 * <p>职责有二：</p>
 * <ol>
 *     <li><b>还原</b>：当开启 {@link ContextProperties#isTrustHeaders()} 时，从受信任的上游 HTTP Header
 *         （见 {@link ContextHeaderConstants}）还原用户/租户/应用上下文，用于网关后的下游服务。</li>
 *     <li><b>清理</b>：无论是否还原，请求结束都会清空所有上下文 Holder，避免线程池复用导致的身份串号。</li>
 * </ol>
 *
 * <p>过滤器排在最前（最外层），确保清理动作覆盖整个请求处理链。</p>
 *
 * @author ycr
 */
@Slf4j
public class ContextFilter implements Filter {

    private final ContextProperties properties;

    public ContextFilter(ContextProperties properties) {
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (properties.isTrustHeaders() && request instanceof HttpServletRequest httpRequest) {
                restoreFromHeaders(httpRequest);
            }
            chain.doFilter(request, response);
        } finally {
            // 必须在 finally 中清理，防止异常路径下上下文残留并被后续请求复用
            UserContextHolder.clear();
            TenantContextHolder.clear();
            AppContextHolder.clear();
        }
    }

    /**
     * 从请求头还原各类上下文，仅在对应头存在时填充
     */
    private void restoreFromHeaders(HttpServletRequest request) {
        restoreUserContext(request);
        restoreTenantContext(request);
        restoreAppContext(request);
    }

    private void restoreUserContext(HttpServletRequest request) {
        Long userId = parseLong(request.getHeader(ContextHeaderConstants.HEADER_USER_ID));
        String username = request.getHeader(ContextHeaderConstants.HEADER_USERNAME);
        String roles = request.getHeader(ContextHeaderConstants.HEADER_ROLES);
        Long deptId = parseLong(request.getHeader(ContextHeaderConstants.HEADER_DEPT_ID));
        if (userId == null && !StringUtils.hasText(username)) {
            return;
        }
        UserContext userContext = new UserContext();
        userContext.setUserId(userId);
        userContext.setUsername(username);
        userContext.setRoles(roles);
        userContext.setDeptId(deptId);
        UserContextHolder.set(userContext);
    }

    private void restoreTenantContext(HttpServletRequest request) {
        Long tenantId = parseLong(request.getHeader(ContextHeaderConstants.HEADER_TENANT_ID));
        String tenantCode = request.getHeader(ContextHeaderConstants.HEADER_TENANT_CODE);
        if (tenantId == null && !StringUtils.hasText(tenantCode)) {
            return;
        }
        TenantContext tenantContext = new TenantContext();
        tenantContext.setTenantId(tenantId);
        tenantContext.setTenantCode(tenantCode);
        TenantContextHolder.set(tenantContext);
    }

    private void restoreAppContext(HttpServletRequest request) {
        String appId = request.getHeader(ContextHeaderConstants.HEADER_APP_ID);
        if (!StringUtils.hasText(appId)) {
            return;
        }
        AppContext appContext = new AppContext();
        appContext.setAppId(appId);
        AppContextHolder.set(appContext);
    }

    /**
     * 宽松解析 Long：为空或非法时返回 null，不中断请求
     */
    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            log.warn("上下文 Header 数值解析失败，已忽略：{}", value);
            return null;
        }
    }
}
