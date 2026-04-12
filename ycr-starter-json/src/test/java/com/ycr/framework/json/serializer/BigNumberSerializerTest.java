package com.ycr.framework.json.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
    void 超过JS安全整数的Long应序列化为字符串() throws JsonProcessingException {
        Long value = 9007199254740992L;
        String json = objectMapper.writeValueAsString(value);
        assertEquals("\"9007199254740992\"", json);
    }

    @Test
    void 安全范围内的Long应保持数字格式() throws JsonProcessingException {
        Long value = 12345L;
        String json = objectMapper.writeValueAsString(value);
        assertEquals("12345", json);
    }

    @Test
    void null值应序列化为null() throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(null);
        assertEquals("null", json);
    }
}
