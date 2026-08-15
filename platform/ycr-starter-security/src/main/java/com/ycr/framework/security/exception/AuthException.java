package com.ycr.framework.security.exception;

import com.ycr.framework.core.exception.BizException;

/**
 * 未登录或登录已过期。
 *
 * @author ycr
 */
public class AuthException extends BizException {

    public AuthException() {
        this("未登录或登录已过期");
    }

    public AuthException(String message) {
        super(401, "AUTH_UNAUTHORIZED", message);
    }
}
