package com.ycr.framework.ddd.autoconfigure;

import com.ycr.framework.ddd.event.DomainEventPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * DDD 领域核心自动配置
 *
 * <p>注册 {@link DomainEventPublisher}（包装容器的 {@link ApplicationEventPublisher}）。</p>
 *
 * @author ycr
 */
@AutoConfiguration
public class DddCoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DomainEventPublisher domainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new DomainEventPublisher(applicationEventPublisher);
    }
}
