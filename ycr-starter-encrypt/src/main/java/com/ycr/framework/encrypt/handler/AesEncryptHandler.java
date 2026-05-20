package com.ycr.framework.encrypt.handler;

import cn.hutool.crypto.symmetric.AES;

import java.nio.charset.StandardCharsets;

/**
 * AES 加解密处理器
 *
 * @author ycr
 */
public class AesEncryptHandler implements EncryptHandler {

    private final AES aes;

    public AesEncryptHandler(String key) {
        this.aes = new AES(key.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        return aes.encryptHex(plainText);
    }

    @Override
    public String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }
        return aes.decryptStr(cipherText);
    }
}
