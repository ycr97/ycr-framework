package com.ycr.framework.idempotent.aop;

import com.ycr.framework.core.exception.BizException;
import com.ycr.framework.idempotent.annotation.Idempotent;
import com.ycr.framework.idempotent.autoconfigure.IdempotentProperties;
import com.ycr.framework.idempotent.exception.IdempotentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * IdempotentAspect 真实织入行为测试
 *
 * @author ycr
 */
class IdempotentAspectTest {

    @SuppressWarnings("unchecked")
    private DemoService weave(RedissonClient client) {
        AspectJProxyFactory factory = new AspectJProxyFactory(new DemoService());
        factory.addAspect(new IdempotentAspect(new IdempotentProperties(), client));
        return factory.getProxy();
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("首次提交应放行并占位")
    void shouldMatchExpectedBehavior001() {
        RedissonClient client = mock(RedissonClient.class);
        RBucket<Object> bucket = mock(RBucket.class);
        when(client.getBucket(anyString())).thenReturn((RBucket) bucket);
        when(bucket.trySet(any(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        DemoService proxy = weave(client);

        assertEquals("ok", proxy.submit());
        verify(bucket).trySet(any(), eq(1L), eq(TimeUnit.SECONDS));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("重复提交应抛幂等异常且为业务码409")
    void shouldMatchExpectedBehavior002() {
        RedissonClient client = mock(RedissonClient.class);
        RBucket<Object> bucket = mock(RBucket.class);
        when(client.getBucket(anyString())).thenReturn((RBucket) bucket);
        when(bucket.trySet(any(), anyLong(), any(TimeUnit.class))).thenReturn(false);
        DemoService proxy = weave(client);

        IdempotentException ex = assertThrows(IdempotentException.class, proxy::submit);
        assertEquals("409", ex.getCode());
        assertEquals(409, ex.getHttpStatus());
        assertInstanceOf(BizException.class, ex);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("业务异常应释放键允许重试")
    void shouldMatchExpectedBehavior003() {
        RedissonClient client = mock(RedissonClient.class);
        RBucket<Object> bucket = mock(RBucket.class);
        when(client.getBucket(anyString())).thenReturn((RBucket) bucket);
        when(bucket.trySet(any(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        DemoService proxy = weave(client);

        assertThrows(IllegalStateException.class, proxy::boom);
        ArgumentCaptor<Object> token = ArgumentCaptor.forClass(Object.class);
        verify(bucket).trySet(token.capture(), eq(1L), eq(TimeUnit.SECONDS));
        verify(bucket).compareAndSet(token.getValue(), null);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("SpEL键应拼入幂等键")
    void shouldMatchExpectedBehavior004() {
        RedissonClient client = mock(RedissonClient.class);
        RBucket<Object> bucket = mock(RBucket.class);
        when(client.getBucket(anyString())).thenReturn((RBucket) bucket);
        when(bucket.trySet(any(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        DemoService proxy = weave(client);

        proxy.submitOrder("o1");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).getBucket(keyCaptor.capture());
        assertTrue(keyCaptor.getValue().contains("o1"), "幂等键应包含 SpEL 求值结果");
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("未显式配置请求键时应拒绝执行")
    void shouldRejectAnnotationWithoutExplicitKey() {
        RedissonClient client = mock(RedissonClient.class);
        DemoService proxy = weave(client);

        IllegalStateException exception = assertThrows(IllegalStateException.class, proxy::unsafeSubmit);

        assertEquals("@Idempotent.key 必须显式配置，避免同一方法的不同请求共用全局键", exception.getMessage());
        verifyNoInteractions(client);
    }

    /** 测试目标 */
    public static class DemoService {

        @Idempotent(key = "'global-submit'")
        public String submit() {
            return "ok";
        }

        @Idempotent(key = "'global-boom'")
        public void boom() {
            throw new IllegalStateException("炸了");
        }

        @Idempotent(key = "#orderId")
        public String submitOrder(String orderId) {
            return "ok:" + orderId;
        }

        @Idempotent
        public String unsafeSubmit() {
            return "unsafe";
        }
    }
}
