package com.ycr.framework.cache.autoconfigure;

import com.ycr.framework.cache.util.RedisUtils;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 缓存模块自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnProperty(prefix = "ycr.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(RedissonClient.class)
public class CacheAutoConfiguration {

    public CacheAutoConfiguration(RedissonClient redissonClient) {
        RedisUtils.setRedissonClient(redissonClient);
    }
}
