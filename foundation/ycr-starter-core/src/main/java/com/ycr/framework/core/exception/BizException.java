package com.ycr.framework.core.exception;

public class BizException extends BaseException {

    public BizException(String code, String message) {
        super(code, message);
    }

    public BizException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public BizException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * 供子类指定 HTTP 状态码（如限流 429、重复提交 409），使 code 与 HTTP 状态一致
     */
    protected BizException(int httpStatus, String code, String message) {
        super(httpStatus, code, message);
    }

    protected BizException(int httpStatus, String code, String message, Throwable cause) {
        super(httpStatus, code, message, cause);
    }
}
