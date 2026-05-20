package com.ycr.framework.cache.util;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;

/**
 * Redis 操作工具类 - 基于 Redisson
 *
 * @author ycr
 */
public final class RedisUtils {

    private static final String KEY_SEPARATOR = ":";

    private static RedissonClient redissonClient;

    private RedisUtils() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    public static void setRedissonClient(RedissonClient redissonClient) {
        RedisUtils.redissonClient = redissonClient;
    }

    public static RedissonClient getClient() {
        if (redissonClient == null) {
            throw new IllegalStateException("RedissonClient 尚未初始化，请检查是否引入了 Redisson 依赖");
        }
        return redissonClient;
    }

    public static String buildKey(String... parts) {
        return String.join(KEY_SEPARATOR, parts);
    }

    public static <T> void set(String key, T value) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    public static <T> void set(String key, T value, Duration duration) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value, duration);
    }

    public static <T> T get(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    public static boolean delete(String key) {
        return redissonClient.getBucket(key).delete();
    }

    public static boolean exists(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    public static boolean expire(String key, Duration duration) {
        return redissonClient.getBucket(key).expire(duration);
    }
}
