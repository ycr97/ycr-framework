package com.ycr.framework.context.resolver;

import com.ycr.framework.context.model.UserContext;
import org.springframework.core.Ordered;

/**
 * 用户上下文解析器。
 *
 * @author ycr
 */
public interface UserContextResolver extends Ordered {

    /**
     * 当前解析器是否支持该请求。
     */
    boolean supports(UserContextResolveRequest request);

    /**
     * 解析用户上下文；无法解析时返回 null。
     */
    UserContext resolve(UserContextResolveRequest request);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
