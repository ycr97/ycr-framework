package com.ycr.framework.context.sign;

import com.ycr.framework.context.exception.ContextAuthException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedissonContextReplayGuardTest {

    @Test
    @DisplayName("首次nonce放行后续并发请求均判定为重放")
    void shouldMatchExpectedBehavior001() throws Exception {
        RedissonClient client = mock(RedissonClient.class);
        @SuppressWarnings("unchecked")
        RBucket<String> bucket = mock(RBucket.class);
        AtomicBoolean stored = new AtomicBoolean();
        when(client.<String>getBucket("prefix:nonce-1")).thenReturn(bucket);
        when(bucket.setIfAbsent(eq("1"), any(Duration.class)))
                .thenAnswer(invocation -> stored.compareAndSet(false, true));
        RedissonContextReplayGuard guard = new RedissonContextReplayGuard(client, "prefix:");

        int concurrency = 32;
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        CountDownLatch ready = new CountDownLatch(concurrency);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < concurrency; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return guard.seen("nonce-1", Duration.ofSeconds(60));
                }));
            }
            ready.await();
            start.countDown();

            int replayed = 0;
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    replayed++;
                }
            }
            assertEquals(concurrency - 1, replayed);
        } finally {
            executor.shutdownNow();
        }
        verify(bucket, times(concurrency)).setIfAbsent("1", Duration.ofSeconds(60));
    }

    @Test
    @DisplayName("redis异常时应failClosed")
    void shouldMatchExpectedBehavior002() {
        RedissonClient client = mock(RedissonClient.class);
        when(client.getBucket("prefix:nonce-1")).thenThrow(new IllegalStateException("redis down"));
        RedissonContextReplayGuard guard = new RedissonContextReplayGuard(client, "prefix:");

        assertThrows(ContextAuthException.class,
                () -> guard.seen("nonce-1", Duration.ofSeconds(60)));
    }

    @Test
    @DisplayName("默认兼容实现不得绕过重放校验")
    void shouldMatchExpectedBehavior003() {
        NoopContextReplayGuard guard = new NoopContextReplayGuard();

        assertThrows(ContextAuthException.class,
                () -> guard.seen("nonce-1", Duration.ofSeconds(60)));
    }
}
