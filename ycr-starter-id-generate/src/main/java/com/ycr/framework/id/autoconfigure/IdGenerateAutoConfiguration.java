package com.ycr.framework.id.autoconfigure;

import com.ycr.framework.id.generator.IdGenerator;
import com.ycr.framework.id.generator.SnowflakeIdGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    public IdGenerator idGenerator(IdGenerateProperties properties) {
        return new SnowflakeIdGenerator(properties.getWorkerId(), properties.getDatacenterId());
    }
}
