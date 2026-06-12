package com.ycr.framework.i18n.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;

/**
 * 国际化配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.i18n")
public class I18nProperties {

    /**
     * 是否启用国际化，默认启用
     */
    private boolean enabled = true;

    /**
     * 默认语言，默认简体中文
     */
    private Locale defaultLocale = Locale.SIMPLIFIED_CHINESE;
}
