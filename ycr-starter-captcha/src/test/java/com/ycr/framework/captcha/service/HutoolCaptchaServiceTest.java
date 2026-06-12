package com.ycr.framework.captcha.service;

import com.ycr.framework.cache.util.RedisUtils;
import com.ycr.framework.captcha.autoconfigure.CaptchaProperties;
import com.ycr.framework.captcha.model.CaptchaResult;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * HutoolCaptchaService 行为测试（mockStatic RedisUtils）
 *
 * @author ycr
 */
class HutoolCaptchaServiceTest {

    private final HutoolCaptchaService service = new HutoolCaptchaService(new CaptchaProperties());

    @Test
    void 生成应返回图片并将答案带TTL存入缓存() {
        try (MockedStatic<RedisUtils> rs = mockStatic(RedisUtils.class)) {
            CaptchaResult result = service.generate();

            assertNotNull(result.getId());
            assertTrue(result.getImageBase64().startsWith("data:image"), "应为 data URI");
            rs.verify(() -> RedisUtils.set(argThat(key -> key.contains(result.getId())),
                    anyString(), any(Duration.class)));
        }
    }

    @Test
    void 校验通过应忽略大小写并一次性删除() {
        try (MockedStatic<RedisUtils> rs = mockStatic(RedisUtils.class)) {
            rs.when(() -> RedisUtils.<String>get(anyString())).thenReturn("AB3D");

            assertTrue(service.verify("id-1", "ab3d"));

            rs.verify(() -> RedisUtils.delete(anyString()));
        }
    }

    @Test
    void 校验失败仍应一次性删除防暴力() {
        try (MockedStatic<RedisUtils> rs = mockStatic(RedisUtils.class)) {
            rs.when(() -> RedisUtils.<String>get(anyString())).thenReturn("AB3D");

            assertFalse(service.verify("id-1", "xxxx"));

            rs.verify(() -> RedisUtils.delete(anyString()));
        }
    }

    @Test
    void 答案不存在应失败() {
        try (MockedStatic<RedisUtils> rs = mockStatic(RedisUtils.class)) {
            rs.when(() -> RedisUtils.<String>get(anyString())).thenReturn(null);

            assertFalse(service.verify("id-1", "ab3d"));
        }
    }

    @Test
    void 入参为空应直接失败不查缓存() {
        try (MockedStatic<RedisUtils> rs = mockStatic(RedisUtils.class)) {
            assertFalse(service.verify(null, "ab3d"));
            assertFalse(service.verify("id-1", ""));

            rs.verify(() -> RedisUtils.get(anyString()), never());
        }
    }
}
