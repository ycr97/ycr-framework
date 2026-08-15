package com.ycr.framework.id.autoconfigure;

import com.ycr.framework.id.generator.IdGenerator;
import com.ycr.framework.id.generator.SnowflakeIdGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ID 生成自动配置
 *
 * @author ycr
 */
@AutoConfiguration
@EnableConfigurationProperties(IdGenerateProperties.class)
public class IdGenerateAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ycr.id", name = "enabled", havingValue = "true")
    public IdGenerator idGenerator(IdGenerateProperties properties) {
        if (properties.getWorkerId() == null || properties.getDatacenterId() == null) {
            throw new IllegalStateException(
                    "ycr.id.worker-id 和 ycr.id.datacenter-id 必须在启用雪花 ID 时显式配置");
        }
        return new SnowflakeIdGenerator(properties.getWorkerId(), properties.getDatacenterId());
    }
}
