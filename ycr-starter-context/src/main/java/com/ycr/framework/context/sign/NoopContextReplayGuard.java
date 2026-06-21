package com.ycr.framework.context.sign;

import java.time.Duration;

/**
 * 默认空实现：不做 nonce 重放拦截。
 *
 * @author ycr
 */
public class NoopContextReplayGuard implements ContextReplayGuard {

    @Override
    public boolean seen(String nonce, Duration ttl) {
        return false;
    }
}
