package com.ycr.framework.translate.enums;

/**
 * 翻译类型
 *
 * <p>用于 {@link com.ycr.framework.translate.annotation.Translate#type()} 选择翻译源。
 * 每个取值都有真实落点：</p>
 * <ul>
 *     <li>{@link #DICT} —— 字典源（{@code DictTranslateSource}，依赖应用侧 {@code DictProvider}）</li>
 *     <li>{@link #ENUM} —— 枚举源（{@code EnumTranslateSource}，内置开箱即用）</li>
 *     <li>{@link #CUSTOM} —— 自定义源（按 {@code @Translate.source()} 名称选取应用注册的 {@code TranslateSource}）</li>
 * </ul>
 *
 * <p>注：关联表翻译不单设类型，应用以 {@link #CUSTOM} 注册一个内部查 Mapper 的源即可覆盖。</p>
 *
 * @author ycr
 */
public enum TranslateType {

    /** 字典翻译，源名固定为 {@code dict} */
    DICT("dict"),

    /** 枚举翻译，源名固定为 {@code enum} */
    ENUM("enum"),

    /** 自定义翻译，源名取 {@code @Translate.source()} */
    CUSTOM(null);

    private final String sourceName;

    TranslateType(String sourceName) {
        this.sourceName = sourceName;
    }

    /**
     * 内置类型对应的固定源名；{@link #CUSTOM} 返回 {@code null}（由注解的 source 决定）
     */
    public String getSourceName() {
        return sourceName;
    }
}
