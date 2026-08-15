package com.ycr.framework.cache.util;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 分布式锁工具类
 *
 * @author ycr
 */
public final class RedisLockUtils {

    private RedisLockUtils() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    public static <T> T tryLock(String lockKey, long waitTime, long leaseTime, Supplier<T> supplier) {
        RLock lock = RedisUtils.getClient().getLock(lockKey);
        try {
            if (!lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS)) {
                return null;
            }
            try {
                return supplier.get();
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public static boolean tryLock(String lockKey, long waitTime, long leaseTime, Runnable runnable) {
        RLock lock = RedisUtils.getClient().getLock(lockKey);
        try {
            if (!lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS)) {
                return false;
            }
            try {
                runnable.run();
                return true;
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
