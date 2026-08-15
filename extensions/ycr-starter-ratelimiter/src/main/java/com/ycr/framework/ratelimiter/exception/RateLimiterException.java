package com.ycr.framework.ratelimiter.exception;

import com.ycr.framework.core.exception.BizException;

/**
 * 限流异常
 *
 * <p>继承 {@link BizException} 并固定业务码与 HTTP 状态 {@code 429}，被 {@code ycr-starter-web} 的全局异常
 * 处理器捕获后返回 {@code HTTP 429 + R.fail("429", message)}，code 与 HTTP 状态一致，便于网关/客户端退避识别。</p>
 *
 * @author ycr
 */
public class RateLimiterException extends BizException {

    /** 限流业务码（同 HTTP 429 Too Many Requests） */
    public static final String CODE = "429";

    /** 限流 HTTP 状态码 */
    public static final int HTTP_STATUS = 429;

    public RateLimiterException(String message) {
        super(HTTP_STATUS, CODE, message);
    }

    public RateLimiterException(String message, Throwable cause) {
        super(HTTP_STATUS, CODE, message, cause);
    }
}
