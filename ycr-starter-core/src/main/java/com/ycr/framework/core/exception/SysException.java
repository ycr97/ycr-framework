package com.ycr.framework.core.exception;

public class SysException extends BaseException {

    /** 系统异常固定按 HTTP 500 处理 */
    private static final int HTTP_STATUS = 500;

    public SysException(String code, String message) {
        super(HTTP_STATUS, code, message);
    }

    public SysException(String code, String message, Throwable cause) {
        super(HTTP_STATUS, code, message, cause);
    }

    public SysException(ErrorCode errorCode) {
        super(HTTP_STATUS, errorCode.getCode(), errorCode.getMessage());
    }

    public SysException(ErrorCode errorCode, Throwable cause) {
        super(HTTP_STATUS, errorCode.getCode(), errorCode.getMessage(), cause);
    }
}
