package com.ycr.framework.auth.autoconfigure;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Servlet 端点认证门禁自动配置。
 *
 * @author ycr
 */
@AutoConfiguration(after = SaTokenAuthAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({SaInterceptor.class, WebMvcConfigurer.class})
@ConditionalOnProperty(prefix = "ycr.auth.satoken", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SaTokenAuthProperties.class)
public class SaTokenWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "ycr.auth.satoken",
            name = "endpoint-policy",
            havingValue = "authenticated",
            matchIfMissing = true)
    public SaInterceptor saTokenLoginInterceptor() {
        return new SaInterceptor(handler -> StpUtil.checkLogin()).isAnnotation(false);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "ycr.auth.satoken",
            name = "endpoint-policy",
            havingValue = "authenticated",
            matchIfMissing = true)
    public WebMvcConfigurer saTokenWebMvcConfigurer(SaInterceptor interceptor,
                                                   SaTokenAuthProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor)
                        .addPathPatterns("/**")
                        .excludePathPatterns(properties.getPermitPaths());
            }
        };
    }
}
