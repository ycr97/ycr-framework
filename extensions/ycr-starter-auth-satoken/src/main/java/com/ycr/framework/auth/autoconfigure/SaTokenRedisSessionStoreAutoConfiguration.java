package com.ycr.framework.auth.autoconfigure;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoForRedisson;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基于应用现有 RedissonClient 的 Sa-Token 分布式会话存储。
 *
 * @author ycr
 */
@AutoConfiguration(after = SaTokenSessionStoreAutoConfiguration.class)
@ConditionalOnProperty(prefix = "ycr.auth.satoken", name = "enabled", havingValue = "true")
public class SaTokenRedisSessionStoreAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RedissonClient.class)
    @ConditionalOnProperty(prefix = "ycr.auth.satoken", name = "session-store", havingValue = "redis")
    static class RedisSessionStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(SaTokenDao.class)
        SaTokenDao redisSaTokenDao(RedissonClient redissonClient) {
            return new SaTokenDaoForRedisson(redissonClient);
        }
    }
}
