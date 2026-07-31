package com.ycr.framework.i18n.resolver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * HeaderLocaleResolver 真实行为测试
 *
 * @author ycr
 */
class HeaderLocaleResolverTest {

    private final HeaderLocaleResolver resolver = new HeaderLocaleResolver(Locale.SIMPLIFIED_CHINESE);

    @Test
    @DisplayName("应优先用X_Lang头")
    void shouldMatchExpectedBehavior001() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HeaderLocaleResolver.HEADER_LANG, "en");

        assertEquals(Locale.ENGLISH, resolver.resolveLocale(request));
    }

    @Test
    @DisplayName("无X_Lang时用AcceptLanguage首段")
    void shouldMatchExpectedBehavior002() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

        assertEquals(Locale.forLanguageTag("zh-CN"), resolver.resolveLocale(request));
    }

    @Test
    @DisplayName("两者皆无时用默认语言")
    void shouldMatchExpectedBehavior003() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertEquals(Locale.SIMPLIFIED_CHINESE, resolver.resolveLocale(request));
    }
}
