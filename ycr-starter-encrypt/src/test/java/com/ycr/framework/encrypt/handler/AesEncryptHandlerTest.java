package com.ycr.framework.encrypt.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AesEncryptHandlerTest {

    private AesEncryptHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AesEncryptHandler("1234567890abcdef");
    }

    @Test
    void 加密后再解密应得到原文() {
        String plainText = "手机号13800138000";
        String encrypted = handler.encrypt(plainText);
        assertNotEquals(plainText, encrypted);

        String decrypted = handler.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    void 相同明文每次加密结果不同_CBC随机IV() {
        String text = "测试数据";
        String enc1 = handler.encrypt(text);
        String enc2 = handler.encrypt(text);
        assertNotEquals(enc1, enc2);
        // 但两次解密结果一致
        assertEquals(text, handler.decrypt(enc1));
        assertEquals(text, handler.decrypt(enc2));
    }

    @Test
    void null输入应返回null() {
        assertNull(handler.encrypt(null));
        assertNull(handler.decrypt(null));
    }
}
