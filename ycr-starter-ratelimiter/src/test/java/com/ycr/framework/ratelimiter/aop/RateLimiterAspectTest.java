package com.ycr.framework.ratelimiter.aop;

import com.ycr.framework.core.exception.BizException;
import com.ycr.framework.ratelimiter.annotation.RateLimiter;
import com.ycr.framework.ratelimiter.autoconfigure.RateLimiterProperties;
import com.ycr.framework.ratelimiter.exception.RateLimiterException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RateLimiterAspect 真实织入行为测试
 *
 * @author ycr
 */
class RateLimiterAspectTest {

    private DemoService weave(RedissonClient client) {
        AspectJProxyFactory factory = new AspectJProxyFactory(new DemoService());
        factory.addAspect(new RateLimiterAspect(new RateLimiterProperties(), client));
        return factory.getProxy();
    }

    @Test
    void 超过速率应抛限流异常且为业务码429() {
        RedissonClient client = mock(RedissonClient.class);
        RRateLimiter limiter = mock(RRateLimiter.class);
        when(client.getRateLimiter(anyString())).thenReturn(limiter);
        when(limiter.trySetRate(any(RateType.class), anyLong(), any(Duration.class))).thenReturn(true);
        when(limiter.tryAcquire()).thenReturn(true, true, false);
        DemoService proxy = weave(client);

        assertEquals("ok", proxy.hit());
        assertEquals("ok", proxy.hit());
        RateLimiterException ex = assertThrows(RateLimiterException.class, proxy::hit);
        assertEquals("429", ex.getCode());
        assertInstanceOf(BizException.class, ex);
    }

    @Test
    void SpEL键应拼入限流键() {
        RedissonClient client = mock(RedissonClient.class);
        RRateLimiter limiter = mock(RRateLimiter.class);
        when(client.getRateLimiter(anyString())).thenReturn(limiter);
        when(limiter.trySetRate(any(RateType.class), anyLong(), any(Duration.class))).thenReturn(true);
        when(limiter.tryAcquire()).thenReturn(true);
        DemoService proxy = weave(client);

        proxy.hitUser("u1");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(client, atLeastOnce()).getRateLimiter(keyCaptor.capture());
        assertTrue(keyCaptor.getValue().contains("u1"), "限流键应包含 SpEL 求值结果");
    }

    @Test
    void 首次应按OVERALL设置速率() {
        RedissonClient client = mock(RedissonClient.class);
        RRateLimiter limiter = mock(RRateLimiter.class);
        when(client.getRateLimiter(anyString())).thenReturn(limiter);
        when(limiter.trySetRate(any(RateType.class), anyLong(), any(Duration.class))).thenReturn(true);
        when(limiter.tryAcquire()).thenReturn(true);
        DemoService proxy = weave(client);

        proxy.hit();

        verify(limiter).trySetRate(eq(RateType.OVERALL), eq(2L), any(Duration.class));
    }

    /** 测试目标 */
    public static class DemoService {

        @RateLimiter(rate = 2, interval = 1)
        public String hit() {
            return "ok";
        }

        @RateLimiter(key = "#userId", rate = 5, interval = 1)
        public String hitUser(String userId) {
            return "ok:" + userId;
        }
    }
}
