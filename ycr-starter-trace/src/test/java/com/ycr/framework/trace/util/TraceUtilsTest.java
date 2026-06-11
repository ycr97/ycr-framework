package com.ycr.framework.trace.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TraceUtils 单元测试
 *
 * @author ycr
 */
class TraceUtilsTest {

    @AfterEach
    void tearDown() {
        TraceUtils.removeTraceId();
    }

    @Test
    void 生成TraceId非空且无横线() {
        String traceId = TraceUtils.generateTraceId();
        assertNotNull(traceId);
        assertFalse(traceId.isEmpty());
        assertFalse(traceId.contains("-"));
    }

    @Test
    void 设置后可从MDC读取() {
        String traceId = TraceUtils.generateTraceId();
        TraceUtils.setTraceId(traceId);

        assertEquals(traceId, TraceUtils.getTraceId());
        assertEquals(traceId, MDC.get(TraceUtils.TRACE_ID_KEY));
    }

    @Test
    void 移除后应为空() {
        TraceUtils.setTraceId("test-trace-id");
        TraceUtils.removeTraceId();

        assertNull(TraceUtils.getTraceId());
    }

    @Test
    void wrap应在子线程还原并清理traceId() throws InterruptedException {
        TraceUtils.setTraceId("abc");
        AtomicReference<String> seen = new AtomicReference<>();
        AtomicBoolean leaked = new AtomicBoolean(true);

        Runnable wrapped = TraceUtils.wrap(() -> {
            seen.set(TraceUtils.getTraceId());
        });

        Thread worker = new Thread(() -> {
            wrapped.run();
            // 包装执行结束后，子线程不应残留 traceId
            leaked.set(TraceUtils.getTraceId() != null);
        });
        worker.start();
        worker.join();

        assertEquals("abc", seen.get(), "子线程内应还原主线程 traceId");
        assertFalse(leaked.get(), "子线程执行后不应残留 traceId");
    }
}
