package com.ycr.framework.web.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CorsAutoConfigurationTest {

    @Test
    void 默认关闭且可显式开启() {
        WebApplicationContextRunner runner = new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(CorsAutoConfiguration.class));
        runner.run(context -> assertThat(context).doesNotHaveBean(CorsAutoConfiguration.class));
        runner.withPropertyValues("ycr.web.cors.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(CorsAutoConfiguration.class));
    }
}
