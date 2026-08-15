package com.ycr.framework.auth.oauth2.autoconfigure;

import com.ycr.framework.auth.oauth2.introspection.ValidatingOpaqueTokenIntrospector;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;

/**
 * OAuth2 Resource Server Opaque Token 自动配置。
 *
 * @author ycr
 */
@AutoConfiguration(after = OAuth2ResourceServerAutoConfiguration.class)
@ConditionalOnProperty(prefix = "ycr.auth.oauth2.resource-server", name = "enabled", havingValue = "true")
public class OAuth2OpaqueAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "ycr.auth.oauth2.resource-server", name = "mode", havingValue = "opaque")
    @ConditionalOnMissingBean(OpaqueTokenIntrospector.class)
    public OpaqueTokenIntrospector oauth2OpaqueTokenIntrospector(OAuth2ResourceServerProperties properties,
                                                                 ObjectProvider<RestTemplateBuilder> builders) {
        OAuth2ResourceServerProperties.Opaque opaque = properties.getOpaque();
        RestTemplateBuilder restTemplateBuilder = builders.getIfAvailable(RestTemplateBuilder::new)
                .basicAuthentication(opaque.getClientId(), opaque.getClientSecret())
                .setConnectTimeout(opaque.getConnectTimeout())
                .setReadTimeout(opaque.getReadTimeout());
        SpringOpaqueTokenIntrospector delegate = new SpringOpaqueTokenIntrospector(
                opaque.getIntrospectionUri(), restTemplateBuilder.build());
        return new ValidatingOpaqueTokenIntrospector(delegate, opaque.getAudiences(), opaque.getIssuer());
    }
}
