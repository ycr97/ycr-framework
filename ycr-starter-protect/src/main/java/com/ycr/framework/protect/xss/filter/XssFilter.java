package com.ycr.framework.protect.xss.filter;

import com.ycr.framework.protect.xss.autoconfigure.XssProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * XSS 过滤器：对请求参数/请求头清理，防止跨站脚本注入。
 *
 * <p>放行 {@code excludePatterns}；若配置了 {@code includePatterns} 则仅过滤匹配路径，否则全部过滤。</p>
 *
 * @author ycr
 */
public class XssFilter extends OncePerRequestFilter {

    private final XssProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public XssFilter(XssProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (shouldClean(request)) {
            filterChain.doFilter(new XssRequestWrapper(request, properties.getMode()), response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean shouldClean(HttpServletRequest request) {
        String path = request.getServletPath();
        List<String> excludes = properties.getExcludePatterns();
        if (excludes != null && excludes.stream().anyMatch(p -> pathMatcher.match(p, path))) {
            return false;
        }
        List<String> includes = properties.getIncludePatterns();
        if (includes != null && !includes.isEmpty()) {
            return includes.stream().anyMatch(p -> pathMatcher.match(p, path));
        }
        return true;
    }
}
