package com.ycr.framework.json.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.ycr.framework.core.enums.BaseEnum;

import java.io.IOException;
import java.util.Objects;

public class BaseEnumDeserializer extends JsonDeserializer<BaseEnum<?>> implements ContextualDeserializer {

    private final Class<? extends BaseEnum<?>> enumClass;

    public BaseEnumDeserializer() {
        this(null);
    }

    public BaseEnumDeserializer(Class<? extends BaseEnum<?>> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public BaseEnum<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == null) {
            p.nextToken();
        }
        if (p.currentToken() == null || p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        if (enumClass == null) {
            throw JsonMappingException.from(p, "BaseEnumDeserializer requires a target enum type");
        }

        ObjectCodec codec = p.getCodec();
        ObjectMapper objectMapper = codec instanceof ObjectMapper mapper ? mapper : new ObjectMapper();
        TreeNode treeNode = codec == null ? objectMapper.readTree(p) : codec.readTree(p);

        for (BaseEnum<?> enumConstant : enumClass.getEnumConstants()) {
            Object enumValue = enumConstant.getValue();
            Object candidate = convertValue(objectMapper, treeNode, enumValue);
            if (Objects.equals(enumValue, candidate)) {
                return enumConstant;
            }
        }

        throw JsonMappingException.from(p,
                "Cannot deserialize value " + treeNode + " to enum " + enumClass.getName());
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
            throws JsonMappingException {
        if (enumClass != null) {
            return this;
        }

        Class<?> rawClass = null;
        if (property != null && property.getType() != null) {
            rawClass = property.getType().getRawClass();
        } else if (ctxt.getContextualType() != null) {
            rawClass = ctxt.getContextualType().getRawClass();
        }

        if (rawClass != null && BaseEnum.class.isAssignableFrom(rawClass) && Enum.class.isAssignableFrom(rawClass)) {
            @SuppressWarnings("unchecked")
            Class<? extends BaseEnum<?>> targetClass = (Class<? extends BaseEnum<?>>) rawClass;
            return new BaseEnumDeserializer(targetClass);
        }

        return this;
    }

    private Object convertValue(ObjectMapper objectMapper, TreeNode treeNode, Object enumValue) {
        if (enumValue == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(treeNode, enumValue.getClass());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
