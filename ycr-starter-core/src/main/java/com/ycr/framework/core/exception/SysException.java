package com.ycr.framework.core.exception;

public class SysException extends BaseException {

    public SysException(String code, String message) {
        super(code, message);
    }

    public SysException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public SysException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SysException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
