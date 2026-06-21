package com.ycr.framework.context.resolver;

import com.ycr.framework.context.enums.SecurityMode;
import com.ycr.framework.context.model.UserContext;
import org.springframework.core.Ordered;

/**
 * 默认 token 用户上下文解析器。
 *
 * <p>框架本身不绑定具体认证中心。业务或 ycr-starter-auth 可注册更高优先级实现来解析 token。</p>
 *
 * @author ycr
 */
public class TokenUserContextResolver implements UserContextResolver {

    @Override
    public boolean supports(UserContextResolveRequest request) {
        return request.getSecurityMode() == SecurityMode.TOKEN_VERIFY
                || request.getSecurityMode() == SecurityMode.MIXED;
    }

    @Override
    public UserContext resolve(UserContextResolveRequest request) {
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 200;
    }
}
