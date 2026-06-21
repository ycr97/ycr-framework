package com.ycr.framework.auth.resolver;

import com.ycr.framework.auth.util.LoginHelper;
import com.ycr.framework.context.enums.SecurityMode;
import com.ycr.framework.context.enums.UserContextSource;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.context.resolver.UserContextResolveRequest;
import com.ycr.framework.context.resolver.UserContextResolver;
import org.springframework.core.Ordered;

/**
 * Sa-Token 用户上下文解析器。
 *
 * @author ycr
 */
public class SaTokenUserContextResolver implements UserContextResolver {

    @Override
    public boolean supports(UserContextResolveRequest request) {
        return request.getSecurityMode() == SecurityMode.TOKEN_VERIFY
                || request.getSecurityMode() == SecurityMode.MIXED;
    }

    @Override
    public UserContext resolve(UserContextResolveRequest request) {
        UserContext userContext = LoginHelper.getUserContext();
        if (userContext != null && userContext.getSource() == null) {
            userContext.setSource(UserContextSource.TOKEN.name());
        }
        return userContext;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 150;
    }
}
