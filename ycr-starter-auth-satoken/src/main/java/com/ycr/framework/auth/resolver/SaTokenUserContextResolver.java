package com.ycr.framework.auth.resolver;

import cn.dev33.satoken.config.SaTokenConfig;
import com.ycr.framework.auth.session.SaTokenSessionManager;
import com.ycr.framework.context.enums.SecurityMode;
import com.ycr.framework.context.enums.UserContextSource;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.context.resolver.UserContextResolveRequest;
import com.ycr.framework.context.resolver.UserContextResolver;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

/**
 * Sa-Token 用户上下文解析器。
 *
 * @author ycr
 */
public class SaTokenUserContextResolver implements UserContextResolver {

    private final SaTokenSessionManager sessionManager;

    private final SaTokenConfig saTokenConfig;

    public SaTokenUserContextResolver(SaTokenSessionManager sessionManager, SaTokenConfig saTokenConfig) {
        this.sessionManager = sessionManager;
        this.saTokenConfig = saTokenConfig;
    }

    @Override
    public boolean supports(UserContextResolveRequest request) {
        return request.getSecurityMode() == SecurityMode.TOKEN_VERIFY
                || request.getSecurityMode() == SecurityMode.MIXED;
    }

    @Override
    public UserContext resolve(UserContextResolveRequest request) {
        UserContext userContext = sessionManager.findUserContext(resolveToken(request));
        if (userContext != null) {
            userContext.setSource(UserContextSource.TOKEN.name());
        }
        return userContext;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 150;
    }

    private String resolveToken(UserContextResolveRequest request) {
        String rawValue = request.getRequest().getHeader(saTokenConfig.getTokenName());
        if (!StringUtils.hasText(rawValue)) {
            return null;
        }
        String tokenValue = rawValue.trim();
        String prefix = saTokenConfig.getTokenPrefix();
        if (!StringUtils.hasText(prefix)) {
            return tokenValue;
        }
        String expectedPrefix = prefix + " ";
        if (!tokenValue.regionMatches(true, 0, expectedPrefix, 0, expectedPrefix.length())) {
            return null;
        }
        String valueWithoutPrefix = tokenValue.substring(expectedPrefix.length()).trim();
        return StringUtils.hasText(valueWithoutPrefix) ? valueWithoutPrefix : null;
    }
}
