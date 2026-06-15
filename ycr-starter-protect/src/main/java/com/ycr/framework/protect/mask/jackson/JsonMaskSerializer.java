package com.ycr.framework.protect.mask.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.ycr.framework.core.util.SpringContextHolder;
import com.ycr.framework.protect.mask.annotation.JsonMask;
import com.ycr.framework.protect.mask.strategy.MaskStrategy;

import java.io.IOException;

/**
 * {@code @JsonMask} 脱敏序列化器
 *
 * <p>{@link ContextualSerializer}：按字段上的 {@link JsonMask} 解析脱敏类型/策略，序列化时对 String 值脱敏。
 * 自定义策略优先从 Spring 容器取 Bean，取不到则按无参构造反射实例化。</p>
 *
 * @author ycr
 */
public class JsonMaskSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private final JsonMask jsonMask;

    public JsonMaskSerializer() {
        this.jsonMask = null;
    }

    public JsonMaskSerializer(JsonMask jsonMask) {
        this.jsonMask = jsonMask;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (value.isEmpty() || jsonMask == null) {
            gen.writeString(value);
            return;
        }
        MaskStrategy strategy = resolveStrategy(jsonMask);
        gen.writeString(strategy.mask(value, jsonMask.character(), jsonMask.left(), jsonMask.right()));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
            throws JsonMappingException {
        if (property == null) {
            return provider.findNullValueSerializer(null);
        }
        if (property.getType().getRawClass() != String.class) {
            return provider.findValueSerializer(property.getType(), property);
        }
        JsonMask annotation = property.getAnnotation(JsonMask.class);
        if (annotation == null) {
            annotation = property.getContextAnnotation(JsonMask.class);
        }
        if (annotation == null) {
            return provider.findValueSerializer(property.getType(), property);
        }
        return new JsonMaskSerializer(annotation);
    }

    private MaskStrategy resolveStrategy(JsonMask mask) {
        Class<? extends MaskStrategy> strategyClass = mask.strategy();
        if (strategyClass == MaskStrategy.class) {
            return mask.value();
        }
        try {
            return SpringContextHolder.getBean(strategyClass);
        } catch (Exception ignored) {
            try {
                return strategyClass.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("无法实例化脱敏策略: " + strategyClass.getName(), e);
            }
        }
    }
}
