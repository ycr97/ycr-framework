package com.ycr.framework.apidoc.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** 在文档总开关关闭时阻断 SpringDoc 与 Knife4j 的动态端点和静态资源。 */
public class ApiDocDisabledFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest
                && response instanceof HttpServletResponse httpResponse
                && isApiDocPath(httpRequest.getRequestURI().substring(httpRequest.getContextPath().length()))) {
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isApiDocPath(String path) {
        return path.equals("/doc.html")
                || path.equals("/swagger-ui.html")
                || path.equals("/v3/api-docs")
                || path.equals("/v3/api-docs.yaml")
                || path.startsWith("/v3/api-docs/")
                || path.startsWith("/swagger-ui/");
    }
}
