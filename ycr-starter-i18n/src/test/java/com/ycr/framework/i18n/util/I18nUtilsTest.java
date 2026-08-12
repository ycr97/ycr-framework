package com.ycr.framework.i18n.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * I18nUtils 真实消息解析测试
 *
 * @author ycr
 */
class I18nUtilsTest {

    @BeforeEach
    void setUp() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:i18n/test_messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        I18nUtils.setMessageSource(source);
    }

    @AfterEach
    void tearDown() {
        I18nUtils.setMessageSource(null);
    }

    @Test
    @DisplayName("应按语言取到不同文案")
    void shouldResolveMessageForRequestedLocale() {
        assertEquals("Hello", I18nUtils.getMessage("greeting", Locale.ENGLISH));
        assertEquals("你好", I18nUtils.getMessage("greeting", Locale.SIMPLIFIED_CHINESE));
    }

    @Test
    @DisplayName("占位参数应生效")
    void shouldResolveMessageArguments() {
        assertEquals("Welcome Tom", I18nUtils.getMessage("welcome", Locale.ENGLISH, "Tom"));
    }

    @Test
    @DisplayName("缺失key应回退code")
    void shouldReturnCodeWhenMessageIsMissing() {
        assertEquals("missing.key", I18nUtils.getMessage("missing.key", Locale.ENGLISH));
    }

    @Test
    @DisplayName("未绑定MessageSource应回退code")
    void shouldReturnCodeWhenMessageSourceIsNotBound() {
        I18nUtils.setMessageSource(null);
        assertEquals("greeting", I18nUtils.getMessage("greeting", Locale.ENGLISH));
    }
}
