package com.ycr.framework.apidoc.autoconfigure;

import com.ycr.framework.apidoc.filter.ApiDocDisabledFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/** API 文档关闭态的 Servlet 端点阻断配置。 */
@AutoConfiguration(before = ApiDocAutoConfiguration.class)
@ConditionalOnClass(Filter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "ycr.api-doc", name = "enabled", havingValue = "false")
public class ApiDocDisabledAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "apiDocDisabledFilterRegistration")
    public FilterRegistrationBean<ApiDocDisabledFilter> apiDocDisabledFilterRegistration() {
        FilterRegistrationBean<ApiDocDisabledFilter> registration =
                new FilterRegistrationBean<>(new ApiDocDisabledFilter());
        registration.setName("ycrApiDocDisabledFilter");
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
