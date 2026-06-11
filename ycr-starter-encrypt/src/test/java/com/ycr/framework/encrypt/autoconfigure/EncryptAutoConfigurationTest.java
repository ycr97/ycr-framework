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
        contextRunner.withPropertyValues("ycr.encrypt.aes-key=1234567890abcdef")
                .run(context -> {
                    assertThat(context).hasSingleBean(EncryptHandler.class);
                    assertThat(EncryptHandlerHolder.getRequired()).isSameAs(context.getBean(EncryptHandler.class));
                });
    }

    @Test
    void 关闭加密时不应创建EncryptHandler() {
        contextRunner.withPropertyValues(
                        "ycr.encrypt.enabled=false",
                        "ycr.encrypt.aes-key=1234567890abcdef")
                .run(context -> assertThat(context).doesNotHaveBean(EncryptHandler.class));
    }
}
