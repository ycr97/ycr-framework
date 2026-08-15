package com.ycr.framework.feign.interceptor;

import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TokenPassInterceptor 原始 token 透传测试
 *
 * @author ycr
 */
class TokenPassInterceptorTest {

    private final TokenPassInterceptor interceptor = new TokenPassInterceptor();

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("透传当前请求的Authorization头")
    void shouldMatchExpectedBehavior001() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer abc.def");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertTrue(template.headers().get(HttpHeaders.AUTHORIZATION).contains("Bearer abc.def"));
    }

    @Test
    @DisplayName("无请求上下文时不写入")
    void shouldMatchExpectedBehavior002() {
        RequestContextHolder.resetRequestAttributes();

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey(HttpHeaders.AUTHORIZATION));
    }
}
