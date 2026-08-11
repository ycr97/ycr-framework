package com.ycr.framework.encrypt.handler;

import cn.hutool.core.util.HexUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * AES-GCM 版本化 envelope 加解密处理器。
 *
 * <p>新密文格式：{@code ycr:v1:aes-gcm:key-id:base64url(nonce):base64url(ciphertext+tag)}。
 * 历史无前缀十六进制密文按 AES-CBC + IV 前缀只读兼容。</p>
 *
 * @author ycr
 */
public class AesEncryptHandler implements EncryptHandler {

    private static final String ENVELOPE_PREFIX = "ycr:v1:aes-gcm:";
    private static final int GCM_NONCE_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final int LEGACY_IV_LENGTH = 16;
    private static final Pattern KEY_ID_PATTERN = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    private final String currentKeyId;
    private final Map<String, SecretKeySpec> keys;
    private final SecretKeySpec legacyKey;
    private final SecureRandom random = new SecureRandom();

    public AesEncryptHandler(String key) {
        this("default", Map.of("default", key), "default");
    }

    public AesEncryptHandler(String currentKeyId, Map<String, String> keys, String legacyKeyId) {
        validateKeyId(currentKeyId);
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("AES 密钥环不能为空");
        }
        Map<String, SecretKeySpec> parsed = new LinkedHashMap<>();
        keys.forEach((keyId, keyValue) -> {
            validateKeyId(keyId);
            parsed.put(keyId, key(keyValue));
        });
        SecretKeySpec currentKey = parsed.get(currentKeyId);
        if (currentKey == null) {
            throw new IllegalArgumentException("current-key-id 在密钥环中不存在: " + currentKeyId);
        }
        String resolvedLegacyKeyId = legacyKeyId == null || legacyKeyId.isBlank()
                ? currentKeyId : legacyKeyId;
        SecretKeySpec resolvedLegacyKey = parsed.get(resolvedLegacyKeyId);
        if (resolvedLegacyKey == null) {
            throw new IllegalArgumentException("legacy-key-id 在密钥环中不存在: " + resolvedLegacyKeyId);
        }
        this.currentKeyId = currentKeyId;
        this.keys = Map.copyOf(parsed);
        this.legacyKey = resolvedLegacyKey;
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        String header = ENVELOPE_PREFIX + currentKeyId;
        byte[] nonce = new byte[GCM_NONCE_LENGTH];
        random.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keys.get(currentKeyId), new GCMParameterSpec(GCM_TAG_BITS, nonce));
            cipher.updateAAD(header.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
            return header + ":" + encoder.encodeToString(nonce) + ":" + encoder.encodeToString(encrypted);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("AES-GCM 加密失败", exception);
        }
    }

    @Override
    public String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }
        if (cipherText.startsWith("ycr:")) {
            return decryptEnvelope(cipherText);
        }
        return decryptLegacyCbc(cipherText);
    }

    private String decryptEnvelope(String cipherText) {
        String[] parts = cipherText.split(":", -1);
        if (parts.length != 6 || !"ycr".equals(parts[0]) || !"v1".equals(parts[1])
                || !"aes-gcm".equals(parts[2])) {
            throw new IllegalArgumentException("不支持或损坏的加密 envelope");
        }
        String keyId = parts[3];
        SecretKeySpec key = keys.get(keyId);
        if (key == null) {
            throw new IllegalArgumentException("密文引用了未知 key-id: " + keyId);
        }
        String header = ENVELOPE_PREFIX + keyId;
        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
            byte[] nonce = decoder.decode(parts[4]);
            byte[] encrypted = decoder.decode(parts[5]);
            if (!parts[4].equals(encoder.encodeToString(nonce))
                    || !parts[5].equals(encoder.encodeToString(encrypted))) {
                throw new IllegalArgumentException("AES-GCM envelope 包含非规范 Base64URL 编码");
            }
            if (nonce.length != GCM_NONCE_LENGTH) {
                throw new IllegalArgumentException("AES-GCM nonce 长度非法");
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, nonce));
            cipher.updateAAD(header.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("AES-GCM 密文校验或解密失败", exception);
        }
    }

    private String decryptLegacyCbc(String cipherText) {
        try {
            byte[] bytes = HexUtil.decodeHex(cipherText);
            if (bytes.length <= LEGACY_IV_LENGTH) {
                throw new IllegalArgumentException("历史 AES-CBC 密文长度非法");
            }
            byte[] iv = new byte[LEGACY_IV_LENGTH];
            byte[] encrypted = new byte[bytes.length - LEGACY_IV_LENGTH];
            System.arraycopy(bytes, 0, iv, 0, LEGACY_IV_LENGTH);
            System.arraycopy(bytes, LEGACY_IV_LENGTH, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, legacyKey, new IvParameterSpec(iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("历史 AES-CBC 密文解密失败", exception);
        }
    }

    private static SecretKeySpec key(String value) {
        if (value == null) {
            throw new IllegalArgumentException("AES 密钥不能为空");
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length != 16 && bytes.length != 24 && bytes.length != 32) {
            throw new IllegalArgumentException("AES 密钥必须是 16/24/32 UTF-8 字节");
        }
        return new SecretKeySpec(bytes, "AES");
    }

    private static void validateKeyId(String keyId) {
        if (keyId == null || !KEY_ID_PATTERN.matcher(keyId).matches()) {
            throw new IllegalArgumentException("AES key-id 仅允许 1-64 位字母、数字、点、下划线和连字符");
        }
    }
}
