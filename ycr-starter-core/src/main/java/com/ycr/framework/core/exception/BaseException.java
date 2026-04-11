package com.ycr.framework.core.exception;

public abstract class BaseException extends RuntimeException {

    private final String code;

    protected BaseException(String code, String message) {
        super(message);
        this.code = code;
    }

    protected BaseException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    protected BaseException(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    protected BaseException(ErrorCode errorCode, Throwable cause) {
        this(errorCode.getCode(), errorCode.getMessage(), cause);
    }

    public String getCode() {
        return code;
    }
}
