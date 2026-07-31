package com.ycr.framework.translate.autoconfigure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.translate.annotation.Translate;
import com.ycr.framework.translate.enums.TranslateType;
import com.ycr.framework.translate.source.DictProvider;
import com.ycr.framework.translate.source.DictTranslateSource;
import com.ycr.framework.translate.source.EnumTranslateSource;
import com.ycr.framework.translate.source.TranslateSourceRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 翻译自动配置测试
 *
 * @author ycr
 */
class TranslateAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
                    JacksonAutoConfiguration.class, TranslateAutoConfiguration.class));

    @Test
    @DisplayName("默认装配注册表与内置枚举源_但无字典源")
    void shouldMatchExpectedBehavior001() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(TranslateSourceRegistry.class);
            assertThat(context).hasSingleBean(EnumTranslateSource.class);
            assertThat(context).hasBean("translateJacksonCustomizer");
            // 未提供 DictProvider，字典源不装配
            assertThat(context).doesNotHaveBean(DictTranslateSource.class);
        });
    }

    @Test
    @DisplayName("提供DictProvider时装配字典源_且ObjectMapper翻译生效")
    void shouldMatchExpectedBehavior002() {
        runner.withUserConfiguration(DictConfig.class).run(context -> {
            assertThat(context).hasSingleBean(DictTranslateSource.class);

            ObjectMapper mapper = context.getBean(ObjectMapper.class);
            JsonNode node = mapper.valueToTree(new Payload());
            assertThat(node.get("status").asText()).isEqualTo("1");
            assertThat(node.get("statusName").asText()).isEqualTo("启用");
        });
    }

    @Test
    @DisplayName("关闭开关时不装配")
    void shouldMatchExpectedBehavior003() {
        runner.withPropertyValues("ycr.translate.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(TranslateSourceRegistry.class);
            assertThat(context).doesNotHaveBean(EnumTranslateSource.class);
        });
    }

    @Configuration
    static class DictConfig {
        @Bean
        DictProvider dictProvider() {
            return (dictCode, itemCode) ->
                    "user_status".equals(dictCode) && "1".equals(itemCode) ? "启用" : null;
        }
    }

    static class Payload {
        @Translate(type = TranslateType.DICT, key = "user_status")
        public String status = "1";
    }
}
