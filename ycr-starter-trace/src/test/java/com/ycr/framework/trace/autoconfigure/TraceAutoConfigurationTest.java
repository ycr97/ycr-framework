package com.ycr.framework.trace.autoconfigure;

import com.ycr.framework.trace.filter.TraceFilter;
import com.ycr.framework.trace.generator.TraceIdGenerator;
import com.ycr.framework.trace.generator.UuidTraceIdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TraceAutoConfiguration 装配与开关测试
 *
 * @author ycr
 */
class TraceAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TraceAutoConfiguration.class));

    @Test
    @SuppressWarnings("rawtypes")
    void 默认应装配过滤器与默认生成器() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(FilterRegistrationBean.class);
            assertThat(context).hasSingleBean(TraceIdGenerator.class);
            assertThat(context.getBean(TraceIdGenerator.class)).isInstanceOf(UuidTraceIdGenerator.class);
        });
    }

    @Test
    @SuppressWarnings("rawtypes")
    void 关闭开关时不装配过滤器() {
        runner.withPropertyValues("ycr.trace.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(FilterRegistrationBean.class));
    }

    @Test
    void 业务自定义生成器应覆盖默认() {
        runner.withUserConfiguration(CustomGeneratorConfig.class).run(context -> {
            assertThat(context).hasSingleBean(TraceIdGenerator.class);
            assertThat(context.getBean(TraceIdGenerator.class)).isInstanceOf(CustomGenerator.class);
        });
    }

    @Configuration
    static class CustomGeneratorConfig {
        @Bean
        TraceIdGenerator customGenerator() {
            return new CustomGenerator();
        }
    }

    static class CustomGenerator implements TraceIdGenerator {
        @Override
        public String generate() {
            return "custom";
        }
    }
}
