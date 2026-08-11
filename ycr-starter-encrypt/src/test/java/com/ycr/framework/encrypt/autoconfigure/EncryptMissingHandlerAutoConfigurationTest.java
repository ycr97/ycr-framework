package com.ycr.framework.encrypt.autoconfigure;

import com.ycr.framework.encrypt.handler.EncryptHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EncryptMissingHandlerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EncryptMissingHandlerAutoConfiguration.class));

    @Test
    @DisplayName("显式启用但缺少处理器时应启动失败")
    void enabledWithoutHandlerShouldFailFast() {
        contextRunner.withPropertyValues("ycr.encrypt.enabled=true")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "ycr.encrypt.enabled=true requires ycr.encrypt.aes-key or a custom EncryptHandler bean"));
    }

    @Test
    @DisplayName("业务提供处理器时缺失处理器门禁应让位")
    void customHandlerShouldSatisfyGate() {
        contextRunner.withPropertyValues("ycr.encrypt.enabled=true")
                .withBean(EncryptHandler.class, () -> mock(EncryptHandler.class))
                .run(context -> assertThat(context).hasSingleBean(EncryptHandler.class));
    }
}
