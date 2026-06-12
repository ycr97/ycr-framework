package com.ycr.framework.translate.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.ycr.framework.translate.annotation.Translate;
import com.ycr.framework.translate.source.TranslateSourceRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * 翻译序列化改造器
 *
 * <p>序列化器构建期扫描 bean 的属性，对标注 {@link Translate} 的编码字段，<b>追加</b>一个同级文本字段写出器
 * （{@link TranslateBeanPropertyWriter}）。原编码字段保留，故输出 JSON 同时含编码与文本。</p>
 *
 * @author ycr
 */
public class TranslateBeanSerializerModifier extends BeanSerializerModifier {

    private final transient TranslateSourceRegistry registry;

    public TranslateBeanSerializerModifier(TranslateSourceRegistry registry) {
        this.registry = registry;
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        List<BeanPropertyWriter> extra = new ArrayList<>();
        for (BeanPropertyWriter writer : beanProperties) {
            AnnotatedMember member = writer.getMember();
            Translate translate = member == null ? null : member.getAnnotation(Translate.class);
            if (translate == null) {
                continue;
            }
            String targetName = translate.targetField().isEmpty()
                    ? writer.getName() + "Name"
                    : translate.targetField();
            String enumClassHint = writer.getType() == null ? null : writer.getType().getRawClass().getName();
            extra.add(new TranslateBeanPropertyWriter(writer, PropertyName.construct(targetName),
                    registry, translate, enumClassHint));
        }
        if (extra.isEmpty()) {
            return beanProperties;
        }
        List<BeanPropertyWriter> result = new ArrayList<>(beanProperties);
        result.addAll(extra);
        return result;
    }
}
