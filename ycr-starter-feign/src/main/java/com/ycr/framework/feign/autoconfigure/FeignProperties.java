package com.ycr.framework.feign.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Feign 增强配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.feign")
public class FeignProperties {

    /** 是否启用上下文/Trace 透传，默认关闭 */
    private boolean contextPassEnabled = false;

    /** 允许接收内部身份上下文或原始 Token 的 Feign client 名称 */
    private List<String> internalClients = new ArrayList<>();

    /** 是否启用下游统一错误解码，默认启用 */
    private boolean errorDecoderEnabled = true;

    /** 是否透传当前请求语言头到下游，默认启用 */
    private boolean localePassEnabled = true;

    /** 语言头名称，默认 Accept-Language */
    private String languageHeader = "Accept-Language";

    /** 是否透传当前请求 Authorization 原始 token 到下游，默认关闭 */
    private boolean tokenPassEnabled = false;
}
