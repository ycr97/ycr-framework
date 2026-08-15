package com.ycr.framework.cache.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisLockUtilsTest {

    @Test
    @DisplayName("等待与租约时间应按毫秒传给Redisson")
    void shouldUseMillisecondsForWaitAndLeaseTime() throws Exception {
        RedissonClient client = mock(RedissonClient.class);
        RLock lock = mock(RLock.class);
        when(client.getLock("lock:test")).thenReturn(lock);
        when(lock.tryLock(3000, 10000, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        try (MockedStatic<RedisUtils> redisUtils = mockStatic(RedisUtils.class)) {
            redisUtils.when(RedisUtils::getClient).thenReturn(client);

            String result = RedisLockUtils.tryLock("lock:test", 3000, 10000, () -> "done");

            assertThat(result).isEqualTo("done");
            verify(lock).tryLock(3000, 10000, TimeUnit.MILLISECONDS);
            verify(lock).unlock();
        }
    }
}
