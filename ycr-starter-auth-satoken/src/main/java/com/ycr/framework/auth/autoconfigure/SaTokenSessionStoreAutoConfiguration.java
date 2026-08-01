package com.ycr.framework.auth.autoconfigure;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Sa-Token 会话存储自动配置。
 *
 * <p>存储类型完全由配置决定，不根据类路径自动切换。</p>
 *
 * @author ycr
 */
@AutoConfiguration(after = SaTokenAuthAutoConfiguration.class)
@ConditionalOnProperty(prefix = "ycr.auth.satoken", name = "enabled", havingValue = "true")
public class SaTokenSessionStoreAutoConfiguration {

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
