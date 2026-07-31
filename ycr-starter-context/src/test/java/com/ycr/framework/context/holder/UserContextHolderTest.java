package com.ycr.framework.context.holder;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.ycr.framework.context.model.UserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserContextHolderTest {

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("设置和获取用户上下文")
    void shouldMatchExpectedBehavior001() {
        UserContext ctx = new UserContext();
        ctx.setUserId(1001L);
        ctx.setUsername("张三");
        ctx.setRoles(Set.of("admin", "user"));

        UserContextHolder.set(ctx);

        UserContext result = UserContextHolder.get();
        assertEquals(1001L, result.getUserId());
        assertEquals("张三", result.getUsername());
        assertEquals(Set.of("admin", "user"), result.getRoles());
    }

    @Test
    @DisplayName("清除上下文后应返回null")
    void shouldMatchExpectedBehavior002() {
        UserContext ctx = new UserContext();
        ctx.setUserId(1001L);
        UserContextHolder.set(ctx);
        UserContextHolder.clear();

        assertNull(UserContextHolder.get());
    }

    @Test
    @DisplayName("TTL支持线程池透传")
    void shouldMatchExpectedBehavior003() throws Exception {
        ThreadPoolExecutor rawExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        rawExecutor.prestartAllCoreThreads();
        ExecutorService executor = TtlExecutors.getTtlExecutorService(rawExecutor);

        UserContext ctx = new UserContext();
        ctx.setUserId(2002L);
        UserContextHolder.set(ctx);

        try {
            Future<Long> future = executor.submit(() -> {
                UserContext innerCtx = UserContextHolder.get();
                return innerCtx != null ? innerCtx.getUserId() : null;
            });
            assertEquals(2002L, future.get());
        } finally {
            executor.shutdown();
        }
    }
}
