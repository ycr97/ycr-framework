package com.ycr.framework.captcha.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ycr.framework.captcha.autoconfigure.CaptchaProperties;
import com.ycr.framework.captcha.model.CaptchaResult;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;

/**
 * 基于 Hutool 的图形验证码服务
 *
 * <p>生成线段干扰验证码，答案以随机 id 为键存入 Redis 并设 TTL；
 * 校验时取出比对（忽略大小写），无论成败都删除该键（一次性，防单码暴力猜解）。</p>
 *
 * @author ycr
 */
public class HutoolCaptchaService implements CaptchaService {

    private final CaptchaProperties properties;
    private final RedissonClient redissonClient;

    public HutoolCaptchaService(CaptchaProperties properties, RedissonClient redissonClient) {
        this.properties = properties;
        this.redissonClient = redissonClient;
    }

    @Override
    public CaptchaResult generate() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(
                properties.getWidth(), properties.getHeight(),
                properties.getCodeCount(), properties.getLineCount());
        String id = IdUtil.fastSimpleUUID();
        redissonClient.<String>getBucket(buildKey(id))
                .set(captcha.getCode(), Duration.ofSeconds(properties.getExpirationSeconds()));
        return new CaptchaResult(id, captcha.getImageBase64Data());
    }

    @Override
    public boolean verify(String id, String code) {
        if (StrUtil.isBlank(id) || StrUtil.isBlank(code)) {
            return false;
        }
        String key = buildKey(id);
        RBucket<String> bucket = redissonClient.getBucket(key);
        String answer = bucket.getAndDelete();
        if (answer == null) {
            return false;
        }
        return answer.equalsIgnoreCase(code);
    }

    private String buildKey(String id) {
        return properties.getKeyPrefix() + ":" + id;
    }
}
