package com.ycr.framework.feign.interceptor;

import com.ycr.framework.context.autoconfigure.ContextProperties;
import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.holder.AppContextHolder;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.AppContext;
import com.ycr.framework.context.model.TenantContext;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.context.sign.ContextHeaderSigner;
import com.ycr.framework.context.sign.ContextHeaderSnapshot;
import com.ycr.framework.context.util.ContextValueUtils;
import com.ycr.framework.trace.util.TraceUtils;
import feign.RequestTemplate;

import java.util.Collection;
import java.util.UUID;

/**
 * Feign 上下文透传拦截器
 *
 * <p>把当前线程的用户/租户/应用上下文与 TraceId 写入下游请求头，使身份与链路信息跨服务传递。
 * 逐项判空，不写入空头。下游由 {@code ContextFilter}/{@code TraceFilter} 还原。</p>
 *
 * @author ycr
 */
public class ContextPassInterceptor extends AbstractMatchableFeignInterceptor {

    private final ContextProperties contextProperties;

    private final ContextHeaderSigner signer;

    public ContextPassInterceptor() {
        ContextProperties properties = new ContextProperties();
        properties.getHeaderSign().setEnabled(false);
        this.contextProperties = properties;
        this.signer = new ContextHeaderSigner();
    }

    public ContextPassInterceptor(ContextProperties contextProperties, ContextHeaderSigner signer) {
        this.contextProperties = contextProperties;
        this.signer = signer;
    }

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
            if (user.getNickname() != null) {
                template.header(ContextHeaderConstants.HEADER_NICKNAME, user.getNickname());
            }
            if (user.getDeptId() != null) {
                template.header(ContextHeaderConstants.HEADER_DEPT_ID, String.valueOf(user.getDeptId()));
            }
            if (user.getTenantId() != null) {
                template.header(ContextHeaderConstants.HEADER_TENANT_ID, String.valueOf(user.getTenantId()));
            }
            writeCollection(template, ContextHeaderConstants.HEADER_ROLES, user.getRoles());
            writeCollection(template, ContextHeaderConstants.HEADER_PERMISSIONS, user.getPermissions());
            if (user.getClientId() != null) {
                template.header(ContextHeaderConstants.HEADER_CLIENT_ID, user.getClientId());
            }
            if (user.getSource() != null) {
                template.header(ContextHeaderConstants.HEADER_USER_SOURCE, user.getSource());
            }
        }

        TenantContext tenant = TenantContextHolder.get();
        if (tenant != null) {
            if (tenant.getTenantId() != null && !template.headers().containsKey(ContextHeaderConstants.HEADER_TENANT_ID)) {
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

        signContextHeaders(template);
    }

    private void writeCollection(RequestTemplate template, String headerName, Collection<String> values) {
        String joined = ContextValueUtils.joinCommaSeparated(values);
        if (!joined.isEmpty()) {
            template.header(headerName, joined);
        }
    }

    private void signContextHeaders(RequestTemplate template) {
        if (!contextProperties.getHeaderSign().isEnabled() || !hasIdentityHeader(template)) {
            return;
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = UUID.randomUUID().toString();
        template.header(contextProperties.getHeaderSign().getTimestampHeader(), timestamp);
        template.header(contextProperties.getHeaderSign().getNonceHeader(), nonce);

        ContextHeaderSnapshot snapshot = new ContextHeaderSnapshot();
        snapshot.setMethod(template.method());
        snapshot.setPath(template.path());
        snapshot.setTimestamp(timestamp);
        snapshot.setNonce(nonce);
        snapshot.setUserId(firstHeader(template, ContextHeaderConstants.HEADER_USER_ID));
        snapshot.setUsername(firstHeader(template, ContextHeaderConstants.HEADER_USERNAME));
        snapshot.setTenantId(firstHeader(template, ContextHeaderConstants.HEADER_TENANT_ID));
        snapshot.setDeptId(firstHeader(template, ContextHeaderConstants.HEADER_DEPT_ID));
        snapshot.setRoles(firstHeader(template, ContextHeaderConstants.HEADER_ROLES));
        snapshot.setPermissions(firstHeader(template, ContextHeaderConstants.HEADER_PERMISSIONS));
        snapshot.setClientId(firstHeader(template, ContextHeaderConstants.HEADER_CLIENT_ID));
        snapshot.setTraceId(firstHeader(template, TraceUtils.HEADER_TRACE_ID));
        template.header(contextProperties.getHeaderSign().getSignatureHeader(),
                signer.sign(snapshot, contextProperties.getHeaderSign().getSecret()));
    }

    private boolean hasIdentityHeader(RequestTemplate template) {
        return template.headers().containsKey(ContextHeaderConstants.HEADER_USER_ID)
                || template.headers().containsKey(ContextHeaderConstants.HEADER_USERNAME)
                || template.headers().containsKey(ContextHeaderConstants.HEADER_TENANT_ID)
                || template.headers().containsKey(ContextHeaderConstants.HEADER_ROLES)
                || template.headers().containsKey(ContextHeaderConstants.HEADER_PERMISSIONS);
    }

    private String firstHeader(RequestTemplate template, String headerName) {
        Collection<String> values = template.headers().get(headerName);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.iterator().next();
    }
}
