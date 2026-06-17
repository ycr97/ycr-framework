package com.ycr.framework.feign.interceptor;

import feign.RequestTemplate;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Objects;

/**
 * 常用 {@link RequestTemplateMatcher} 工厂。
 *
 * @author ycr
 */
public final class RequestTemplateMatchers {

    private RequestTemplateMatchers() {
    }

    /** 匹配全部请求。 */
    public static RequestTemplateMatcher allMatch() {
        return template -> true;
    }

    /** 按 Feign client 名称匹配（{@code @FeignClient} 的 value/name）。无目标时返回 false。 */
    public static RequestTemplateMatcher clientName(String name) {
        return template -> template.feignTarget() != null
                && Objects.equals(template.feignTarget().name(), name);
    }

    /** 按请求路径以 Ant 风格匹配。 */
    public static RequestTemplateMatcher requestPath(String pattern) {
        PathMatcher pathMatcher = new AntPathMatcher();
        return template -> pathMatcher.match(pattern, template.path());
    }

    /** 按 HTTP 方法名（如 {@code GET}/{@code POST}）匹配。 */
    public static RequestTemplateMatcher httpMethod(String method) {
        return template -> Objects.equals(template.method(), method);
    }
}
