package com.ycr.framework.storage.autoconfigure;

import com.ycr.framework.storage.service.FileStorageService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;

/** 存储显式启用但后端未成功装配时提供明确的启动失败语义。 */
@AutoConfiguration(after = StorageAutoConfiguration.class)
@ConditionalOnProperty(prefix = "ycr.storage", name = "enabled", havingValue = "true")
public class StorageMissingBackendAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(FileStorageService.class)
    public Object missingStorageBackend(StorageProperties properties, ResourceLoader resourceLoader) {
        if ("s3".equalsIgnoreCase(properties.getType())
                && !ClassUtils.isPresent("software.amazon.awssdk.services.s3.S3Client", resourceLoader.getClassLoader())) {
            throw new IllegalStateException(
                    "ycr.storage.type=s3 requires software.amazon.awssdk:s3 on the runtime classpath");
        }
        throw new IllegalStateException("Unsupported ycr.storage.type: " + properties.getType());
    }
}
