package com.ycr.framework.feign.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.feign.decoder.FeignErrorDecoder;
import com.ycr.framework.feign.interceptor.ContextPassInterceptor;
import com.ycr.framework.feign.interceptor.LocalePassInterceptor;
import com.ycr.framework.feign.interceptor.TokenPassInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Feign 增强自动配置
 *
 * <p>仅在类路径存在 Feign 时装配：上下文/Trace 透传拦截器 + 下游统一错误解码器，各有开关。</p>
 *
 * <p>语言/Token 透传拦截器依赖 Servlet API 读取当前请求，故额外按 {@code jakarta.servlet.http.HttpServletRequest}
 * 条件装配；非 Servlet 应用（如纯 WebFlux/独立 Feign 客户端）下不装配，避免运行时 {@code NoClassDefFoundError}。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@ConditionalOnClass(name = "feign.RequestInterceptor")
@EnableConfigurationProperties(FeignProperties.class)
public class FeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ycr.feign", name = "context-pass-enabled", havingValue = "true", matchIfMissing = true)
    public ContextPassInterceptor contextPassInterceptor() {
        return new ContextPassInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ycr.feign", name = "error-decoder-enabled", havingValue = "true", matchIfMissing = true)
    public FeignErrorDecoder feignErrorDecoder(ObjectMapper objectMapper) {
        return new FeignErrorDecoder(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = {
            "jakarta.servlet.http.HttpServletRequest",
            "org.springframework.web.context.request.ServletRequestAttributes"
    })
    @ConditionalOnProperty(prefix = "ycr.feign", name = "locale-pass-enabled", havingValue = "true", matchIfMissing = true)
    public LocalePassInterceptor localePassInterceptor(FeignProperties properties) {
        return new LocalePassInterceptor(properties.getLanguageHeader());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = {
            "jakarta.servlet.http.HttpServletRequest",
            "org.springframework.web.context.request.ServletRequestAttributes"
    })
    @ConditionalOnProperty(prefix = "ycr.feign", name = "token-pass-enabled", havingValue = "true")
    public TokenPassInterceptor tokenPassInterceptor() {
        return new TokenPassInterceptor();
    }
}
