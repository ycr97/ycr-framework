package com.ycr.framework.ddd.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 应用服务标记注解
 *
 * <p>语义标记 + 兼作 Spring {@link Service}：标注的类自动注册为 Bean。{@link #value()} 经
 * {@link AliasFor} 真转发给 {@link Service#value()} 作为 Bean 名称（非死属性）。</p>
 *
 * @author ycr
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface ApplicationService {

    /**
     * Bean 名称，转发给 {@link Service#value()}
     */
    @AliasFor(annotation = Service.class, attribute = "value")
    String value() default "";
}
