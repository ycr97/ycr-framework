package com.ycr.framework.feign.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Feign 增强配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.feign")
public class FeignProperties {

    /** 是否启用上下文/Trace 透传，默认启用 */
    private boolean contextPassEnabled = true;

    /** 是否启用下游统一错误解码，默认启用 */
    private boolean errorDecoderEnabled = true;
}
