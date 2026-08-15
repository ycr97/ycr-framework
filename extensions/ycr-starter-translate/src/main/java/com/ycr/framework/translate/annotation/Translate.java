package com.ycr.framework.translate.annotation;

import com.ycr.framework.translate.enums.TranslateType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段翻译注解
 *
 * <p>标注在「编码字段」上，JSON 序列化时框架会按 {@link #type()} 选取翻译源，把编码翻译为文本，
 * 并写出到一个<b>同级目标字段</b>（默认为原字段名 + {@code Name}）。原编码字段保留不变。</p>
 *
 * <p>示例：{@code @Translate(type = DICT, key = "user_status") private String status;}
 * 序列化后 JSON 同时含 {@code "status":"1"} 与 {@code "statusName":"启用"}。</p>
 *
 * @author ycr
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Translate {

    /**
     * 翻译类型，决定使用哪个翻译源
     */
    TranslateType type() default TranslateType.DICT;

    /**
     * 分组键，传给翻译源作为第一参数：
     * <ul>
     *     <li>{@code DICT} 时为字典编码（如 {@code user_status}）</li>
     *     <li>{@code ENUM} 时为枚举全限定类名（留空则由字段类型推断）</li>
     *     <li>{@code CUSTOM} 时语义由自定义源自行约定</li>
     * </ul>
     */
    String key() default "";

    /**
     * 自定义源名称，仅 {@code type = CUSTOM} 时生效，对应所注册 {@code TranslateSource} 的 {@code name()}
     */
    String source() default "";

    /**
     * 翻译结果写出的同级字段名；留空则取「原字段名 + Name」
     */
    String targetField() default "";
}
