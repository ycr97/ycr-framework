package com.ycr.framework.log.enums;

/**
 * 操作日志采集项枚举
 *
 * <p>通过全局 {@code ycr.log.includes} 设置默认采集项，{@code @Log} 注解的 includes/excludes 在其基础上增减。
 * 当前仅枚举「已真实实现且安全」的采集项，避免声明却不生效。</p>
 *
 * <p>以下采集项尚未实现，各有未决前提，待按需立项后再加入枚举（不在此提前暴露空开关）：</p>
 * <ul>
 *   <li>请求头：需对 Authorization/Cookie 等鉴权头做脱敏，否则泄露令牌。</li>
 *   <li>请求体：需引入请求体缓存包装（ContentCachingRequestWrapper）才能重复读取。</li>
 *   <li>响应体：需 JSON 序列化（依赖 json 模块）且需评估大响应体体积。</li>
 *   <li>浏览器 / 操作系统：需引入 User-Agent 解析器。</li>
 * </ul>
 *
 * @author ycr
 */
public enum Include {

    /** 客户端 IP */
    IP_ADDRESS,

    /** 请求参数（query/form，敏感键已脱敏） */
    REQUEST_PARAMS
}
