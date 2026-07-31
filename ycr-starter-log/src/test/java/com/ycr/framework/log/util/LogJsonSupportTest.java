package com.ycr.framework.log.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LogJsonSupport 序列化/脱敏/截断/降级测试
 *
 * @author ycr
 */
class LogJsonSupportTest {

    private final LogJsonSupport support =
            new LogJsonSupport(new ObjectMapper(), Set.of("password", "pwd"));

    @Test
    @DisplayName("普通对象应序列化为JSON")
    void shouldMatchExpectedBehavior001() {
        String json = support.serialize(Map.of("name", "张三"), 2000);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("张三"));
    }

    @Test
    @DisplayName("敏感字段应按字段名脱敏")
    void shouldMatchExpectedBehavior002() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", "张三");
        m.put("password", "secret123");
        String json = support.serialize(m, 2000);
        assertTrue(json.contains("******"), json);
        assertFalse(json.contains("secret123"), json);
    }

    @Test
    @DisplayName("嵌套敏感字段也应脱敏")
    void shouldMatchExpectedBehavior003() {
        String json = support.serialize(Map.of("inner", Map.of("pwd", "x9")), 2000);
        assertTrue(json.contains("******"), json);
        assertFalse(json.contains("x9"), json);
    }

    @Test
    @DisplayName("超长应截断并加后缀")
    void shouldMatchExpectedBehavior004() {
        String json = support.serialize(Map.of("k", "0123456789abcdef"), 10);
        assertTrue(json.endsWith("…(truncated)"), json);
        assertTrue(json.length() <= 10 + "…(truncated)".length(), json);
    }

    @Test
    @DisplayName("噪声类型应跳过返回null")
    void shouldMatchExpectedBehavior005() {
        assertTrue(support.isSkippable(new ByteArrayInputStream(new byte[0])));
        assertNull(support.serialize(new ByteArrayInputStream(new byte[0]), 2000));
    }

    @Test
    @DisplayName("无ObjectMapper应降级返回null")
    void shouldMatchExpectedBehavior006() {
        LogJsonSupport degraded = new LogJsonSupport(null, Set.of());
        assertNull(degraded.serialize(Map.of("k", "v"), 2000));
    }

    @Test
    @DisplayName("序列化异常应兜底返回null")
    void shouldMatchExpectedBehavior007() {
        // 空 Bean（无属性）默认 ObjectMapper 会抛 InvalidDefinitionException
        assertNull(support.serialize(new Object(), 2000));
    }
}
