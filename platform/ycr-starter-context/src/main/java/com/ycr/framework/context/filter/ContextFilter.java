package com.ycr.framework.context.filter;

import com.ycr.framework.context.autoconfigure.ContextProperties;
import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.context.resolver.UserContextResolveRequest;
import com.ycr.framework.context.resolver.UserContextResolverChain;
import com.ycr.framework.context.servlet.ServletContextBinder;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

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

    private final ServletContextBinder contextBinder;

    public ContextFilter(ContextProperties properties, UserContextResolverChain resolverChain) {
        this(properties, resolverChain, new ServletContextBinder());
    }

    public ContextFilter(ContextProperties properties,
                         UserContextResolverChain resolverChain,
                         ServletContextBinder contextBinder) {
        this.properties = properties;
        this.resolverChain = resolverChain;
        this.contextBinder = contextBinder;
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
            contextBinder.clear();
        }
    }

    /**
     * 从解析链还原上下文。
     */
    private void restore(HttpServletRequest request) {
        contextBinder.clear();
        UserContext userContext = resolverChain.resolve(new UserContextResolveRequest(
                request,
                properties.effectiveSecurityMode(),
                request.getHeader(ContextHeaderConstants.HEADER_TRACE_ID)));
        if (userContext == null) {
            return;
        }
        contextBinder.bind(userContext, request);
    }
}
