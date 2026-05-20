package com.ycr.framework.encrypt.handler;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.core.util.HexUtil;

import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * AES 加解密处理器（CBC 模式，随机 IV 前缀）
 *
 * @author ycr
 */
public class AesEncryptHandler implements EncryptHandler {

    private static final int IV_LENGTH = 16;
    private final byte[] key;
    private final SecureRandom random = new SecureRandom();

    public AesEncryptHandler(String key) {
        this.key = key.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key, iv);
        byte[] encrypted = aes.encrypt(plainText);
        // IV + ciphertext，十六进制编码
        byte[] result = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, result, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, result, IV_LENGTH, encrypted.length);
        return HexUtil.encodeHexStr(result);
    }

    @Override
    public String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }
        byte[] bytes = HexUtil.decodeHex(cipherText);
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(bytes, 0, iv, 0, IV_LENGTH);
        byte[] encrypted = new byte[bytes.length - IV_LENGTH];
        System.arraycopy(bytes, IV_LENGTH, encrypted, 0, encrypted.length);
        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key, iv);
        return aes.decryptStr(encrypted);
    }
}
