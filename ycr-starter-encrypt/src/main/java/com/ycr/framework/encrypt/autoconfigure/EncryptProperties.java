package com.ycr.framework.encrypt.autoconfigure;

import com.ycr.framework.encrypt.enums.EncryptAlgorithm;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 加解密配置属性
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.encrypt")
public class EncryptProperties {

    /** 是否启用字段加解密 */
    private boolean enabled = true;

    /** 默认加密算法 */
    private EncryptAlgorithm algorithm = EncryptAlgorithm.AES;

    /** AES 密钥（16/24/32 字节） */
    private String aesKey = "";
}
