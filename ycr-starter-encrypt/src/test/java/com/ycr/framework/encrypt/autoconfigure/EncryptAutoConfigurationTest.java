package com.ycr.framework.encrypt.autoconfigure;

import com.ycr.framework.encrypt.context.EncryptHandlerHolder;
import com.ycr.framework.encrypt.handler.EncryptHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class EncryptAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EncryptAutoConfiguration.class));

    @AfterEach
    void tearDown() {
        EncryptHandlerHolder.clear();
    }

    @Test
    void 配置AesKey时应创建EncryptHandler并初始化Holder() {
        contextRunner.withPropertyValues(
                        "ycr.encrypt.enabled=true",
                        "ycr.encrypt.aes-key=1234567890abcdef")
                .run(context -> {
                    assertThat(context).hasSingleBean(EncryptHandler.class);
                    assertThat(context).hasSingleBean(EncryptAutoConfiguration.EncryptHandlerLifecycle.class);
                    assertThat(EncryptHandlerHolder.getRequired()).isSameAs(context.getBean(EncryptHandler.class));
                });
    }

    @Test
    void 默认不应创建EncryptHandler与Lifecycle() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(EncryptHandler.class);
            assertThat(context).doesNotHaveBean(EncryptAutoConfiguration.EncryptHandlerLifecycle.class);
        });
    }

    @Test
    void 显式开启但未配置AesKey时不应创建Lifecycle() {
        contextRunner.withPropertyValues("ycr.encrypt.enabled=true")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(EncryptHandler.class);
                    assertThat(context).doesNotHaveBean(EncryptAutoConfiguration.EncryptHandlerLifecycle.class);
                });
    }
}
