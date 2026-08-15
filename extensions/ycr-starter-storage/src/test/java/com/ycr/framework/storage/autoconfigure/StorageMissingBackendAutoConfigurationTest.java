package com.ycr.framework.storage.autoconfigure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class StorageMissingBackendAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    StorageAutoConfiguration.class, StorageMissingBackendAutoConfiguration.class));

    @Test
    @DisplayName("未知存储类型应启动失败")
    void unsupportedStorageTypeShouldFailFast() {
        contextRunner.withPropertyValues(
                        "ycr.storage.enabled=true",
                        "ycr.storage.type=unknown")
                .run(context -> assertThat(context.getStartupFailure())
                        .hasRootCauseMessage("Unsupported ycr.storage.type: unknown"));
    }

    @Test
    @DisplayName("启用S3但运行时缺少SDK应启动失败")
    void missingS3SdkShouldFailFast() {
        contextRunner.withClassLoader(new FilteredClassLoader("software.amazon.awssdk.services.s3"))
                .withPropertyValues(
                        "ycr.storage.enabled=true",
                        "ycr.storage.type=s3")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "ycr.storage.type=s3 requires software.amazon.awssdk:s3 on the runtime classpath"));
    }
}
