package com.ycr.framework.auth.autoconfigure;

import cn.dev33.satoken.dao.SaTokenDao;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 会话模式缺少 Redisson 依赖时提供明确的启动失败语义。
 *
 * @author ycr
 */
@AutoConfiguration(after = SaTokenSessionStoreAutoConfiguration.class)
@ConditionalOnProperty(prefix = "ycr.auth.satoken", name = "enabled", havingValue = "true")
public class SaTokenMissingRedisAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingClass("org.redisson.api.RedissonClient")
    @ConditionalOnProperty(prefix = "ycr.auth.satoken", name = "session-store", havingValue = "redis")
    static class MissingRedisDependencyConfiguration {

        @Bean
        @ConditionalOnMissingBean(SaTokenDao.class)
        SaTokenDao missingRedisDependency() {
            throw new IllegalStateException(
                    "ycr.auth.satoken.session-store=redis requires RedissonClient; "
                            + "add ycr-starter-cache and configure Redis");
        }
    }
}
