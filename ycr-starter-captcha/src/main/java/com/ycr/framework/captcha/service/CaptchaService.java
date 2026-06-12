package com.ycr.framework.captcha.service;

import com.ycr.framework.captcha.model.CaptchaResult;

/**
 * 验证码服务
 *
 * <p>业务方可实现此接口替换为算术/中文/滑块等验证码，框架通过 {@code @ConditionalOnMissingBean}
 * 让自定义实现覆盖默认的 Hutool 图形实现。</p>
 *
 * @author ycr
 */
public interface CaptchaService {

    /**
     * 生成验证码，答案带 TTL 存入缓存
     *
     * @return 验证码标识与图片 Base64
     */
    CaptchaResult generate();

    /**
     * 校验验证码。无论成败都会使该验证码失效（一次性，防暴力猜解）。
     *
     * @param id   生成时返回的标识
     * @param code 用户输入
     * @return 是否通过（忽略大小写）；超时/已用/入参为空均返回 false
     */
    boolean verify(String id, String code);
}
