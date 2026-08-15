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
 * <p>请求入口：优先取上游请求头中的 TraceId 和 RequestId，缺失则自动生成，写入 MDC 并回写响应头；
 * 请求出口（finally）清理 MDC，避免线程池复用导致标识残留。</p>
 *
 * @author ycr
 */
public class TraceFilter implements Filter {

    private final String traceHeaderName;
    private final String requestHeaderName;

    public TraceFilter(String traceHeaderName, String requestHeaderName) {
        this.traceHeaderName = traceHeaderName;
        this.requestHeaderName = requestHeaderName;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String traceId = request.getHeader(traceHeaderName);
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceUtils.generateTraceId();
        }
        String requestId = request.getHeader(requestHeaderName);
        if (requestId == null || requestId.isBlank()) {
            requestId = TraceUtils.generateTraceId();
        }
        TraceUtils.setTraceId(traceId);
        TraceUtils.setRequestId(requestId);
        response.setHeader(traceHeaderName, traceId);
        response.setHeader(requestHeaderName, requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            TraceUtils.removeRequestId();
            TraceUtils.removeTraceId();
        }
    }
}
