package com.ycr.framework.feign.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.feign.decoder.FeignErrorDecoder;
import com.ycr.framework.feign.interceptor.ContextPassInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FeignAutoConfiguration 装配与开关测试
 *
 * @author ycr
 */
class FeignAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(ObjectMapperConfig.class)
            .withConfiguration(AutoConfigurations.of(FeignAutoConfiguration.class));

    @Test
    void 默认应装配透传拦截器与错误解码器() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(ContextPassInterceptor.class);
            assertThat(context).hasSingleBean(FeignErrorDecoder.class);
        });
    }

    @Test
    void 关闭透传开关时不装配拦截器() {
        runner.withPropertyValues("ycr.feign.context-pass-enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(ContextPassInterceptor.class));
    }

    @Test
    void 关闭解码开关时不装配解码器() {
        runner.withPropertyValues("ycr.feign.error-decoder-enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(FeignErrorDecoder.class));
    }

    @Configuration
    static class ObjectMapperConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
