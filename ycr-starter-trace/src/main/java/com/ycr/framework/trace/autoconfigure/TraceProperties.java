package com.ycr.framework.trace.autoconfigure;

import com.ycr.framework.trace.util.TraceUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

/**
 * 链路追踪配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.trace")
public class TraceProperties {

    /**
     * 是否启用链路追踪，默认启用
     */
    private boolean enabled = true;

    /**
     * TraceId 透传头名称，默认取 {@link TraceUtils#HEADER_TRACE_ID}（与 Feign 透传统一来源）
     */
    private String headerName = TraceUtils.HEADER_TRACE_ID;

    /**
     * RequestId 透传头名称
     */
    private String requestHeaderName = TraceUtils.HEADER_REQUEST_ID;

    /**
     * 过滤器排序，默认最外层（{@link Ordered#HIGHEST_PRECEDENCE}）。
     *
     * <p>置于 {@code ContextFilter}（{@code HIGHEST_PRECEDENCE+10}）之外，确保 TraceId 最先设置、
     * 最后清理，使 context 还原及全链路日志都携带 traceId。</p>
     */
    private int filterOrder = Ordered.HIGHEST_PRECEDENCE;

    private SlowRequest slowRequest = new SlowRequest();

    /**
     * 慢请求日志配置。
     */
    @Data
    public static class SlowRequest {

        /** 是否记录慢请求 */
        private boolean enabled = true;

        /** 慢请求阈值，单位毫秒 */
        private long thresholdMs = 1000L;

        /** 位于 ContextFilter 内层，确保日志记录时上下文仍存在 */
        private int filterOrder = Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
