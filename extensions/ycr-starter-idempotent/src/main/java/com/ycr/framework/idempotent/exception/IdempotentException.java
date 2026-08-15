package com.ycr.framework.idempotent.exception;

import com.ycr.framework.core.exception.BizException;

/**
 * 幂等异常
 *
 * <p>继承 {@link BizException} 并固定业务码与 HTTP 状态 {@code 409}（冲突/重复提交），被 {@code ycr-starter-web}
 * 的全局异常处理器捕获后返回 {@code HTTP 409 + R.fail("409", message)}，code 与 HTTP 状态一致。</p>
 *
 * @author ycr
 */
public class IdempotentException extends BizException {

    /** 重复提交业务码（同 HTTP 409 Conflict） */
    public static final String CODE = "409";

    /** 重复提交 HTTP 状态码 */
    public static final int HTTP_STATUS = 409;

    public IdempotentException(String message) {
        super(HTTP_STATUS, CODE, message);
    }

    public IdempotentException(String message, Throwable cause) {
        super(HTTP_STATUS, CODE, message, cause);
    }
}
