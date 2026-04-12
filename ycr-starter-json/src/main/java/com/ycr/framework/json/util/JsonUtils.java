package com.ycr.framework.json.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.ycr.framework.core.enums.BaseEnum;
import com.ycr.framework.core.util.SpringContextHolder;
import com.ycr.framework.json.serializer.BaseEnumDeserializer;
import com.ycr.framework.json.serializer.BaseEnumSerializer;
import com.ycr.framework.json.serializer.BigNumberSerializer;
import org.springframework.beans.BeansException;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class JsonUtils {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = createDefaultObjectMapper();

    private static volatile ObjectMapper objectMapper = DEFAULT_OBJECT_MAPPER;

    private JsonUtils() {
    }

    public static void setObjectMapper(ObjectMapper objectMapper) {
        JsonUtils.objectMapper = objectMapper;
    }

    public static String toJson(Object value) {
        try {
            return getObjectMapper().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to serialize object to JSON", ex);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return getObjectMapper().readValue(json, clazz);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to deserialize JSON", ex);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return getObjectMapper().readValue(json, typeReference);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to deserialize JSON", ex);
        }
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper current = objectMapper;
        if (current != DEFAULT_OBJECT_MAPPER || SpringContextHolder.getContext() == null) {
            return current;
        }
        try {
            ObjectMapper springObjectMapper = SpringContextHolder.getBean(ObjectMapper.class);
            objectMapper = springObjectMapper;
            return springObjectMapper;
        } catch (BeansException ex) {
            return current;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        javaTimeModule.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        javaTimeModule.addDeserializer(LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));
        mapper.registerModule(javaTimeModule);

        SimpleModule extensionModule = new SimpleModule();
        extensionModule.addSerializer(Long.class, (com.fasterxml.jackson.databind.JsonSerializer) BigNumberSerializer.INSTANCE);
        extensionModule.addSerializer(Long.TYPE, (com.fasterxml.jackson.databind.JsonSerializer) BigNumberSerializer.INSTANCE);
        extensionModule.addSerializer(BigInteger.class, (com.fasterxml.jackson.databind.JsonSerializer) BigNumberSerializer.INSTANCE);
        extensionModule.addSerializer(BaseEnum.class, (com.fasterxml.jackson.databind.JsonSerializer) BaseEnumSerializer.INSTANCE);
        extensionModule.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyEnumDeserializer(DeserializationConfig config,
                                                              com.fasterxml.jackson.databind.JavaType type,
                                                              BeanDescription beanDesc,
                                                              JsonDeserializer<?> deserializer) {
                Class<?> rawClass = type.getRawClass();
                if (BaseEnum.class.isAssignableFrom(rawClass) && Enum.class.isAssignableFrom(rawClass)) {
                    return new BaseEnumDeserializer((Class<? extends BaseEnum<?>>) rawClass);
                }
                return deserializer;
            }
        });
        mapper.registerModule(extensionModule);

        return mapper;
    }
}
