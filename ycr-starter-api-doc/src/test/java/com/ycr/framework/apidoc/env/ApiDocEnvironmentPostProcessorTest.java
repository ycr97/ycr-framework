package com.ycr.framework.apidoc.env;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class ApiDocEnvironmentPostProcessorTest {

    private final ApiDocEnvironmentPostProcessor processor = new ApiDocEnvironmentPostProcessor();

    @Test
    @DisplayName("文档总开关应在Config Data加载后求值")
    void shouldRunAfterConfigDataEnvironmentPostProcessor() {
        assertThat(processor.getOrder()).isGreaterThan(ConfigDataEnvironmentPostProcessor.ORDER);
    }

    @Test
    @DisplayName("关闭YCR文档总开关时应优先关闭SpringDoc和Knife4j")
    void disabledMasterSwitchShouldOverrideDocumentationEngines() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("ycr.api-doc.enabled", "false")
                .withProperty("springdoc.api-docs.enabled", "true")
                .withProperty("springdoc.swagger-ui.enabled", "true")
                .withProperty("knife4j.enable", "true");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("springdoc.api-docs.enabled", Boolean.class)).isFalse();
        assertThat(environment.getProperty("springdoc.swagger-ui.enabled", Boolean.class)).isFalse();
        assertThat(environment.getProperty("knife4j.enable", Boolean.class)).isFalse();
        assertThat(environment.getPropertySources().iterator().next().getName())
                .isEqualTo(ApiDocEnvironmentPostProcessor.PROPERTY_SOURCE_NAME);
    }

    @Test
    @DisplayName("文档总开关开启时不应改写底层组件配置")
    void enabledMasterSwitchShouldPreserveEngineConfiguration() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("ycr.api-doc.enabled", "true")
                .withProperty("springdoc.api-docs.enabled", "true");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getPropertySources()
                .contains(ApiDocEnvironmentPostProcessor.PROPERTY_SOURCE_NAME)).isFalse();
        assertThat(environment.getProperty("springdoc.api-docs.enabled", Boolean.class)).isTrue();
    }
}
