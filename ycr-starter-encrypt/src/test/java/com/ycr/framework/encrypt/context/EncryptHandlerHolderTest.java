package com.ycr.framework.encrypt.context;

import com.ycr.framework.encrypt.handler.EncryptHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EncryptHandlerHolderTest {

    @AfterEach
    void tearDown() {
        EncryptHandlerHolder.clear();
    }

    @Test
    void set后getRequired应返回同一个处理器() {
        EncryptHandler handler = new EncryptHandler() {
            @Override
            public String encrypt(String plainText) {
                return plainText;
            }

            @Override
            public String decrypt(String cipherText) {
                return cipherText;
            }
        };

        EncryptHandlerHolder.set(handler);

        assertSame(handler, EncryptHandlerHolder.getRequired());
    }

    @Test
    void clear后getRequired应抛出异常() {
        EncryptHandlerHolder.clear();

        assertThrows(IllegalStateException.class, EncryptHandlerHolder::getRequired);
    }

    @Test
    void set不允许空处理器() {
        assertThrows(IllegalArgumentException.class, () -> EncryptHandlerHolder.set(null));
    }
}
