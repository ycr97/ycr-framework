package com.ycr.framework.security.autoconfigure;

import cn.dev33.satoken.interceptor.SaInterceptor;
import com.ycr.framework.security.properties.SecurityProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 安全模块自动配置
 *
 * <p>在 Servlet Web 环境下注册 SaToken 注解鉴权拦截器（{@link SaInterceptor} 开启注解模式），
 * 拦截 {@code /**} 并放行 {@link SecurityProperties#getExcludePaths()} 中配置的路径。
 * 鉴权方式以方法/类上的 {@code @SaCheckLogin}、{@code @SaCheckRole}、{@code @SaCheckPermission}
 * 等注解为准，框架不做全局强制登录，保持按需鉴权的灵活性。</p>
 *
 * <p>可通过 {@code ycr.security.enabled=false} 关闭拦截器注册。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityAutoConfiguration {

    /**
     * 注册 SaToken 注解鉴权拦截器。
     */
    @Bean
    @ConditionalOnProperty(prefix = "ycr.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public WebMvcConfigurer ycrSecurityWebMvcConfigurer(SecurityProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new SaInterceptor().isAnnotation(true))
                        .addPathPatterns("/**")
                        .excludePathPatterns(properties.getExcludePaths().toArray(new String[0]));
            }
        };
    }
}
