package com.ycr.framework.storage.autoconfigure;

import com.ycr.framework.storage.service.FileStorageService;
import com.ycr.framework.storage.service.LocalFileStorageService;
import com.ycr.framework.storage.service.S3FileStorageService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

/**
 * 文件存储自动配置
 *
 * <p>按 {@code ycr.storage.type} 装配后端：{@code local}（默认，本地文件系统）或 {@code s3}（S3 兼容）。
 * 业务方自行实现 {@link FileStorageService} 亦可覆盖。通过 {@code ycr.storage.enabled=false} 关闭。</p>
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

    /**
     * S3 兼容后端装配。
     *
     * <p>仅当类路径存在 AWS SDK 的 {@link S3Client}（即业务方引入了可选依赖 {@code software.amazon.awssdk:s3}）
     * 且 {@code ycr.storage.type=s3} 时生效。{@link S3Client} 注册为 Bean，由容器在关闭时自动 close。</p>
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(S3Client.class)
    @ConditionalOnProperty(prefix = "ycr.storage", name = "type", havingValue = "s3")
    static class S3StorageConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public S3Client ycrS3Client(StorageProperties properties) {
            StorageProperties.S3 s3 = properties.getS3();
            S3ClientBuilder builder = S3Client.builder()
                    .region(Region.of(s3.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())));
            if (StringUtils.hasText(s3.getEndpoint())) {
                builder.endpointOverride(URI.create(s3.getEndpoint()));
            }
            if (s3.isPathStyleAccess()) {
                builder.forcePathStyle(true);
            }
            return builder.build();
        }

        @Bean
        @ConditionalOnMissingBean
        public FileStorageService s3FileStorageService(S3Client ycrS3Client, StorageProperties properties) {
            StorageProperties.S3 s3 = properties.getS3();
            return new S3FileStorageService(ycrS3Client, s3.getBucket(), s3.getUrlPrefix());
        }
    }
}
