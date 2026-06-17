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

    /** 是否透传当前请求语言头到下游，默认启用 */
    private boolean localePassEnabled = true;

    /** 语言头名称，默认 Accept-Language */
    private String languageHeader = "Accept-Language";

    /** 是否透传当前请求 Authorization 原始 token 到下游，默认关闭 */
    private boolean tokenPassEnabled = false;
}
