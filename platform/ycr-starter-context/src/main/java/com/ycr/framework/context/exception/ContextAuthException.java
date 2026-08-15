package com.ycr.framework.context.exception;

import com.ycr.framework.core.exception.BizException;

/**
 * 上下文认证异常，用于签名上下文头缺失、过期、错误或身份来源冲突。
 *
 * @author ycr
 */
public class ContextAuthException extends BizException {

    public ContextAuthException(String message) {
        super(401, "AUTH_UNAUTHORIZED", message);
    }
}
