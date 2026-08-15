package com.ycr.framework.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 禁止非法内容校验注解：拒绝包含 XSS 脚本、SQL 注入、爬虫、python 代码等特征的字符串。
 *
 * <p>配合 {@code @Valid} 使用，适用于字段或方法参数。null/空白视为合法（应由 {@code @NotBlank} 等单独管控）。</p>
 *
 * @author ycr
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ForbiddenContentValidator.class)
public @interface ForbiddenContent {

    String message() default "存在非法内容";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
