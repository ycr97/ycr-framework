package com.ycr.framework.json.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigNumberSerializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, BigNumberSerializer.INSTANCE);
        module.addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE);
        objectMapper.registerModule(module);
    }

    @Test
    @DisplayName("超过JS安全整数的Long应序列化为字符串")
    void shouldMatchExpectedBehavior001() throws JsonProcessingException {
        Long value = 9007199254740992L;
        String json = objectMapper.writeValueAsString(value);
        assertEquals("\"9007199254740992\"", json);
    }

    @Test
    @DisplayName("安全范围内的Long应保持数字格式")
    void shouldMatchExpectedBehavior002() throws JsonProcessingException {
        Long value = 12345L;
        String json = objectMapper.writeValueAsString(value);
        assertEquals("12345", json);
    }

    @Test
    @DisplayName("null值应序列化为null")
    void shouldMatchExpectedBehavior003() throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(null);
        assertEquals("null", json);
    }
}
