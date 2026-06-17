package com.ycr.framework.feign.interceptor;

import feign.Request;
import feign.RequestTemplate;
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
    void allMatch恒为真() {
        assertTrue(RequestTemplateMatchers.allMatch().match(new RequestTemplate()));
    }

    @Test
    void httpMethod按方法名匹配() {
        RequestTemplate get = new RequestTemplate().method(Request.HttpMethod.GET);
        assertTrue(RequestTemplateMatchers.httpMethod("GET").match(get));
        assertFalse(RequestTemplateMatchers.httpMethod("POST").match(get));
    }

    @Test
    void requestPath按Ant风格匹配() {
        RequestTemplate template = new RequestTemplate().uri("/api/users/1");
        assertTrue(RequestTemplateMatchers.requestPath("/api/**").match(template));
        assertFalse(RequestTemplateMatchers.requestPath("/other/**").match(template));
    }

    @Test
    void clientName在无目标时返回假() {
        // 裸 RequestTemplate 未绑定 feignTarget，应安全返回 false 而非抛错
        assertFalse(RequestTemplateMatchers.clientName("any-svc").match(new RequestTemplate()));
    }

    @Test
    void 组合and与negate() {
        RequestTemplate template = new RequestTemplate().uri("/api/x");
        RequestTemplateMatcher path = RequestTemplateMatchers.requestPath("/api/**");
        assertTrue(path.match(template));
        assertFalse(path.negate().match(template));
        assertFalse(path.and(t -> false).match(template));
    }
}
