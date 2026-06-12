package com.ycr.framework.i18n.util;

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
        I18nUtils.setMessageSource(source);
    }

    @AfterEach
    void tearDown() {
        I18nUtils.setMessageSource(null);
    }

    @Test
    void 应按语言取到不同文案() {
        assertEquals("Hello", I18nUtils.getMessage("greeting", Locale.ENGLISH));
        assertEquals("你好", I18nUtils.getMessage("greeting", Locale.SIMPLIFIED_CHINESE));
    }

    @Test
    void 占位参数应生效() {
        assertEquals("Welcome Tom", I18nUtils.getMessage("welcome", Locale.ENGLISH, "Tom"));
    }

    @Test
    void 缺失key应回退code() {
        assertEquals("missing.key", I18nUtils.getMessage("missing.key", Locale.ENGLISH));
    }

    @Test
    void 未绑定MessageSource应回退code() {
        I18nUtils.setMessageSource(null);
        assertEquals("greeting", I18nUtils.getMessage("greeting", Locale.ENGLISH));
    }
}
