package com.ycr.framework.feign.interceptor;

import feign.RequestTemplate;

/**
 * Feign 请求模板匹配器：判定某个拦截器是否对当前请求生效。
 *
 * <p>支持 {@link #and}/{@link #or}/{@link #negate} 组合，用于按 client 名称、路径、方法等规则
 * 选择性地让拦截器只作用于部分下游请求。</p>
 *
 * @author ycr
 */
@FunctionalInterface
public interface RequestTemplateMatcher {

    /**
     * 是否匹配当前请求模板。
     *
     * @param template feign 请求模板
     * @return true 表示命中
     */
    boolean match(RequestTemplate template);

    default RequestTemplateMatcher and(RequestTemplateMatcher other) {
        return template -> this.match(template) && other.match(template);
    }

    default RequestTemplateMatcher or(RequestTemplateMatcher other) {
        return template -> this.match(template) || other.match(template);
    }

    default RequestTemplateMatcher negate() {
        return template -> !this.match(template);
    }
}
