package com.ycr.framework.auth.oauth2.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.auth.oauth2.filter.OAuth2UserContextFilter;
import com.ycr.framework.auth.oauth2.handler.YcrBearerAccessDeniedHandler;
import com.ycr.framework.auth.oauth2.handler.YcrBearerAuthenticationEntryPoint;
import com.ycr.framework.auth.oauth2.mapper.OAuth2UserContextMapper;
import com.ycr.framework.context.autoconfigure.ContextProperties;
import com.ycr.framework.context.servlet.ServletContextBinder;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.cors.CorsUtils;

import java.util.List;

/**
 * OAuth2 Resource Server Servlet Web 自动配置。
 *
 * @author ycr
 */
@AutoConfiguration(after = OAuth2ResourceServerAutoConfiguration.class)
@ConditionalOnClass({Filter.class, SecurityFilterChain.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "ycr.auth.oauth2.resource-server", name = "enabled", havingValue = "true")
public class OAuth2ResourceServerWebAutoConfiguration {

    @Bean(name = "ycrBearerAuthenticationEntryPoint")
    @ConditionalOnMissingBean(name = "ycrBearerAuthenticationEntryPoint")
    public YcrBearerAuthenticationEntryPoint ycrBearerAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new YcrBearerAuthenticationEntryPoint(objectMapper);
    }

    @Bean(name = "ycrBearerAccessDeniedHandler")
    @ConditionalOnMissingBean(name = "ycrBearerAccessDeniedHandler")
    public YcrBearerAccessDeniedHandler ycrBearerAccessDeniedHandler(ObjectMapper objectMapper) {
        return new YcrBearerAccessDeniedHandler(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2UserContextFilter oauth2UserContextFilter(OAuth2UserContextMapper mapper,
                                                           ServletContextBinder contextBinder,
                                                           ContextProperties contextProperties,
                                                           @Qualifier("ycrBearerAuthenticationEntryPoint")
                                                           AuthenticationEntryPoint authenticationEntryPoint) {
        return new OAuth2UserContextFilter(mapper, contextBinder, contextProperties, authenticationEntryPoint);
    }

    @Bean(name = "ycrOAuth2ResourceServerSecurityFilterChain")
    @Order(100)
    @ConditionalOnMissingBean(name = "ycrOAuth2ResourceServerSecurityFilterChain")
    public SecurityFilterChain ycrOAuth2ResourceServerSecurityFilterChain(
            HttpSecurity http,
            OAuth2ResourceServerProperties properties,
            OAuth2UserContextFilter contextFilter,
            @Qualifier("ycrBearerAuthenticationEntryPoint") AuthenticationEntryPoint authenticationEntryPoint,
            @Qualifier("ycrBearerAccessDeniedHandler") AccessDeniedHandler accessDeniedHandler) throws Exception {
        if (properties.getMode() == null) {
            throw new IllegalStateException(
                    "ycr.auth.oauth2.resource-server.mode is required when "
                            + "ycr.auth.oauth2.resource-server.enabled=true");
        }

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers(CorsUtils::isPreFlightRequest).permitAll();
                    List<String> permitPaths = properties.getPermitPaths();
                    if (permitPaths != null && !permitPaths.isEmpty()) {
                        authorize.requestMatchers(permitPaths.toArray(new String[0])).permitAll();
                    }
                    if (properties.getEndpointPolicy()
                            == OAuth2ResourceServerProperties.EndpointPolicy.AUTHENTICATED) {
                        authorize.anyRequest().authenticated();
                    } else {
                        authorize.anyRequest().permitAll();
                    }
                })
                .oauth2ResourceServer(oauth2 -> {
                    if (properties.getMode() == OAuth2ResourceServerProperties.TokenMode.JWT) {
                        oauth2.jwt(Customizer.withDefaults());
                    } else {
                        oauth2.opaqueToken(Customizer.withDefaults());
                    }
                    oauth2.authenticationEntryPoint(authenticationEntryPoint)
                            .accessDeniedHandler(accessDeniedHandler)
                            .withObjectPostProcessor(new ObjectPostProcessor<BearerTokenAuthenticationFilter>() {
                                @Override
                                public <O extends BearerTokenAuthenticationFilter> O postProcess(O filter) {
                                    filter.setAuthenticationFailureHandler((request, response, failure) ->
                                            authenticationEntryPoint.commence(request, response, failure));
                                    return filter;
                                }
                            });
                })
                .addFilterAfter(contextFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }
}
