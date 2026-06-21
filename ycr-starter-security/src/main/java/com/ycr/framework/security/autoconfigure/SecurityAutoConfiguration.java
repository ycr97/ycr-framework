package com.ycr.framework.security.autoconfigure;

import com.ycr.framework.security.aspect.AuthorizeAspect;
import com.ycr.framework.security.checker.CompositePermissionChecker;
import com.ycr.framework.security.checker.ContextPermissionChecker;
import com.ycr.framework.security.checker.PermissionChecker;
import com.ycr.framework.security.checker.RemotePermissionChecker;
import com.ycr.framework.security.properties.SecurityProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 安全模块自动配置
 *
 * <p>注册 ycr 自有鉴权 AOP 与默认权限校验器。可通过 {@code ycr.security.enabled=false} 关闭切面。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {

    /**
     * 注册默认权限校验器。
     */
    @Bean
    @ConditionalOnMissingBean(PermissionChecker.class)
    public PermissionChecker permissionChecker(SecurityProperties properties,
                                               ObjectProvider<RemotePermissionChecker> remotePermissionChecker) {
        ContextPermissionChecker contextPermissionChecker = new ContextPermissionChecker();
        if (properties.getPermission().getMode() == SecurityProperties.PermissionMode.CONTEXT) {
            return contextPermissionChecker;
        }
        return new CompositePermissionChecker(
                contextPermissionChecker,
                remotePermissionChecker.getIfAvailable(),
                properties);
    }

    /**
     * 注册 ycr 鉴权切面。
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ycr.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AuthorizeAspect authorizeAspect(PermissionChecker permissionChecker) {
        return new AuthorizeAspect(permissionChecker);
    }
}
