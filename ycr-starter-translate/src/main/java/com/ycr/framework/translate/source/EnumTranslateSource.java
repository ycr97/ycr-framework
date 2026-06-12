package com.ycr.framework.translate.source;

import com.ycr.framework.core.enums.BaseEnum;

/**
 * 枚举翻译源（内置，开箱即用）
 *
 * <p>{@code key} 为 {@link BaseEnum} 实现枚举的全限定类名，{@code code} 为其 {@code getValue()} 编码，
 * 翻译为 {@code getDescription()}。比较时两侧统一转字符串，容忍 JSON 数字/字符串差异（如 {@code "1"} 匹配 {@code 1}）。</p>
 *
 * @author ycr
 */
public class EnumTranslateSource implements TranslateSource {

    /** 内置源名 */
    public static final String NAME = "enum";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String translate(String key, Object code) {
        if (code == null) {
            return null;
        }
        // 字段本身即枚举实例时直接取描述（无需 key 解析）
        if (code instanceof BaseEnum) {
            return ((BaseEnum<?>) code).getDescription();
        }
        if (key == null || key.isEmpty()) {
            return null;
        }
        Class<?> clazz = resolve(key);
        if (clazz == null || !clazz.isEnum() || !BaseEnum.class.isAssignableFrom(clazz)) {
            return null;
        }
        String target = String.valueOf(code);
        for (Object constant : clazz.getEnumConstants()) {
            BaseEnum<?> e = (BaseEnum<?>) constant;
            if (target.equals(String.valueOf(e.getValue()))) {
                return e.getDescription();
            }
        }
        return null;
    }

    private Class<?> resolve(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}
