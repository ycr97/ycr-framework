package com.ycr.framework.protect.mask.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ycr.framework.protect.mask.enums.MaskType;
import com.ycr.framework.protect.mask.jackson.JsonMaskSerializer;
import com.ycr.framework.protect.mask.strategy.MaskStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JSON 字段脱敏注解
 *
 * <p>标在 {@code String} 字段上，序列化时按 {@link #value() 类型} 或 {@link #strategy() 策略} 脱敏输出。
 * 经 Jackson 元注解机制自动生效，无需任何额外配置——只要类路径有本模块即可。</p>
 *
 * <p>示例：{@code @JsonMask(MaskType.MOBILE_PHONE) private String phone;} → {@code 138****5678}。</p>
 *
 * @author ycr
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = JsonMaskSerializer.class)
public @interface JsonMask {

    /**
     * 脱敏类型，默认 {@link MaskType#CUSTOM}（配合 {@link #left()}/{@link #right()} 使用）
     */
    MaskType value() default MaskType.CUSTOM;

    /**
     * 自定义脱敏策略，优先级高于 {@link #value()}；默认 {@link MaskStrategy}（表示不启用，走 value）
     */
    Class<? extends MaskStrategy> strategy() default MaskStrategy.class;

    /**
     * 左侧保留位数（仅 {@code value=CUSTOM} 时生效）
     */
    int left() default 0;

    /**
     * 右侧保留位数（仅 {@code value=CUSTOM} 时生效）
     */
    int right() default 0;

    /**
     * 脱敏符号，默认 {@code *}
     */
    char character() default '*';
}
