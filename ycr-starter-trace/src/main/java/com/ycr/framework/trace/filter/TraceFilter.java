package com.ycr.framework.trace.filter;

import com.ycr.framework.trace.util.TraceUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 链路追踪过滤器
 *
 * <p>请求入口：优先取上游请求头中的 TraceId，缺失则自动生成，写入 MDC 并回写响应头供下游/前端串联；
 * 请求出口（finally）清理 MDC，避免线程池复用导致 traceId 残留。</p>
 *
 * @author ycr
 */
public class TraceFilter implements Filter {

    /** TraceId 透传头名称 */
    private final String headerName;

    public TraceFilter(String headerName) {
        this.headerName = headerName;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            // 优先复用上游传递的 TraceId，缺失则生成新的
            String traceId = request.getHeader(headerName);
            if (traceId == null || traceId.isBlank()) {
                traceId = TraceUtils.generateTraceId();
            }
            TraceUtils.setTraceId(traceId);
            response.setHeader(headerName, traceId);

            chain.doFilter(request, response);
        } finally {
            TraceUtils.removeTraceId();
        }
    }
}
