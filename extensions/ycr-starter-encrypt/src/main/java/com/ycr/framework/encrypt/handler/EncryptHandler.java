package com.ycr.framework.encrypt.handler;

/**
 * 加解密处理器接口
 *
 * @author ycr
 */
public interface EncryptHandler {

    /**
     * 加密
     */
    String encrypt(String plainText);

    /**
     * 解密
     */
    String decrypt(String cipherText);
}
