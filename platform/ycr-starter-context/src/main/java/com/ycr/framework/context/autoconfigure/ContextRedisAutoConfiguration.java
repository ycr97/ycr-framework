package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.sign.ContextReplayGuard;
import com.ycr.framework.context.sign.RedissonContextReplayGuard;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 上下文 Redis 防重放自动配置。
 *
 * @author ycr
 */
@AutoConfiguration(
        before = ContextAutoConfiguration.class,
        afterName = {
                "org.redisson.spring.starter.RedissonAutoConfigurationV2",
                "org.redisson.spring.starter.RedissonAutoConfiguration"
        })
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnBean(RedissonClient.class)
@EnableConfigurationProperties(ContextProperties.class)
public class ContextRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ContextReplayGuard.class)
    public ContextReplayGuard contextReplayGuard(RedissonClient redissonClient, ContextProperties properties) {
        return new RedissonContextReplayGuard(
                redissonClient,
                properties.getHeaderSign().getReplayKeyPrefix());
    }
}
