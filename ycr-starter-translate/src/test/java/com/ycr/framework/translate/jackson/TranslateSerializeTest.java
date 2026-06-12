package com.ycr.framework.translate.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ycr.framework.translate.annotation.Translate;
import com.ycr.framework.translate.enums.TranslateType;
import com.ycr.framework.translate.source.DictProvider;
import com.ycr.framework.translate.source.DictTranslateSource;
import com.ycr.framework.translate.source.EnumTranslateSource;
import com.ycr.framework.translate.source.StatusEnum;
import com.ycr.framework.translate.source.TranslateSourceRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 翻译序列化端到端测试（核心做透证据）：
 * 经挂载改造器的 ObjectMapper 序列化后，编码字段保留且新增同级文本字段。
 *
 * @author ycr
 */
class TranslateSerializeTest {

    static class Order {
        @Translate(type = TranslateType.DICT, key = "user_status")
        public String status = "1";

        // 指向真实枚举类，但 code=9 在枚举中不存在 -> 翻译为 null
        @Translate(type = TranslateType.ENUM, key = "com.ycr.framework.translate.source.StatusEnum", targetField = "stateText")
        public Integer state = 9;

        public String name = "甲";
    }

    private ObjectMapper mapperWithTranslate() {
        DictProvider dictProvider = (dictCode, itemCode) ->
                "user_status".equals(dictCode) && "1".equals(itemCode) ? "启用" : null;
        TranslateSourceRegistry registry = new TranslateSourceRegistry(
                List.of(new EnumTranslateSource(), new DictTranslateSource(dictProvider)));

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new TranslateBeanSerializerModifier(registry));
        mapper.registerModule(module);
        return mapper;
    }

    @Test
    void 序列化后编码保留且新增同级文本字段() throws Exception {
        // gender 字段类型为 StatusEnum？不——gender 是 Integer，ENUM 推断需要枚举类型字段
        Order order = new Order();
        JsonNode node = mapperWithTranslate().valueToTree(order);

        // 字典翻译：status 保留 + statusName=启用
        assertEquals("1", node.get("status").asText());
        assertEquals("启用", node.get("statusName").asText());

        // 非法枚举 code：stateText 写出 null
        assertTrue(node.has("stateText"));
        assertTrue(node.get("stateText").isNull());

        // 无注解字段不受影响、不产生多余字段
        assertEquals("甲", node.get("name").asText());
        assertFalse(node.has("nameName"));
    }

    @Test
    void 枚举字段类型推断翻译() throws Exception {
        // 字段声明类型即枚举类，ENUM 且 key 留空时按字段类型推断
        class Holder {
            @Translate(type = TranslateType.ENUM)
            public StatusEnum status = StatusEnum.ENABLED;
        }
        JsonNode node = mapperWithTranslate().valueToTree(new Holder());
        assertEquals("启用", node.get("statusName").asText());
    }
}
