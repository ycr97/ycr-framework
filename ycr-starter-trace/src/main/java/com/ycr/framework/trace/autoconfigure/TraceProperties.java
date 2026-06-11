package com.ycr.framework.trace.autoconfigure;

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
     * TraceId 透传头名称
     */
    private String headerName = "X-Trace-Id";

    /**
     * 过滤器排序，默认最外层（{@link Ordered#HIGHEST_PRECEDENCE}）。
     *
     * <p>置于 {@code ContextFilter}（{@code HIGHEST_PRECEDENCE+10}）之外，确保 TraceId 最先设置、
     * 最后清理，使 context 还原及全链路日志都携带 traceId。</p>
     */
    private int filterOrder = Ordered.HIGHEST_PRECEDENCE;
}
