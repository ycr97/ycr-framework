package com.ycr.framework.captcha.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 验证码配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.captcha")
public class CaptchaProperties {

    /** 是否启用验证码，默认启用 */
    private boolean enabled = true;

    /** 图片宽度（像素） */
    private int width = 130;

    /** 图片高度（像素） */
    private int height = 48;

    /** 验证码字符数 */
    private int codeCount = 4;

    /** 干扰线条数 */
    private int lineCount = 5;

    /** 有效期（秒），超时自动过期 */
    private long expirationSeconds = 120;

    /** 缓存键前缀 */
    private String keyPrefix = "ycr:captcha";
}
