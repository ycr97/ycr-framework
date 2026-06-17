package com.ycr.framework.feign.interceptor;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LocalePassInterceptor 语言头透传测试
 *
 * @author ycr
 */
class LocalePassInterceptorTest {

    private final LocalePassInterceptor interceptor = new LocalePassInterceptor("Accept-Language");

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void 透传当前请求的语言头() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "zh-CN");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertTrue(template.headers().get("Accept-Language").contains("zh-CN"));
    }

    @Test
    void 请求无语言头时不写入() {
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest()));

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey("Accept-Language"));
    }

    @Test
    void 无请求上下文时不写入() {
        RequestContextHolder.resetRequestAttributes();

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey("Accept-Language"));
    }
}
