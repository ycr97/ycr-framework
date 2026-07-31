package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.filter.ContextFilter;
import com.ycr.framework.context.resolver.UserContextResolverChain;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Servlet 请求上下文过滤器自动配置。
 *
 * @author ycr
 */
@AutoConfiguration(after = ContextAutoConfiguration.class)
@ConditionalOnClass(Filter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ContextServletAutoConfiguration {

    /**
     * 注册上下文过滤器。
     *
     * <p>排序置于最外层并预留少量余量，确保清理动作覆盖整个请求处理链。</p>
     */
    @Bean
    @ConditionalOnMissingBean(name = "contextFilterRegistration")
    public FilterRegistrationBean<ContextFilter> contextFilterRegistration(ContextProperties properties,
                                                                           UserContextResolverChain resolverChain) {
        FilterRegistrationBean<ContextFilter> registration =
                new FilterRegistrationBean<>(new ContextFilter(properties, resolverChain));
        registration.addUrlPatterns("/*");
        registration.setName("ycrContextFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}
