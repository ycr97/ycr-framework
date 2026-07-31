package com.ycr.framework.context.sign;

import com.ycr.framework.context.exception.ContextAuthException;

import java.time.Duration;

/**
 * 防重放基础设施不可用时拒绝签名身份请求。
 *
 * @author ycr
 */
public class FailClosedContextReplayGuard implements ContextReplayGuard {

    @Override
    public boolean seen(String nonce, Duration ttl) {
        throw new ContextAuthException("上下文 nonce 防重放组件不可用");
    }
}
