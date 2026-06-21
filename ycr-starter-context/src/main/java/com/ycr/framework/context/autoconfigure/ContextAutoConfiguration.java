package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.filter.ContextFilter;
import com.ycr.framework.context.resolver.ManualUserContextResolver;
import com.ycr.framework.context.resolver.SignedHeaderUserContextResolver;
import com.ycr.framework.context.resolver.SystemUserContextResolver;
import com.ycr.framework.context.resolver.TokenUserContextResolver;
import com.ycr.framework.context.resolver.UserContextResolver;
import com.ycr.framework.context.resolver.UserContextResolverChain;
import com.ycr.framework.context.sign.ContextHeaderSigner;
import com.ycr.framework.context.sign.ContextReplayGuard;
import com.ycr.framework.context.sign.NoopContextReplayGuard;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * 上下文模块自动配置
 *
 * <p>Context Holder 本身为静态工具类无需注册 Bean；本配置仅在 Servlet Web 环境下注册
 * {@link ContextFilter}，负责请求级的上下文还原与请求结束清理。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@EnableConfigurationProperties(ContextProperties.class)
public class ContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ContextHeaderSigner contextHeaderSigner() {
        return new ContextHeaderSigner();
    }

    @Bean
    @ConditionalOnMissingBean
    public ContextReplayGuard contextReplayGuard() {
        return new NoopContextReplayGuard();
    }

    @Bean
    @ConditionalOnMissingBean
    public SignedHeaderUserContextResolver signedHeaderUserContextResolver(ContextProperties properties,
                                                                           ContextHeaderSigner signer,
                                                                           ContextReplayGuard replayGuard) {
        return new SignedHeaderUserContextResolver(properties, signer, replayGuard);
    }

    @Bean
    @ConditionalOnMissingBean(TokenUserContextResolver.class)
    public TokenUserContextResolver tokenUserContextResolver() {
        return new TokenUserContextResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public ManualUserContextResolver manualUserContextResolver() {
        return new ManualUserContextResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public SystemUserContextResolver systemUserContextResolver() {
        return new SystemUserContextResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserContextResolverChain userContextResolverChain(List<UserContextResolver> resolvers) {
        return new UserContextResolverChain(resolvers);
    }

    /**
     * 注册上下文过滤器。
     *
     * <p>排序置于最外层（{@link Ordered#HIGHEST_PRECEDENCE} 之后留少量余量），确保 finally 中的
     * 清理动作覆盖整个请求处理链，避免线程池复用导致上下文残留串号。</p>
     */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
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
