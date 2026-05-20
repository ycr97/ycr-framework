package com.ycr.framework.encrypt.autoconfigure;

import com.ycr.framework.encrypt.handler.AesEncryptHandler;
import com.ycr.framework.encrypt.handler.EncryptHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 加解密自动配置
 *
 * @author ycr
 */
@AutoConfiguration
@EnableConfigurationProperties(EncryptProperties.class)
@ConditionalOnProperty(prefix = "ycr.encrypt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EncryptAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ycr.encrypt", name = "aes-key")
    public EncryptHandler encryptHandler(EncryptProperties properties) {
        return new AesEncryptHandler(properties.getAesKey());
    }
}
