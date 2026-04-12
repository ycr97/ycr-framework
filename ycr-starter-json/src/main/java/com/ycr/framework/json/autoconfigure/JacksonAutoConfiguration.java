package com.ycr.framework.json.autoconfigure;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.ycr.framework.core.enums.BaseEnum;
import com.ycr.framework.json.serializer.BaseEnumDeserializer;
import com.ycr.framework.json.serializer.BaseEnumSerializer;
import com.ycr.framework.json.serializer.BigNumberSerializer;
import com.ycr.framework.json.util.JsonUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.math.BigInteger;
import java.time.format.DateTimeFormatter;

@AutoConfiguration("ycrFrameworkJacksonAutoConfiguration")
@EnableConfigurationProperties(JacksonExtensionProperties.class)
public class JacksonAutoConfiguration {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer(
            JacksonExtensionProperties properties) {
        return builder -> {
            builder.serializers(
                    new LocalDateTimeSerializer(DATE_TIME_FORMATTER),
                    new LocalDateSerializer(DATE_FORMATTER),
                    new LocalTimeSerializer(TIME_FORMATTER)
            );
            builder.deserializers(
                    new LocalDateTimeDeserializer(DATE_TIME_FORMATTER),
                    new LocalDateDeserializer(DATE_FORMATTER),
                    new LocalTimeDeserializer(TIME_FORMATTER)
            );
            builder.postConfigurer(objectMapper -> {
                objectMapper.registerModule(createExtensionModule(properties));
                JsonUtils.setObjectMapper(objectMapper);
            });
        };
    }

    private Module createExtensionModule(JacksonExtensionProperties properties) {
        SimpleModule module = new SimpleModule();
        registerBaseEnumSupport(module);
        if (properties.isBigNumberToString()) {
            registerBigNumberSupport(module);
        }
        return module;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerBaseEnumSupport(SimpleModule module) {
        module.addSerializer(BaseEnum.class, (JsonSerializer) BaseEnumSerializer.INSTANCE);
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyEnumDeserializer(DeserializationConfig config,
                                                              JavaType type,
                                                              BeanDescription beanDesc,
                                                              JsonDeserializer<?> deserializer) {
                Class<?> rawClass = type.getRawClass();
                if (BaseEnum.class.isAssignableFrom(rawClass) && Enum.class.isAssignableFrom(rawClass)) {
                    return new BaseEnumDeserializer((Class<? extends BaseEnum<?>>) rawClass);
                }
                return deserializer;
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerBigNumberSupport(SimpleModule module) {
        module.addSerializer(Long.class, (JsonSerializer) BigNumberSerializer.INSTANCE);
        module.addSerializer(Long.TYPE, (JsonSerializer) BigNumberSerializer.INSTANCE);
        module.addSerializer(BigInteger.class, (JsonSerializer) BigNumberSerializer.INSTANCE);
    }
}
