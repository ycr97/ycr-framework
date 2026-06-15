package com.ycr.framework.protect.xss.autoconfigure;

import com.ycr.framework.protect.xss.filter.XssFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * XSS 过滤自动配置
 *
 * <p>仅 Servlet Web 环境装配。注册 {@link XssFilter} 拦截 {@code /*}，对请求参数/请求头按
 * {@code ycr.protect.xss.mode} 清理。通过 {@code ycr.protect.xss.enabled=false} 关闭。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@EnableConfigurationProperties(XssProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "ycr.protect.xss", name = "enabled", havingValue = "true", matchIfMissing = true)
public class XssAutoConfiguration {

    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration(XssProperties properties) {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>(new XssFilter(properties));
        registration.addUrlPatterns("/*");
        registration.setName("ycrXssFilter");
        // 置于较外层，但晚于 Trace/Context 过滤器，保证清理在业务读取参数之前完成
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);
        return registration;
    }
}
