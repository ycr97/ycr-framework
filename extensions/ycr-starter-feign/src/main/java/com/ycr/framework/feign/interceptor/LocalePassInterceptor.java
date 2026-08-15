package com.ycr.framework.feign.interceptor;

import feign.RequestTemplate;
import org.springframework.util.StringUtils;

/**
 * Locale 透传拦截器：把当前入站请求的语言头转发给下游，使多语言上下文跨服务保持一致。
 *
 * <p>ycr 不维护独立的 locale 上下文持有器，故语言取自当前 Servlet 请求头；无请求上下文或头为空时静默跳过。</p>
 *
 * @author ycr
 */
public class LocalePassInterceptor extends AbstractMatchableFeignInterceptor {

    private final String languageHeader;

    public LocalePassInterceptor(String languageHeader) {
        this.languageHeader = languageHeader;
    }

    @Override
    protected void doApply(RequestTemplate template) {
        String language = CurrentRequestHelper.header(languageHeader);
        if (StringUtils.hasText(language)) {
            template.header(languageHeader, language);
        }
    }
}
