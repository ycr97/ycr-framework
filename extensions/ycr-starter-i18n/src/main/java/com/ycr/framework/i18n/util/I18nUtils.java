package com.ycr.framework.i18n.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * 国际化工具类
 *
 * <p>静态门面，{@link MessageSource} 由自动配置从容器内既有 Bean 绑定（复用 Spring Boot 的 MessageSource）。
 * 取文案时按 {@link LocaleContextHolder} 当前语言解析；未绑定 MessageSource 时回退返回 code 本身。</p>
 *
 * @author ycr
 */
public final class I18nUtils {

    private static volatile MessageSource messageSource;

    private I18nUtils() {
    }

    /**
     * 绑定 MessageSource（由自动配置调用）
     */
    public static void setMessageSource(MessageSource messageSource) {
        I18nUtils.messageSource = messageSource;
    }

    /**
     * 取当前语言的国际化文案
     *
     * @param code 消息编码
     * @param args 占位参数
     * @return 文案，缺失或未绑定 MessageSource 时返回 code
     */
    public static String getMessage(String code, Object... args) {
        return getMessage(code, LocaleContextHolder.getLocale(), args);
    }

    /**
     * 取指定语言的国际化文案
     *
     * @param code   消息编码
     * @param locale 语言
     * @param args   占位参数
     * @return 文案，缺失或未绑定 MessageSource 时返回 code
     */
    public static String getMessage(String code, Locale locale, Object... args) {
        if (messageSource == null) {
            return code;
        }
        return messageSource.getMessage(code, args, code, locale);
    }
}
