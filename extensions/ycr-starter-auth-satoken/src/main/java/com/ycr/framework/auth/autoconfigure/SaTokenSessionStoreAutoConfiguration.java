package com.ycr.framework.auth.autoconfigure;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * Sa-Token 会话存储自动配置。
 *
 * <p>存储类型完全由配置决定，不根据类路径自动切换。</p>
 *
 * @author ycr
 */
@AutoConfiguration(after = SaTokenAuthAutoConfiguration.class)
@ConditionalOnProperty(prefix = "ycr.auth.satoken", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SaTokenAuthProperties.class)
public class SaTokenSessionStoreAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "ycr.auth.satoken", name = "session-store", havingValue = "redis")
    public InitializingBean saTokenRedisAuthDomainValidator(SaTokenAuthProperties properties,
                                                            ObjectProvider<StpLogic> stpLogicProvider) {
        return () -> {
            if (!StringUtils.hasText(properties.getAuthDomain())) {
                throw new IllegalStateException(
                        "ycr.auth.satoken.auth-domain is required when session-store=redis");
            }
            StpLogic stpLogic = stpLogicProvider.getIfAvailable();
            if (stpLogic != null && !properties.getAuthDomain().trim().equals(stpLogic.getLoginType())) {
                throw new IllegalStateException(
                        "custom StpLogic loginType must match ycr.auth.satoken.auth-domain");
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(SaTokenDao.class)
    @ConditionalOnProperty(
            prefix = "ycr.auth.satoken",
            name = "session-store",
            havingValue = "memory",
            matchIfMissing = true)
    public SaTokenDao memorySaTokenDao() {
        return new SaTokenDaoDefaultImpl();
    }
}
