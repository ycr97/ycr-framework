package com.ycr.framework.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.web.handler.GlobalExceptionHandler;
import com.ycr.framework.web.handler.UnifiedResponseBodyAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(WebResponseProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ycr.web.response", name = "enabled", havingValue = "true")
    public UnifiedResponseBodyAdvice unifiedResponseBodyAdvice(WebResponseProperties properties,
                                                               ObjectMapper objectMapper) {
        return new UnifiedResponseBodyAdvice(properties, objectMapper);
    }
}
