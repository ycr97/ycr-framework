package com.ycr.framework.i18n.resolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

/**
 * 请求头语言解析器
 *
 * <p>无状态解析当前请求语言：优先自定义头 {@code X-Lang}，其次标准 {@code Accept-Language} 首段，
 * 都没有则用默认语言。</p>
 *
 * @author ycr
 */
public class HeaderLocaleResolver implements LocaleResolver {

    /** 自定义语言头 */
    public static final String HEADER_LANG = "X-Lang";

    private final Locale defaultLocale;

    public HeaderLocaleResolver(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    @Override
    @NonNull
    public Locale resolveLocale(HttpServletRequest request) {
        String lang = request.getHeader(HEADER_LANG);
        if (lang != null && !lang.isBlank()) {
            return Locale.forLanguageTag(lang.replace("_", "-"));
        }
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null && !acceptLanguage.isBlank()) {
            return Locale.forLanguageTag(acceptLanguage.split(",")[0].trim());
        }
        return defaultLocale;
    }

    @Override
    public void setLocale(@NonNull HttpServletRequest request, HttpServletResponse response, Locale locale) {
        // 无状态解析器，不支持运行期设置
    }
}
