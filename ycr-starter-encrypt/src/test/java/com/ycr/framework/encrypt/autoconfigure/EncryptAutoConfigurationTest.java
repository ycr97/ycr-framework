package com.ycr.framework.encrypt.autoconfigure;

import com.ycr.framework.encrypt.context.EncryptHandlerHolder;
import com.ycr.framework.encrypt.handler.EncryptHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class EncryptAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    EncryptAutoConfiguration.class, EncryptMissingHandlerAutoConfiguration.class));

    @AfterEach
    void tearDown() {
        EncryptHandlerHolder.clear();
    }

    @Test
    @DisplayName("配置AesKey时应创建EncryptHandler并初始化Holder")
    void shouldMatchExpectedBehavior001() {
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
    @DisplayName("默认不应创建EncryptHandler与Lifecycle")
    void shouldMatchExpectedBehavior002() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(EncryptHandler.class);
            assertThat(context).doesNotHaveBean(EncryptAutoConfiguration.EncryptHandlerLifecycle.class);
        });
    }

    @Test
    @DisplayName("显式开启但未配置AesKey时应启动失败")
    void shouldFailWhenEnabledWithoutKeyOrCustomHandler() {
        contextRunner.withPropertyValues("ycr.encrypt.enabled=true")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "ycr.encrypt.enabled=true requires ycr.encrypt.aes-key or a custom EncryptHandler bean"));
    }

    @Test
    @DisplayName("显式开启且提供自定义Handler时应初始化Holder")
    void shouldMatchExpectedBehavior004() {
        EncryptHandler custom = new EncryptHandler() {
            @Override
            public String encrypt(String plaintext) {
                return plaintext;
            }

            @Override
            public String decrypt(String ciphertext) {
                return ciphertext;
            }
        };
        contextRunner.withBean(EncryptHandler.class, () -> custom)
                .withPropertyValues("ycr.encrypt.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(EncryptAutoConfiguration.EncryptHandlerLifecycle.class);
                    assertThat(EncryptHandlerHolder.getRequired()).isSameAs(custom);
                });
    }
}
