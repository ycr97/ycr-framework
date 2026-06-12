package com.ycr.framework.ratelimiter.exception;

import com.ycr.framework.core.exception.BizException;

/**
 * 限流异常
 *
 * <p>继承 {@link BizException} 并固定业务码 {@code 429}，从而被 {@code ycr-starter-web} 的全局异常处理器
 * 捕获并返回统一 {@code R.fail("429", message)} 响应，而非裸 500。</p>
 *
 * @author ycr
 */
public class RateLimiterException extends BizException {

    /** 限流业务码 */
    public static final String CODE = "429";

    public RateLimiterException(String message) {
        super(CODE, message);
    }

    public RateLimiterException(String message, Throwable cause) {
        super(CODE, message, cause);
    }
}
