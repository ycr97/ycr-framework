package com.ycr.framework.protect.mask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.protect.mask.annotation.JsonMask;
import com.ycr.framework.protect.mask.enums.MaskType;
import com.ycr.framework.protect.mask.strategy.MaskStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code @JsonMask} 序列化脱敏测试：经真实 {@link ObjectMapper} 验证各类型/自定义策略的输出。
 *
 * @author ycr
 */
class JsonMaskTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class FixedStrategy implements MaskStrategy {
        @Override
        public String mask(String value, char character, int left, int right) {
            return "FIXED";
        }
    }

    public static class Demo {
        @JsonMask(MaskType.MOBILE_PHONE)
        public String phone = "13812345678";
        @JsonMask(MaskType.EMAIL)
        public String email = "david@126.com";
        @JsonMask(MaskType.CHINESE_NAME)
        public String name = "张三丰";
        @JsonMask(value = MaskType.CUSTOM, left = 1, right = 1)
        public String custom = "abcdef";
        @JsonMask(MaskType.PASSWORD)
        public String password = "secret";
        @JsonMask(strategy = FixedStrategy.class)
        public String byStrategy = "anything";
        public String plain = "visible";
    }

    @Test
    void 各脱敏类型按规则输出_未注解字段保持原样() throws Exception {
        JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(new Demo()));
        assertEquals("138****5678", node.get("phone").asText());
        assertEquals("d****@126.com", node.get("email").asText());
        assertEquals("张**", node.get("name").asText());
        assertEquals("a****f", node.get("custom").asText());
        assertEquals("******", node.get("password").asText());
        assertEquals("FIXED", node.get("byStrategy").asText());
        assertEquals("visible", node.get("plain").asText());
    }

    @Test
    void 空值与null安全() throws Exception {
        Demo demo = new Demo();
        demo.phone = "";
        demo.email = null;
        JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(demo));
        assertEquals("", node.get("phone").asText());
        assertTrue(node.get("email").isNull());
    }
}
