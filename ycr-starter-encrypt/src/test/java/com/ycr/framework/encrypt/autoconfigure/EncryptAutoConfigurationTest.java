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
            .withConfiguration(AutoConfigurations.of(EncryptAutoConfiguration.class));

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
                        "ycr.encrypt.enabled=true requires ycr.encrypt.aes-key, ycr.encrypt.keys, "
                                + "or a custom EncryptHandler bean"));
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

    @Test
    @DisplayName("密钥环应按current-key-id创建可轮换Handler")
    void keyRingShouldConfigureCurrentAndLegacyKeys() {
        contextRunner.withPropertyValues(
                        "ycr.encrypt.enabled=true",
                        "ycr.encrypt.current-key-id=key-2026",
                        "ycr.encrypt.legacy-key-id=key-2025",
                        "ycr.encrypt.keys.key-2025=1234567890abcdef",
                        "ycr.encrypt.keys.key-2026=abcdef1234567890")
                .run(context -> {
                    assertThat(context).hasSingleBean(EncryptHandler.class);
                    String encrypted = context.getBean(EncryptHandler.class).encrypt("secret");
                    assertThat(encrypted).startsWith("ycr:v1:aes-gcm:key-2026:");
                });
    }

    @Test
    @DisplayName("单密钥和密钥环同时配置时应拒绝启动")
    void ambiguousKeyConfigurationShouldFailFast() {
        contextRunner.withPropertyValues(
                        "ycr.encrypt.enabled=true",
                        "ycr.encrypt.aes-key=1234567890abcdef",
                        "ycr.encrypt.keys.default=abcdef1234567890")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "ycr.encrypt.aes-key 与 ycr.encrypt.keys 不得同时配置"));
    }
}
