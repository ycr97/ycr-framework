package com.ycr.framework.log.enums;

/**
 * 操作日志采集项枚举
 *
 * <p>通过全局 {@code ycr.log.includes} 设置默认采集项，{@code @Log} 注解的 includes/excludes 在其基础上增减。</p>
 *
 * @author ycr
 */
public enum Include {

    /** 请求头 */
    REQUEST_HEADERS,

    /** 请求参数（query/form） */
    REQUEST_PARAMS,

    /** 请求体（JSON body） */
    REQUEST_BODY,

    /** 响应体 */
    RESPONSE_BODY,

    /** 客户端 IP */
    IP_ADDRESS,

    /** 浏览器信息 */
    BROWSER,

    /** 操作系统 */
    OS
}
