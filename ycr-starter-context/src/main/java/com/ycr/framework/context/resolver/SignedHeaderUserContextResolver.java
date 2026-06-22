package com.ycr.framework.context.resolver;

import com.ycr.framework.context.autoconfigure.ContextProperties;
import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.enums.SecurityMode;
import com.ycr.framework.context.enums.UserContextSource;
import com.ycr.framework.context.exception.ContextAuthException;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.context.sign.ContextHeaderSigner;
import com.ycr.framework.context.sign.ContextHeaderSnapshot;
import com.ycr.framework.context.sign.ContextReplayGuard;
import com.ycr.framework.context.util.ContextValueUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import java.time.Clock;

/**
 * 签名上下文 Header 解析器。
 *
 * @author ycr
 */
public class SignedHeaderUserContextResolver implements UserContextResolver {

    private final ContextProperties properties;

    private final ContextHeaderSigner signer;

    private final ContextReplayGuard replayGuard;

    private final Clock clock;

    public SignedHeaderUserContextResolver(ContextProperties properties, ContextHeaderSigner signer,
                                           ContextReplayGuard replayGuard) {
        this(properties, signer, replayGuard, Clock.systemUTC());
    }

    public SignedHeaderUserContextResolver(ContextProperties properties, ContextHeaderSigner signer,
                                           ContextReplayGuard replayGuard, Clock clock) {
        this.properties = properties;
        this.signer = signer;
        this.replayGuard = replayGuard;
        this.clock = clock;
    }

    @Override
    public boolean supports(UserContextResolveRequest request) {
        return request.getSecurityMode() == SecurityMode.GATEWAY_TRUST
                || request.getSecurityMode() == SecurityMode.MIXED;
    }

    @Override
    public UserContext resolve(UserContextResolveRequest request) {
        HttpServletRequest servletRequest = request.getRequest();
        if (!hasAnyIdentityHeader(servletRequest)) {
            return null;
        }
        ContextHeaderSnapshot snapshot = snapshot(servletRequest, request.getTraceId());
        ContextProperties.HeaderSign sign = properties.getHeaderSign();
        if (sign.isEnabled() && !verify(servletRequest, snapshot, sign)) {
            return null;
        }
        return restoreUser(snapshot, servletRequest);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    private boolean verify(HttpServletRequest request, ContextHeaderSnapshot snapshot, ContextProperties.HeaderSign sign) {
        String signature = request.getHeader(sign.getSignatureHeader());
        if (!StringUtils.hasText(signature)
                || !StringUtils.hasText(snapshot.getTimestamp())
                || !StringUtils.hasText(snapshot.getNonce())) {
            return rejectOrIgnore("上下文签名头缺失");
        }
        if (signer.isExpired(snapshot, sign.getTtl(), clock)) {
            return rejectOrIgnore("上下文签名已过期");
        }
        if (replayGuard.seen(snapshot.getNonce(), sign.getTtl())) {
            return rejectOrIgnore("上下文签名 nonce 已重复");
        }
        if (!signer.verify(snapshot, sign.getSecret(), signature)) {
            return rejectOrIgnore("上下文签名校验失败");
        }
        return true;
    }

    private boolean rejectOrIgnore(String message) {
        if (properties.getHeaderSign().isRejectInvalid()) {
            throw new ContextAuthException(message);
        }
        return false;
    }

    private boolean hasAnyIdentityHeader(HttpServletRequest request) {
        return StringUtils.hasText(request.getHeader(ContextHeaderConstants.HEADER_USER_ID))
                || StringUtils.hasText(request.getHeader(ContextHeaderConstants.HEADER_USERNAME))
                || StringUtils.hasText(request.getHeader(ContextHeaderConstants.HEADER_TENANT_ID))
                || StringUtils.hasText(request.getHeader(ContextHeaderConstants.HEADER_ROLES))
                || StringUtils.hasText(request.getHeader(ContextHeaderConstants.HEADER_PERMISSIONS));
    }

    private ContextHeaderSnapshot snapshot(HttpServletRequest request, String traceId) {
        ContextHeaderSnapshot snapshot = new ContextHeaderSnapshot();
        snapshot.setMethod(request.getMethod());
        snapshot.setPath(request.getRequestURI());
        snapshot.setTimestamp(request.getHeader(properties.getHeaderSign().getTimestampHeader()));
        snapshot.setNonce(request.getHeader(properties.getHeaderSign().getNonceHeader()));
        snapshot.setUserId(request.getHeader(ContextHeaderConstants.HEADER_USER_ID));
        snapshot.setUsername(request.getHeader(ContextHeaderConstants.HEADER_USERNAME));
        snapshot.setTenantId(request.getHeader(ContextHeaderConstants.HEADER_TENANT_ID));
        snapshot.setDeptId(request.getHeader(ContextHeaderConstants.HEADER_DEPT_ID));
        snapshot.setRoles(request.getHeader(ContextHeaderConstants.HEADER_ROLES));
        snapshot.setPermissions(request.getHeader(ContextHeaderConstants.HEADER_PERMISSIONS));
        snapshot.setClientId(request.getHeader(ContextHeaderConstants.HEADER_CLIENT_ID));
        snapshot.setTraceId(StringUtils.hasText(traceId) ? traceId : request.getHeader(ContextHeaderConstants.HEADER_TRACE_ID));
        return snapshot;
    }

    private UserContext restoreUser(ContextHeaderSnapshot snapshot, HttpServletRequest request) {
        UserContext userContext = new UserContext();
        userContext.setUserId(parseLong(snapshot.getUserId()));
        userContext.setUsername(snapshot.getUsername());
        userContext.setNickname(request.getHeader(ContextHeaderConstants.HEADER_NICKNAME));
        userContext.setTenantId(parseLong(snapshot.getTenantId()));
        userContext.setDeptId(parseLong(snapshot.getDeptId()));
        userContext.setRoles(ContextValueUtils.parseCommaSeparated(snapshot.getRoles()));
        userContext.setPermissions(ContextValueUtils.parseCommaSeparated(snapshot.getPermissions()));
        userContext.setClientId(snapshot.getClientId());
        userContext.setSource(UserContextSource.GATEWAY_HEADER.name());
        if (userContext.getUserId() == null && !StringUtils.hasText(userContext.getUsername())) {
            return null;
        }
        return userContext;
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
