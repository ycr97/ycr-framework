package com.ycr.framework.log.annotation;

import com.ycr.framework.log.enums.Include;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 *
 * <p>标注在 Controller 方法或类上，由 {@code LogAspect} 自动采集并交给 {@code LogHandler} 处理。
 * 方法级注解优先于类级：方法未指定 {@link #module()} 时回退取类级 {@code @Log} 的模块。</p>
 *
 * @author ycr
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /**
     * 日志描述，默认空时回退取被注解方法名
     */
    String value() default "";

    /**
     * 所属模块
     */
    String module() default "";

    /**
     * 在全局采集项基础上额外包含的信息
     */
    Include[] includes() default {};

    /**
     * 在全局采集项基础上排除的信息
     */
    Include[] excludes() default {};

    /**
     * 是否忽略该方法的日志记录，默认 false
     */
    boolean ignore() default false;
}
