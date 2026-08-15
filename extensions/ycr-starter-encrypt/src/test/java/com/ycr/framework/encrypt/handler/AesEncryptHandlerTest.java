package com.ycr.framework.encrypt.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AesEncryptHandlerTest {

    private AesEncryptHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AesEncryptHandler("1234567890abcdef");
    }

    @Test
    @DisplayName("AES-GCM envelope加密后再解密应得到原文")
    void encryptedEnvelopeShouldRoundTrip() {
        String plainText = "手机号13800138000";
        String encrypted = handler.encrypt(plainText);
        assertNotEquals(plainText, encrypted);
        assertTrue(encrypted.startsWith("ycr:v1:aes-gcm:default:"));

        String decrypted = handler.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("相同明文应因随机nonce产生不同GCM密文")
    void samePlaintextShouldProduceDifferentCiphertext() {
        String text = "测试数据";
        String enc1 = handler.encrypt(text);
        String enc2 = handler.encrypt(text);
        assertNotEquals(enc1, enc2);
        assertEquals(text, handler.decrypt(enc1));
        assertEquals(text, handler.decrypt(enc2));
    }

    @Test
    @DisplayName("null输入应返回null")
    void nullInputShouldReturnNull() {
        assertNull(handler.encrypt(null));
        assertNull(handler.decrypt(null));
    }

    @Test
    @DisplayName("轮换后应使用新key-id写入并继续读取旧GCM密文")
    void rotatedKeyRingShouldReadOldEnvelopeAndWriteWithCurrentKey() {
        AesEncryptHandler oldHandler = new AesEncryptHandler(
                "key-2025", Map.of("key-2025", "1234567890abcdef"), "key-2025");
        String oldCiphertext = oldHandler.encrypt("历史数据");

        AesEncryptHandler rotatedHandler = new AesEncryptHandler(
                "key-2026",
                Map.of(
                        "key-2025", "1234567890abcdef",
                        "key-2026", "abcdef1234567890"),
                "key-2025");

        assertEquals("历史数据", rotatedHandler.decrypt(oldCiphertext));
        assertTrue(rotatedHandler.encrypt("新数据").startsWith("ycr:v1:aes-gcm:key-2026:"));
    }

    @Test
    @DisplayName("应使用legacy-key-id只读兼容历史CBC密文")
    void legacyCbcCiphertextShouldRemainReadable() throws Exception {
        String legacyCiphertext = legacyEncrypt("历史手机号", "1234567890abcdef");
        AesEncryptHandler rotatedHandler = new AesEncryptHandler(
                "key-2026",
                Map.of(
                        "key-2025", "1234567890abcdef",
                        "key-2026", "abcdef1234567890"),
                "key-2025");

        assertEquals("历史手机号", rotatedHandler.decrypt(legacyCiphertext));
    }

    @Test
    @DisplayName("篡改GCM密文、元数据或Base64URL表示时应校验失败")
    void tamperedEnvelopeShouldFailAuthentication() {
        String encrypted = handler.encrypt("敏感数据");
        char replacement = encrypted.endsWith("A") ? 'B' : 'A';
        String tampered = encrypted.substring(0, encrypted.length() - 1) + replacement;

        assertThrows(IllegalArgumentException.class, () -> handler.decrypt(tampered));
        assertThrows(IllegalArgumentException.class,
                () -> handler.decrypt(encrypted.replace(":default:", ":unknown:")));
    }

    @Test
    @DisplayName("非法密钥长度和缺失current-key-id应立即失败")
    void invalidKeyConfigurationShouldFailFast() {
        assertThrows(IllegalArgumentException.class, () -> new AesEncryptHandler("too-short"));
        assertThrows(IllegalArgumentException.class, () -> new AesEncryptHandler(
                "missing", Map.of("default", "1234567890abcdef"), "default"));
    }

    private String legacyEncrypt(String plaintext, String key) throws Exception {
        byte[] iv = "fixed-legacy-iv!".getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES"),
                new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] envelope = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, envelope, 0, iv.length);
        System.arraycopy(encrypted, 0, envelope, iv.length, encrypted.length);
        return HexFormat.of().formatHex(envelope);
    }
}
