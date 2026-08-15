package com.ycr.framework.security.exception;

import com.ycr.framework.core.exception.BizException;

/**
 * 无访问权限。
 *
 * @author ycr
 */
public class ForbiddenException extends BizException {

    public ForbiddenException() {
        this("无访问权限");
    }

    public ForbiddenException(String message) {
        super(403, "AUTH_FORBIDDEN", message);
    }
}
