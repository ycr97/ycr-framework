package com.ycr.framework.context.holder;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.ycr.framework.context.model.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TenantContextHolderTest {

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void 设置和获取租户上下文() {
        TenantContext ctx = new TenantContext();
        ctx.setTenantId(3001L);
        ctx.setTenantCode("tenant-a");
        ctx.setTenantName("租户A");

        TenantContextHolder.set(ctx);

        TenantContext result = TenantContextHolder.get();
        assertEquals(3001L, result.getTenantId());
        assertEquals("tenant-a", result.getTenantCode());
        assertEquals("租户A", result.getTenantName());
    }

    @Test
    void 清除上下文后应返回null() {
        TenantContext ctx = new TenantContext();
        ctx.setTenantId(3001L);
        TenantContextHolder.set(ctx);
        TenantContextHolder.clear();

        assertNull(TenantContextHolder.get());
    }

    @Test
    void TTL支持线程池透传() throws Exception {
        ThreadPoolExecutor rawExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        rawExecutor.prestartAllCoreThreads();
        ExecutorService executor = TtlExecutors.getTtlExecutorService(rawExecutor);

        TenantContext ctx = new TenantContext();
        ctx.setTenantId(4002L);
        TenantContextHolder.set(ctx);

        try {
            Future<Long> future = executor.submit(() -> {
                TenantContext innerCtx = TenantContextHolder.get();
                return innerCtx != null ? innerCtx.getTenantId() : null;
            });
            assertEquals(4002L, future.get());
        } finally {
            executor.shutdown();
        }
    }
}
