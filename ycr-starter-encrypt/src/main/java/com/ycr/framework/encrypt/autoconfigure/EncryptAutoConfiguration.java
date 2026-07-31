package com.ycr.framework.encrypt.autoconfigure;

import com.ycr.framework.encrypt.context.EncryptHandlerHolder;
import com.ycr.framework.encrypt.handler.AesEncryptHandler;
import com.ycr.framework.encrypt.handler.EncryptHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;

/**
 * 加解密自动配置
 *
 * @author ycr
 */
@AutoConfiguration
@EnableConfigurationProperties(EncryptProperties.class)
@ConditionalOnProperty(prefix = "ycr.encrypt", name = "enabled", havingValue = "true")
public class EncryptAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ycr.encrypt", name = "aes-key")
    public EncryptHandler encryptHandler(EncryptProperties properties) {
        return new AesEncryptHandler(properties.getAesKey());
    }

    @Bean
    @ConditionalOnBean(EncryptHandler.class)
    @ConditionalOnMissingBean
    public EncryptHandlerLifecycle encryptHandlerLifecycle(EncryptHandler encryptHandler) {
        return new EncryptHandlerLifecycle(encryptHandler);
    }

    public static class EncryptHandlerLifecycle implements InitializingBean, DisposableBean {

        private final EncryptHandler encryptHandler;

        public EncryptHandlerLifecycle(EncryptHandler encryptHandler) {
            this.encryptHandler = encryptHandler;
        }

        @Override
        public void afterPropertiesSet() {
            EncryptHandlerHolder.set(encryptHandler);
        }

        @Override
        public void destroy() {
            EncryptHandlerHolder.clear();
        }
    }
}
