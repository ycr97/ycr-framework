package com.ycr.framework.context.sign;

import com.ycr.framework.context.exception.ContextAuthException;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * 基于 Redis 原子 SET NX 的上下文 nonce 防重放实现。
 *
 * @author ycr
 */
public class RedissonContextReplayGuard implements ContextReplayGuard {

    private static final String PRESENT = "1";

    private final RedissonClient redissonClient;

    private final String keyPrefix;

    public RedissonContextReplayGuard(RedissonClient redissonClient, String keyPrefix) {
        if (!StringUtils.hasText(keyPrefix)) {
            throw new IllegalArgumentException("上下文 nonce Redis 键前缀不能为空");
        }
        this.redissonClient = redissonClient;
        this.keyPrefix = keyPrefix;
    }

    @Override
    public boolean seen(String nonce, Duration ttl) {
        if (!StringUtils.hasText(nonce) || ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new ContextAuthException("上下文 nonce 或有效期无效");
        }
        try {
            RBucket<String> bucket = redissonClient.getBucket(keyPrefix + nonce);
            return !bucket.setIfAbsent(PRESENT, ttl);
        } catch (RuntimeException e) {
            throw new ContextAuthException("上下文 nonce 防重放校验失败");
        }
    }
}
