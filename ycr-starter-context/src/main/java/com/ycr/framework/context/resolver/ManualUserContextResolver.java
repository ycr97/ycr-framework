package com.ycr.framework.context.resolver;

import com.ycr.framework.context.model.UserContext;
import org.springframework.core.Ordered;

/**
 * 手工上下文解析器占位实现。
 *
 * @author ycr
 */
public class ManualUserContextResolver implements UserContextResolver {

    @Override
    public boolean supports(UserContextResolveRequest request) {
        return false;
    }

    @Override
    public UserContext resolve(UserContextResolveRequest request) {
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 300;
    }
}
