package com.ycr.framework.translate.source;

/**
 * 翻译源 SPI
 *
 * <p>把「分组键 + 编码」解析为展示文本。框架内置 {@link EnumTranslateSource}（枚举）与
 * {@link DictTranslateSource}（字典），应用可注册任意自定义实现（如关联表查询），
 * 以 {@link #name()} 作为注册标识被 {@code @Translate(type = CUSTOM, source = ...)} 选取。</p>
 *
 * @author ycr
 */
public interface TranslateSource {

    /**
     * 源名称，作为注册表索引键；内置源固定为 {@code dict} / {@code enum}
     */
    String name();

    /**
     * 翻译
     *
     * @param key  分组键（字典编码 / 枚举类名 / 自定义语义），可能为空
     * @param code 待翻译的编码值，可能为 {@code null}
     * @return 展示文本；无法翻译时返回 {@code null}（序列化时该目标字段写出 null）
     */
    String translate(String key, Object code);
}
