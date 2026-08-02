package com.ycr.framework.auth.oauth2.autoconfigure;

import com.ycr.framework.auth.oauth2.mapper.DefaultOAuth2UserContextMapper;
import com.ycr.framework.auth.oauth2.mapper.OAuth2UserContextMapper;
import com.ycr.framework.context.autoconfigure.ContextProperties;
import com.ycr.framework.security.aspect.AuthorizeAspect;
import com.ycr.framework.security.checker.PermissionChecker;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * OAuth2 Resource Server 核心自动配置。
 *
 * @author ycr
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "ycr.auth.oauth2.resource-server", name = "enabled", havingValue = "true")
@EnableConfigurationProperties({OAuth2ResourceServerProperties.class, ContextProperties.class})
public class OAuth2ResourceServerAutoConfiguration {

    @Bean
    InitializingBean oauth2ResourceServerConfigurationValidator(OAuth2ResourceServerProperties properties,
                                                                ContextProperties contextProperties,
                                                                Environment environment) {
        return () -> new OAuth2ResourceServerPropertiesValidator(properties, contextProperties, environment)
                .validate();
    }

    @Bean
    @ConditionalOnMissingBean(OAuth2UserContextMapper.class)
    public OAuth2UserContextMapper oauth2UserContextMapper(OAuth2ResourceServerProperties properties) {
        return new DefaultOAuth2UserContextMapper(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizeAspect authorizeAspect(PermissionChecker permissionChecker) {
        return new AuthorizeAspect(permissionChecker);
    }
}
