package com.ycr.framework.encrypt.autoconfigure;

import com.ycr.framework.encrypt.handler.EncryptHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/** 字段加密显式启用但没有可用处理器时提供明确的启动失败语义。 */
@AutoConfiguration(after = EncryptAutoConfiguration.class)
@ConditionalOnProperty(prefix = "ycr.encrypt", name = "enabled", havingValue = "true")
public class EncryptMissingHandlerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EncryptHandler.class)
    public Object missingEncryptHandler() {
        throw new IllegalStateException(
                "ycr.encrypt.enabled=true requires ycr.encrypt.aes-key or a custom EncryptHandler bean");
    }
}
