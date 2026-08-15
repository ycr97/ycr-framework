package com.ycr.framework.translate.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ycr.framework.translate.jackson.TranslateBeanSerializerModifier;
import com.ycr.framework.translate.source.DictProvider;
import com.ycr.framework.translate.source.DictTranslateSource;
import com.ycr.framework.translate.source.EnumTranslateSource;
import com.ycr.framework.translate.source.TranslateSource;
import com.ycr.framework.translate.source.TranslateSourceRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 字段翻译自动配置
 *
 * <p>装配内容：内置枚举源、（应用提供 {@link DictProvider} 时的）字典源、聚合所有源的注册表，
 * 以及把翻译改造器挂进 Jackson 的 {@link Jackson2ObjectMapperBuilderCustomizer}。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@ConditionalOnClass(ObjectMapper.class)
@EnableConfigurationProperties(TranslateProperties.class)
@ConditionalOnProperty(prefix = "ycr.translate", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TranslateAutoConfiguration {

    /**
     * 内置枚举翻译源（开箱即用）
     */
    @Bean
    @ConditionalOnMissingBean(EnumTranslateSource.class)
    public EnumTranslateSource enumTranslateSource() {
        return new EnumTranslateSource();
    }

    /**
     * 字典翻译源，仅当应用提供 {@link DictProvider} 时装配
     */
    @Bean
    @ConditionalOnBean(DictProvider.class)
    @ConditionalOnMissingBean(DictTranslateSource.class)
    public DictTranslateSource dictTranslateSource(DictProvider dictProvider) {
        return new DictTranslateSource(dictProvider);
    }

    /**
     * 聚合容器内所有翻译源（含应用自定义源）
     */
    @Bean
    @ConditionalOnMissingBean(TranslateSourceRegistry.class)
    public TranslateSourceRegistry translateSourceRegistry(ObjectProvider<TranslateSource> sources) {
        List<TranslateSource> list = sources.orderedStream().toList();
        return new TranslateSourceRegistry(list);
    }

    /**
     * 把翻译改造器注册进 ObjectMapper
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer translateJacksonCustomizer(TranslateSourceRegistry registry) {
        return builder -> builder.postConfigurer(objectMapper -> {
            SimpleModule module = new SimpleModule("ycrTranslateModule");
            module.setSerializerModifier(new TranslateBeanSerializerModifier(registry));
            objectMapper.registerModule(module);
        });
    }
}
