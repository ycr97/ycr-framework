package com.ycr.framework.storage.autoconfigure;

import com.ycr.framework.storage.service.FileStorageService;
import com.ycr.framework.storage.service.LocalFileStorageService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 文件存储自动配置
 *
 * <p>默认装配本地实现（{@code ycr.storage.type=local}）。业务方实现 {@link FileStorageService} 即可覆盖
 * 为 S3/MinIO 等。通过 {@code ycr.storage.enabled=false} 关闭。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@EnableConfigurationProperties(StorageProperties.class)
@ConditionalOnProperty(prefix = "ycr.storage", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StorageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ycr.storage", name = "type", havingValue = "local", matchIfMissing = true)
    public FileStorageService localFileStorageService(StorageProperties properties) {
        StorageProperties.Local local = properties.getLocal();
        return new LocalFileStorageService(local.getPath(), local.getUrlPrefix());
    }
}
