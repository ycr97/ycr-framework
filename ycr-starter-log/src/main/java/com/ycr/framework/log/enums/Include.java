package com.ycr.framework.log.enums;

/**
 * 操作日志采集项枚举
 *
 * <p>通过全局 {@code ycr.log.includes} 设置默认采集项，{@code @Log} 注解的 includes/excludes 在其基础上增减。
 * 默认仅采集 IP，其余维度按需开启。</p>
 *
 * <p>请求体/响应体经 {@code LogJsonSupport} 序列化（字段名脱敏、截断、无 ObjectMapper 时软降级）；
 * 请求头对 Authorization/Cookie/Set-Cookie 强制脱敏；浏览器/操作系统由 Hutool 解析 User-Agent；
 * IP 归属地经 {@code IpRegionResolver} SPI（L1 默认 no-op）。</p>
 *
 * @author ycr
 */
public enum Include {

    /** 客户端 IP */
    IP_ADDRESS,

    /** 请求参数（query/form，敏感键已脱敏） */
    REQUEST_PARAMS,

    /** 请求体（@RequestBody 参数序列化，脱敏、截断） */
    REQUEST_BODY,

    /** 响应体（返回值序列化，脱敏、截断） */
    RESPONSE_BODY,

    /** 请求头（Authorization/Cookie/Set-Cookie 强制脱敏 + sensitiveKeys） */
    REQUEST_HEADERS,

    /** 浏览器（User-Agent 解析） */
    BROWSER,

    /** 操作系统（User-Agent 解析） */
    OS,

    /** IP 归属地（经 IpRegionResolver SPI） */
    IP_REGION
}
