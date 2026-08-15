package com.ycr.framework.business.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 接入层拦截链配置
 *
 * @author ycr
 */
@ConfigurationProperties(prefix = "ycr.business")
public class BusinessProperties {

    /**
     * 是否启用 {@code @BizApi} 拦截链（默认启用）
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
