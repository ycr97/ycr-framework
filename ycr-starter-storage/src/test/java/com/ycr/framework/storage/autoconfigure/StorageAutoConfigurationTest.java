package com.ycr.framework.storage.autoconfigure;

import com.ycr.framework.storage.model.FileInfo;
import com.ycr.framework.storage.service.FileStorageService;
import com.ycr.framework.storage.service.LocalFileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StorageAutoConfiguration 装配与开关测试
 *
 * @author ycr
 */
class StorageAutoConfigurationTest {

    @TempDir
    Path tempDir;

    private ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(StorageAutoConfiguration.class))
                .withPropertyValues("ycr.storage.local.path=" + tempDir);
    }

    @Test
    @DisplayName("默认不应装配本地实现")
    void shouldMatchExpectedBehavior001() {
        runner().run(context -> assertThat(context).doesNotHaveBean(FileStorageService.class));
    }

    @Test
    @DisplayName("显式开启时应装配本地实现")
    void shouldMatchExpectedBehavior002() {
        runner().withPropertyValues("ycr.storage.enabled=true").run(context -> {
            assertThat(context).hasSingleBean(FileStorageService.class);
            assertThat(context.getBean(FileStorageService.class)).isInstanceOf(LocalFileStorageService.class);
        });
    }

    @Test
    @DisplayName("业务自定义实现应覆盖默认")
    void shouldMatchExpectedBehavior003() {
        runner().withPropertyValues("ycr.storage.enabled=true")
                .withUserConfiguration(CustomConfig.class).run(context -> {
            assertThat(context).hasSingleBean(FileStorageService.class);
            assertThat(context.getBean(FileStorageService.class)).isInstanceOf(CustomStorage.class);
        });
    }

    @Configuration
    static class CustomConfig {
        @Bean
        FileStorageService customStorage() {
            return new CustomStorage();
        }
    }

    static class CustomStorage implements FileStorageService {
        @Override
        public FileInfo upload(InputStream content, String originalFilename) {
            return null;
        }

        @Override
        public InputStream download(String path) {
            return null;
        }

        @Override
        public boolean delete(String path) {
            return false;
        }

        @Override
        public boolean exists(String path) {
            return false;
        }
    }
}
