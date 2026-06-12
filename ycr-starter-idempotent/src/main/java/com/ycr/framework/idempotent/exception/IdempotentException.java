package com.ycr.framework.idempotent.exception;

import com.ycr.framework.core.exception.BizException;

/**
 * 幂等异常
 *
 * <p>继承 {@link BizException} 并固定业务码 {@code 409}（冲突/重复提交），从而被 {@code ycr-starter-web}
 * 的全局异常处理器捕获并返回统一 {@code R.fail("409", message)}，而非裸 500。</p>
 *
 * @author ycr
 */
public class IdempotentException extends BizException {

    /** 重复提交业务码 */
    public static final String CODE = "409";

    public IdempotentException(String message) {
        super(CODE, message);
    }

    public IdempotentException(String message, Throwable cause) {
        super(CODE, message, cause);
    }
}
