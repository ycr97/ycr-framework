package com.ycr.framework.trace.util;

import com.ycr.framework.trace.generator.TraceIdGenerator;
import com.ycr.framework.trace.generator.UuidTraceIdGenerator;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 链路追踪工具类
 *
 * <p>TraceId 以 MDC 为规范存储，日志模板使用 {@code %X{traceId}} 即可原生输出。
 * MDC 是线程本地的，跨线程（线程池/异步）场景请用 {@link #wrap(Runnable)} / {@link #wrap(Callable)}
 * 把当前完整 MDC 捕获到子线程，执行后还原原上下文，避免线程池上下文残留。</p>
 *
 * @author ycr
 */
public final class TraceUtils {

    /** MDC 中的 TraceId 键，对应日志模板 {@code %X{traceId}} */
    public static final String TRACE_ID_KEY = "traceId";

    /** MDC 中的 RequestId 键 */
    public static final String REQUEST_ID_KEY = "requestId";

    /** 跨服务透传 TraceId 的 HTTP 头名称（Filter 回写、Feign 透传共用此默认值） */
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    /** 请求标识透传头名称 */
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    /** 默认生成器，可由自动配置注入业务自定义实现 */
    private static volatile TraceIdGenerator generator = new UuidTraceIdGenerator();

    private TraceUtils() {
    }

    /** 生成一个新的 TraceId */
    public static String generateTraceId() {
        return generator.generate();
    }

    /** 写入当前线程 MDC */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /** 读取当前线程 TraceId */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /** 从当前线程 MDC 移除 TraceId */
    public static void removeTraceId() {
        MDC.remove(TRACE_ID_KEY);
    }

    /** 写入当前线程 RequestId */
    public static void setRequestId(String requestId) {
        MDC.put(REQUEST_ID_KEY, requestId);
    }

    /** 读取当前线程 RequestId */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID_KEY);
    }

    /** 从当前线程 MDC 移除 RequestId */
    public static void removeRequestId() {
        MDC.remove(REQUEST_ID_KEY);
    }

    /** 替换 TraceId 生成器（由自动配置在装配时调用） */
    public static void setGenerator(TraceIdGenerator traceIdGenerator) {
        generator = traceIdGenerator;
    }

    /**
     * 包装 Runnable，使其在执行线程内还原提交时的完整 MDC，执行后恢复原上下文。
     *
     * <p>用于把主线程 traceId 带入线程池/异步任务（如 log 模块的异步落库执行器）。</p>
     */
    public static Runnable wrap(Runnable runnable) {
        Map<String, String> captured = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            restoreContext(captured);
            try {
                runnable.run();
            } finally {
                restoreContext(previous);
            }
        };
    }

    /**
     * 包装 Callable，语义同 {@link #wrap(Runnable)}
     */
    public static <T> Callable<T> wrap(Callable<T> callable) {
        Map<String, String> captured = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            restoreContext(captured);
            try {
                return callable.call();
            } finally {
                restoreContext(previous);
            }
        };
    }

    /** 还原线程原有 MDC：原本无则清理，避免线程池残留 */
    private static void restoreContext(Map<String, String> context) {
        MDC.clear();
        if (context != null && !context.isEmpty()) {
            MDC.setContextMap(context);
        }
    }
}
