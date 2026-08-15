package com.ycr.framework.apidoc.autoconfigure;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API 文档自动配置测试。
 *
 * @author ycr
 */
class ApiDocAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ApiDocAutoConfiguration.class));

    @Test
    @DisplayName("默认开启且可关闭")
    void shouldMatchExpectedBehavior001() {
        runner.run(context -> assertThat(context).hasSingleBean(OpenAPI.class));
        runner.withPropertyValues("ycr.api-doc.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(OpenAPI.class));
    }
}
