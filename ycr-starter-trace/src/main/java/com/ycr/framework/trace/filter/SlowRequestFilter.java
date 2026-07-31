package com.ycr.framework.trace.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 慢请求日志过滤器。
 *
 * @author ycr
 */
@Slf4j
public class SlowRequestFilter implements Filter {

    private final long thresholdMs;

    public SlowRequestFilter(long thresholdMs) {
        this.thresholdMs = thresholdMs;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            if (elapsedMs >= thresholdMs
                    && request instanceof HttpServletRequest httpRequest
                    && response instanceof HttpServletResponse httpResponse) {
                log.warn("event=slow_request traceId={} requestId={} userId={} tenantId={} clientId={} "
                                + "method={} uri={} status={} elapsedMs={}",
                        MDC.get("traceId"),
                        MDC.get("requestId"),
                        MDC.get("userId"),
                        MDC.get("tenantId"),
                        MDC.get("clientId"),
                        httpRequest.getMethod(),
                        httpRequest.getRequestURI(),
                        httpResponse.getStatus(),
                        elapsedMs);
            }
        }
    }
}
