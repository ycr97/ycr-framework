package com.ycr.framework.encrypt.autoconfigure;

import com.ycr.framework.encrypt.enums.EncryptAlgorithm;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 加解密配置属性
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.encrypt")
public class EncryptProperties {

    /** 是否启用字段加解密 */
    private boolean enabled = false;

    /** 默认加密算法 */
    private EncryptAlgorithm algorithm = EncryptAlgorithm.AES;

    /**
     * 兼容单密钥配置（16/24/32 UTF-8 字节）。
     *
     * <p>配置 {@link #keys} 后不得再配置本项。</p>
     */
    private String aesKey = "";

    /** 当前写入密文使用的密钥 ID */
    private String currentKeyId = "default";

    /** AES 密钥环：key-id -> 16/24/32 UTF-8 字节密钥 */
    private Map<String, String> keys = new LinkedHashMap<>();

    /** 历史无版本 AES-CBC 密文使用的密钥 ID；留空时使用 current-key-id */
    private String legacyKeyId = "";
}
