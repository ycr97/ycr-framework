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
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

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
    public EncryptHandler encryptHandler(EncryptProperties properties) {
        if (properties.getAlgorithm() != com.ycr.framework.encrypt.enums.EncryptAlgorithm.AES) {
            throw new IllegalStateException("ycr.encrypt.algorithm 当前仅支持 AES");
        }
        Map<String, String> keys = new LinkedHashMap<>(properties.getKeys());
        if (keys.isEmpty()) {
            if (!StringUtils.hasText(properties.getAesKey())) {
                throw new IllegalStateException(
                        "ycr.encrypt.enabled=true requires ycr.encrypt.aes-key, ycr.encrypt.keys, "
                                + "or a custom EncryptHandler bean");
            }
            keys.put(properties.getCurrentKeyId(), properties.getAesKey());
        } else if (StringUtils.hasText(properties.getAesKey())) {
            throw new IllegalStateException("ycr.encrypt.aes-key 与 ycr.encrypt.keys 不得同时配置");
        }
        return new AesEncryptHandler(
                properties.getCurrentKeyId(), keys, properties.getLegacyKeyId());
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
