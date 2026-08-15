package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.filter.ContextFilter;
import com.ycr.framework.context.enums.SecurityMode;
import com.ycr.framework.context.resolver.SignedHeaderUserContextResolver;
import com.ycr.framework.context.resolver.UserContextResolver;
import com.ycr.framework.context.resolver.UserContextResolverChain;
import com.ycr.framework.context.servlet.ServletContextBinder;
import com.ycr.framework.context.sign.ContextHeaderSigner;
import com.ycr.framework.context.sign.ContextReplayGuard;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Servlet 请求上下文过滤器自动配置。
 *
 * @author ycr
 */
@AutoConfiguration(after = ContextAutoConfiguration.class)
@ConditionalOnClass(Filter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ContextServletAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SignedHeaderUserContextResolver signedHeaderUserContextResolver(ContextProperties properties,
                                                                           ContextHeaderSigner signer,
                                                                           ContextReplayGuard replayGuard,
                                                                           Environment environment) {
        String audience = properties.getHeaderSign().getAudience();
        if (!StringUtils.hasText(audience)) {
            audience = environment.getProperty("spring.application.name");
        }
        SecurityMode securityMode = properties.effectiveSecurityMode();
        if (securityMode == SecurityMode.GATEWAY_TRUST || securityMode == SecurityMode.MIXED) {
            if (!properties.getHeaderSign().isEnabled()
                    || !StringUtils.hasText(properties.getHeaderSign().getSecret())) {
                throw new IllegalStateException(
                        "gateway-trust/mixed 模式必须启用 ycr.context.header-sign 并配置 secret");
            }
            if (!StringUtils.hasText(audience)) {
                throw new IllegalStateException(
                        "gateway-trust/mixed 模式必须配置 ycr.context.header-sign.audience 或 spring.application.name");
            }
        }
        return new SignedHeaderUserContextResolver(properties, signer, replayGuard, audience);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserContextResolverChain userContextResolverChain(List<UserContextResolver> resolvers) {
        return new UserContextResolverChain(resolvers);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServletContextBinder servletContextBinder() {
        return new ServletContextBinder();
    }

    /**
     * 注册上下文过滤器。
     *
     * <p>排序置于最外层并预留少量余量，确保清理动作覆盖整个请求处理链。</p>
     */
    @Bean
    @ConditionalOnMissingBean(name = "contextFilterRegistration")
    public FilterRegistrationBean<ContextFilter> contextFilterRegistration(ContextProperties properties,
                                                                           UserContextResolverChain resolverChain,
                                                                           ServletContextBinder contextBinder) {
        FilterRegistrationBean<ContextFilter> registration =
                new FilterRegistrationBean<>(new ContextFilter(properties, resolverChain, contextBinder));
        registration.addUrlPatterns("/*");
        registration.setName("ycrContextFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}
