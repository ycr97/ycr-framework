package com.ycr.framework.translate.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 翻译模块配置
 *
 * @author ycr
 */
@ConfigurationProperties(prefix = "ycr.translate")
public class TranslateProperties {

    /**
     * 是否启用字段翻译（默认启用）
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
