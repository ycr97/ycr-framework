package com.ycr.framework.cache.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RedisUtilsTest {

    @Test
    void 工具类不可实例化() throws Exception {
        var constructor = RedisUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThrows(Exception.class, constructor::newInstance);
    }

    @Test
    void buildKey_应正确拼接前缀和键() {
        assertEquals("user:1001", RedisUtils.buildKey("user", "1001"));
    }

    @Test
    void buildKey_多段拼接() {
        assertEquals("app:user:profile:1001", RedisUtils.buildKey("app", "user", "profile", "1001"));
    }
}
