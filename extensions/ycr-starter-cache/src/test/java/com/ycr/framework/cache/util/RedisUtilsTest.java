package com.ycr.framework.cache.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RedisUtilsTest {

    @Test
    @DisplayName("工具类不可实例化")
    void shouldMatchExpectedBehavior001() throws Exception {
        var constructor = RedisUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThrows(Exception.class, constructor::newInstance);
    }

    @Test
    @DisplayName("buildKey_应正确拼接前缀和键")
    void shouldMatchExpectedBehavior002() {
        assertEquals("user:1001", RedisUtils.buildKey("user", "1001"));
    }

    @Test
    @DisplayName("buildKey_多段拼接")
    void shouldMatchExpectedBehavior003() {
        assertEquals("app:user:profile:1001", RedisUtils.buildKey("app", "user", "profile", "1001"));
    }
}
