package com.ycr.framework.auth.autoconfigure;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.ycr.framework.auth.handler.SaTokenExceptionHandler;
import com.ycr.framework.auth.resolver.SaTokenUserContextResolver;
import com.ycr.framework.auth.session.SaTokenSessionManager;
import com.ycr.framework.security.aspect.AuthorizeAspect;
import com.ycr.framework.security.checker.PermissionChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Sa-Token 认证适配器核心自动配置。
 *
 * @author ycr
 */
@AutoConfiguration
@ConditionalOnClass(StpUtil.class)
@ConditionalOnProperty(prefix = "ycr.auth.satoken", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SaTokenAuthProperties.class)
public class SaTokenAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StpLogic saTokenStpLogic(SaTokenAuthProperties properties) {
        String authDomain = StringUtils.hasText(properties.getAuthDomain())
                ? properties.getAuthDomain().trim()
                : StpUtil.TYPE;
        return new StpLogic(authDomain);
    }

    @Bean
    @ConditionalOnMissingBean
    public SaTokenSessionManager saTokenSessionManager() {
        return new SaTokenSessionManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public SaTokenExceptionHandler saTokenExceptionHandler() {
        return new SaTokenExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(SaTokenUserContextResolver.class)
    public SaTokenUserContextResolver saTokenUserContextResolver(SaTokenSessionManager sessionManager,
                                                                 SaTokenConfig saTokenConfig) {
        return new SaTokenUserContextResolver(sessionManager, saTokenConfig);
    }

    /**
     * Auth 组合 Starter 启用时同步启用 YCR 方法鉴权，无需重复配置 ycr.security.enabled。
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthorizeAspect authorizeAspect(PermissionChecker permissionChecker) {
        return new AuthorizeAspect(permissionChecker);
    }

    @Bean
    InitializingBean saTokenOAuth2MutualExclusionValidator(Environment environment) {
        return () -> {
            if (environment.getProperty("ycr.auth.oauth2.resource-server.enabled", Boolean.class, false)) {
                throw new IllegalStateException(
                        "ycr.auth.satoken.enabled and ycr.auth.oauth2.resource-server.enabled cannot both be true");
            }
        };
    }
}
