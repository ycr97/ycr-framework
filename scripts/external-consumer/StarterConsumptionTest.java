package com.example;

import com.ycr.framework.encrypt.handler.AesEncryptHandler;
import com.ycr.framework.web.autoconfigure.WebAutoConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StarterConsumptionTest {

    @Test
    void bomShouldResolveFrameworkStartersOutsideReactor() {
        assertThat(WebAutoConfiguration.class.getName())
                .isEqualTo("com.ycr.framework.web.autoconfigure.WebAutoConfiguration");
        AesEncryptHandler handler = new AesEncryptHandler("1234567890abcdef");
        String encrypted = handler.encrypt("secret");
        assertThat(encrypted).startsWith("ycr:v1:aes-gcm:default:");
        assertThat(handler.decrypt(encrypted)).isEqualTo("secret");
    }
}
