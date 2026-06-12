package com.ycr.framework.i18n.autoconfigure;

import com.ycr.framework.i18n.resolver.HeaderLocaleResolver;
import com.ycr.framework.i18n.util.I18nUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.LocaleResolver;

/**
 * 国际化自动配置
 *
 * <p>将容器内既有的 {@link MessageSource}（Spring Boot 或应用提供）绑定到 {@link I18nUtils}——不自建以免与
 * Boot 的 {@code MessageSourceAutoConfiguration} 冲突；用 {@link ObjectProvider} 绑定，缺失也不报错。
 * Web 环境注册 {@link HeaderLocaleResolver}。通过 {@code ycr.i18n.enabled=false} 关闭。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@EnableConfigurationProperties(I18nProperties.class)
@ConditionalOnProperty(prefix = "ycr.i18n", name = "enabled", havingValue = "true", matchIfMissing = true)
public class I18nAutoConfiguration {

    public I18nAutoConfiguration(ObjectProvider<MessageSource> messageSourceProvider) {
        I18nUtils.setMessageSource(messageSourceProvider.getIfAvailable());
    }

    /**
     * Web 环境下的请求头语言解析器，应用可自定义 {@link LocaleResolver} 覆盖
     */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnMissingBean
    public LocaleResolver localeResolver(I18nProperties properties) {
        return new HeaderLocaleResolver(properties.getDefaultLocale());
    }
}
