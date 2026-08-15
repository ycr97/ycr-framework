package com.ycr.framework.context.sign;

import java.time.Duration;

/**
 * 上下文签名 nonce 重放防护。
 *
 * @author ycr
 */
public interface ContextReplayGuard {

    /**
     * nonce 是否已经在有效期内出现过。
     */
    boolean seen(String nonce, Duration ttl);
}
