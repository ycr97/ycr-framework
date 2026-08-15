package com.ycr.framework.translate.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.ycr.framework.translate.annotation.Translate;
import com.ycr.framework.translate.enums.TranslateType;
import com.ycr.framework.translate.source.TranslateSource;
import com.ycr.framework.translate.source.TranslateSourceRegistry;

/**
 * 翻译用属性写出器
 *
 * <p>由 {@link TranslateBeanSerializerModifier} 基于原编码字段的写出器「改名复制」而来：沿用原字段的取值访问器，
 * 但写出名改为目标字段名。序列化时取原编码值、查翻译源、写出文本（无法翻译则写 null）。</p>
 *
 * @author ycr
 */
public class TranslateBeanPropertyWriter extends BeanPropertyWriter {

    private final transient TranslateSourceRegistry registry;
    private final transient Translate translate;
    /** 字段声明类型全限定名，供 ENUM 类型在 key 留空时推断枚举类 */
    private final String enumClassHint;

    protected TranslateBeanPropertyWriter(BeanPropertyWriter base, PropertyName targetName,
                                          TranslateSourceRegistry registry, Translate translate,
                                          String enumClassHint) {
        super(base, targetName);
        this.registry = registry;
        this.translate = translate;
        this.enumClassHint = enumClassHint;
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
        // get(bean) 复用 base 的访问器，读到的是「原编码字段」的值
        Object code = get(bean);
        String text = resolveText(code);
        if (text == null) {
            gen.writeNullField(getName());
        } else {
            gen.writeStringField(getName(), text);
        }
    }

    private String resolveText(Object code) {
        String sourceName = translate.type() == TranslateType.CUSTOM
                ? translate.source()
                : translate.type().getSourceName();
        TranslateSource source = registry.get(sourceName);
        if (source == null) {
            return null;
        }
        String key = translate.key();
        // ENUM 且未显式给 key 时，用字段声明类型作为枚举类名
        if (translate.type() == TranslateType.ENUM && (key == null || key.isEmpty())) {
            key = enumClassHint;
        }
        return source.translate(key, code);
    }
}
