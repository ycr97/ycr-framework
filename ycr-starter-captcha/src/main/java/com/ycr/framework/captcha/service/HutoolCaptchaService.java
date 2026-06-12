package com.ycr.framework.captcha.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ycr.framework.cache.util.RedisUtils;
import com.ycr.framework.captcha.autoconfigure.CaptchaProperties;
import com.ycr.framework.captcha.model.CaptchaResult;

import java.time.Duration;

/**
 * 基于 Hutool 的图形验证码服务
 *
 * <p>生成线段干扰验证码，答案以随机 id 为键经 {@link RedisUtils} 存入缓存并设 TTL；
 * 校验时取出比对（忽略大小写），无论成败都删除该键（一次性，防单码暴力猜解）。</p>
 *
 * @author ycr
 */
public class HutoolCaptchaService implements CaptchaService {

    private final CaptchaProperties properties;

    public HutoolCaptchaService(CaptchaProperties properties) {
        this.properties = properties;
    }

    @Override
    public CaptchaResult generate() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(
                properties.getWidth(), properties.getHeight(),
                properties.getCodeCount(), properties.getLineCount());
        String id = IdUtil.fastSimpleUUID();
        RedisUtils.set(buildKey(id), captcha.getCode(), Duration.ofSeconds(properties.getExpirationSeconds()));
        return new CaptchaResult(id, captcha.getImageBase64Data());
    }

    @Override
    public boolean verify(String id, String code) {
        if (StrUtil.isBlank(id) || StrUtil.isBlank(code)) {
            return false;
        }
        String key = buildKey(id);
        String answer = RedisUtils.get(key);
        if (answer == null) {
            return false;
        }
        boolean passed = answer.equalsIgnoreCase(code);
        // 一次性：无论成败都失效，防止对同一验证码反复猜解
        RedisUtils.delete(key);
        return passed;
    }

    private String buildKey(String id) {
        return properties.getKeyPrefix() + ":" + id;
    }
}
