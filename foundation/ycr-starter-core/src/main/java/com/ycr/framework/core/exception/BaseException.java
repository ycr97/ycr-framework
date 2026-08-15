package com.ycr.framework.core.exception;

public abstract class BaseException extends RuntimeException {

    /** 默认 HTTP 状态码（业务异常按 400 处理） */
    private static final int DEFAULT_HTTP_STATUS = 400;

    private final String code;

    /** 期望的 HTTP 状态码，供全局异常处理器据此设置响应状态，保持 code 与 HTTP 状态一致 */
    private final int httpStatus;

    protected BaseException(String code, String message) {
        this(DEFAULT_HTTP_STATUS, code, message);
    }

    protected BaseException(String code, String message, Throwable cause) {
        this(DEFAULT_HTTP_STATUS, code, message, cause);
    }

    protected BaseException(int httpStatus, String code, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    protected BaseException(int httpStatus, String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
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

    public int getHttpStatus() {
        return httpStatus;
    }
}
