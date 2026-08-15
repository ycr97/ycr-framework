package com.ycr.framework.feign.interceptor;

import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RequestTemplateMatchers 工厂匹配测试
 *
 * @author ycr
 */
class RequestTemplateMatchersTest {

    @Test
    @DisplayName("allMatch恒为真")
    void shouldMatchExpectedBehavior001() {
        assertTrue(RequestTemplateMatchers.allMatch().match(new RequestTemplate()));
    }

    @Test
    @DisplayName("httpMethod按方法名匹配")
    void shouldMatchExpectedBehavior002() {
        RequestTemplate get = new RequestTemplate().method(Request.HttpMethod.GET);
        assertTrue(RequestTemplateMatchers.httpMethod("GET").match(get));
        assertFalse(RequestTemplateMatchers.httpMethod("POST").match(get));
    }

    @Test
    @DisplayName("requestPath按Ant风格匹配")
    void shouldMatchExpectedBehavior003() {
        RequestTemplate template = new RequestTemplate().uri("/api/users/1");
        assertTrue(RequestTemplateMatchers.requestPath("/api/**").match(template));
        assertFalse(RequestTemplateMatchers.requestPath("/other/**").match(template));
    }

    @Test
    @DisplayName("clientName在无目标时返回假")
    void shouldMatchExpectedBehavior004() {
        // 裸 RequestTemplate 未绑定 feignTarget，应安全返回 false 而非抛错
        assertFalse(RequestTemplateMatchers.clientName("any-svc").match(new RequestTemplate()));
    }

    @Test
    @DisplayName("组合and与negate")
    void shouldMatchExpectedBehavior005() {
        RequestTemplate template = new RequestTemplate().uri("/api/x");
        RequestTemplateMatcher path = RequestTemplateMatchers.requestPath("/api/**");
        assertTrue(path.match(template));
        assertFalse(path.negate().match(template));
        assertFalse(path.and(t -> false).match(template));
    }
}
