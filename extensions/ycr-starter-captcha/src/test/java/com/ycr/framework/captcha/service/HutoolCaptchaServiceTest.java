package com.ycr.framework.captcha.service;

import com.ycr.framework.captcha.autoconfigure.CaptchaProperties;
import com.ycr.framework.captcha.model.CaptchaResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * HutoolCaptchaService 行为测试。
 *
 * @author ycr
 */
class HutoolCaptchaServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("生成应返回图片并将答案带TTL存入缓存")
    void shouldMatchExpectedBehavior001() {
        RedissonClient client = mock(RedissonClient.class);
        RBucket<String> bucket = mock(RBucket.class);
        when(client.<String>getBucket(anyString())).thenReturn(bucket);
        HutoolCaptchaService service = service(client);

        CaptchaResult result = service.generate();

        assertNotNull(result.getId());
        assertTrue(result.getImageBase64().startsWith("data:image"), "应为 data URI");
        verify(client).getBucket(anyString());
        verify(bucket).set(anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("校验通过应忽略大小写并一次性删除")
    void shouldMatchExpectedBehavior002() {
        RBucket<String> bucket = bucketReturning("AB3D");
        HutoolCaptchaService service = serviceWithBucket(bucket);

        assertTrue(service.verify("id-1", "ab3d"));

        verify(bucket).getAndDelete();
    }

    @Test
    @DisplayName("校验失败仍应一次性删除防暴力")
    void shouldMatchExpectedBehavior003() {
        RBucket<String> bucket = bucketReturning("AB3D");
        HutoolCaptchaService service = serviceWithBucket(bucket);

        assertFalse(service.verify("id-1", "xxxx"));

        verify(bucket).getAndDelete();
    }

    @Test
    @DisplayName("答案不存在应失败")
    void shouldMatchExpectedBehavior004() {
        RBucket<String> bucket = bucketReturning(null);
        HutoolCaptchaService service = serviceWithBucket(bucket);

        assertFalse(service.verify("id-1", "ab3d"));
    }

    @Test
    @DisplayName("入参为空应直接失败不查缓存")
    void shouldMatchExpectedBehavior005() {
        RedissonClient client = mock(RedissonClient.class);
        HutoolCaptchaService service = service(client);

        assertFalse(service.verify(null, "ab3d"));
        assertFalse(service.verify("id-1", ""));

        verifyNoInteractions(client);
    }

    private HutoolCaptchaService service(RedissonClient client) {
        return new HutoolCaptchaService(new CaptchaProperties(), client);
    }

    @SuppressWarnings("unchecked")
    private RBucket<String> bucketReturning(String answer) {
        RBucket<String> bucket = mock(RBucket.class);
        when(bucket.getAndDelete()).thenReturn(answer);
        return bucket;
    }

    @SuppressWarnings("unchecked")
    private HutoolCaptchaService serviceWithBucket(RBucket<String> bucket) {
        RedissonClient client = mock(RedissonClient.class);
        when(client.<String>getBucket(anyString())).thenReturn(bucket);
        return service(client);
    }
}
