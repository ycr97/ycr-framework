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
}
