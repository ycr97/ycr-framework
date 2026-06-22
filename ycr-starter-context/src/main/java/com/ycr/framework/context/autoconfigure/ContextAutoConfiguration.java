package com.ycr.framework.context.autoconfigure;

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 上下文模块自动配置
 *
 * <p>Context Holder 本身为静态工具类无需注册 Bean；本配置提供与运行环境无关的上下文解析能力。
 * Servlet Filter 由 {@link ContextServletAutoConfiguration} 独立装配。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
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
}
