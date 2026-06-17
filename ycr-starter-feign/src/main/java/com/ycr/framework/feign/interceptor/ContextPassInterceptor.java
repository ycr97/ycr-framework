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

/**
 * Feign 上下文透传拦截器
 *
 * <p>把当前线程的用户/租户/应用上下文与 TraceId 写入下游请求头，使身份与链路信息跨服务传递。
 * 逐项判空，不写入空头。下游由 {@code ContextFilter}/{@code TraceFilter} 还原。</p>
 *
 * @author ycr
 */
public class ContextPassInterceptor extends AbstractMatchableFeignInterceptor {

    @Override
    protected void doApply(RequestTemplate template) {
        UserContext user = UserContextHolder.get();
        if (user != null) {
            if (user.getUserId() != null) {
                template.header(ContextHeaderConstants.HEADER_USER_ID, String.valueOf(user.getUserId()));
            }
            if (user.getUsername() != null) {
                template.header(ContextHeaderConstants.HEADER_USERNAME, user.getUsername());
            }
            if (user.getRoles() != null) {
                template.header(ContextHeaderConstants.HEADER_ROLES, user.getRoles());
            }
            if (user.getDeptId() != null) {
                template.header(ContextHeaderConstants.HEADER_DEPT_ID, String.valueOf(user.getDeptId()));
            }
        }

        TenantContext tenant = TenantContextHolder.get();
        if (tenant != null) {
            if (tenant.getTenantId() != null) {
                template.header(ContextHeaderConstants.HEADER_TENANT_ID, String.valueOf(tenant.getTenantId()));
            }
            if (tenant.getTenantCode() != null) {
                template.header(ContextHeaderConstants.HEADER_TENANT_CODE, tenant.getTenantCode());
            }
        }

        AppContext app = AppContextHolder.get();
        if (app != null && app.getAppId() != null) {
            template.header(ContextHeaderConstants.HEADER_APP_ID, app.getAppId());
        }

        String traceId = TraceUtils.getTraceId();
        if (traceId != null) {
            template.header(TraceUtils.HEADER_TRACE_ID, traceId);
        }
    }
}
