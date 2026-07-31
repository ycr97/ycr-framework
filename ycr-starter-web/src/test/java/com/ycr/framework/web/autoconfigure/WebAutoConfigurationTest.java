package com.ycr.framework.web.autoconfigure;

import com.ycr.framework.web.handler.GlobalExceptionHandler;
import com.ycr.framework.web.handler.UnifiedResponseBodyAdvice;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class WebAutoConfigurationTest {

    private final WebApplicationContextRunner webRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class, WebAutoConfiguration.class));

    @Test
    void Web环境默认只应注册异常处理器() {
        webRunner.run(context -> {
            assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
            assertThat(context).doesNotHaveBean(UnifiedResponseBodyAdvice.class);
            assertThat(context).hasSingleBean(WebResponseProperties.class);
        });
    }

    @Test
    void 显式开启响应包装时应注册响应包装器() {
        webRunner.withPropertyValues("ycr.web.response.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
                    assertThat(context).hasSingleBean(UnifiedResponseBodyAdvice.class);
                });
    }

    @Test
    void 应绑定包含和排除路径配置() {
        webRunner.withPropertyValues(
                        "ycr.web.response.include-paths[0]=/api/**",
                        "ycr.web.response.exclude-paths[0]=/api/raw/**")
                .run(context -> {
                    WebResponseProperties properties = context.getBean(WebResponseProperties.class);
                    assertThat(properties.getIncludePaths()).containsExactly("/api/**");
                    assertThat(properties.getExcludePaths()).containsExactly("/api/raw/**");
                });
    }
}
