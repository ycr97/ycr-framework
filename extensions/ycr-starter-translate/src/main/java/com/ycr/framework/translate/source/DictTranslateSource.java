package com.ycr.framework.translate.source;

/**
 * 字典翻译源（内置机制，数据来自应用侧 {@link DictProvider}）
 *
 * <p>{@code key} 为字典编码，{@code code} 为字典项编码，委托 {@link DictProvider#getLabel} 取文本。</p>
 *
 * @author ycr
 */
public class DictTranslateSource implements TranslateSource {

    /** 内置源名 */
    public static final String NAME = "dict";

    private final DictProvider dictProvider;

    public DictTranslateSource(DictProvider dictProvider) {
        this.dictProvider = dictProvider;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String translate(String key, Object code) {
        if (key == null || key.isEmpty() || code == null) {
            return null;
        }
        return dictProvider.getLabel(key, String.valueOf(code));
    }
}
