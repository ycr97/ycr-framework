package com.ycr.framework.log.util;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    void 普通对象应序列化为JSON() {
        String json = support.serialize(Map.of("name", "张三"), 2000);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("张三"));
    }

    @Test
    void 敏感字段应按字段名脱敏() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", "张三");
        m.put("password", "secret123");
        String json = support.serialize(m, 2000);
        assertTrue(json.contains("******"), json);
        assertFalse(json.contains("secret123"), json);
    }

    @Test
    void 嵌套敏感字段也应脱敏() {
        String json = support.serialize(Map.of("inner", Map.of("pwd", "x9")), 2000);
        assertTrue(json.contains("******"), json);
        assertFalse(json.contains("x9"), json);
    }

    @Test
    void 超长应截断并加后缀() {
        String json = support.serialize(Map.of("k", "0123456789abcdef"), 10);
        assertTrue(json.endsWith("…(truncated)"), json);
        assertTrue(json.length() <= 10 + "…(truncated)".length(), json);
    }

    @Test
    void 噪声类型应跳过返回null() {
        assertTrue(support.isSkippable(new ByteArrayInputStream(new byte[0])));
        assertNull(support.serialize(new ByteArrayInputStream(new byte[0]), 2000));
    }

    @Test
    void 无ObjectMapper应降级返回null() {
        LogJsonSupport degraded = new LogJsonSupport(null, Set.of());
        assertNull(degraded.serialize(Map.of("k", "v"), 2000));
    }

    @Test
    void 序列化异常应兜底返回null() {
        // 空 Bean（无属性）默认 ObjectMapper 会抛 InvalidDefinitionException
        assertNull(support.serialize(new Object(), 2000));
    }
}
