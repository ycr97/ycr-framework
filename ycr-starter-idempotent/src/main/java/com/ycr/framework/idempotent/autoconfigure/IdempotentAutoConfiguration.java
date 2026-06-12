package com.ycr.framework.idempotent.autoconfigure;

import com.ycr.framework.idempotent.aop.IdempotentAspect;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 幂等自动配置
 *
 * <p>仅在类路径存在且容器内已有 {@link RedissonClient} 时装配切面（{@code @ConditionalOnBean}），
 * 无 Redis 环境不会破坏上下文。通过 {@code ycr.idempotent.enabled=false} 关闭。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@ConditionalOnClass(RedissonClient.class)
@EnableConfigurationProperties(IdempotentProperties.class)
@ConditionalOnProperty(prefix = "ycr.idempotent", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IdempotentAutoConfiguration {

    @Bean
    @ConditionalOnBean(RedissonClient.class)
    @ConditionalOnMissingBean
    public IdempotentAspect idempotentAspect(IdempotentProperties properties, RedissonClient redissonClient) {
        return new IdempotentAspect(properties, redissonClient);
    }
}
