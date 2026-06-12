package com.ycr.framework.ddd.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 聚合根标记注解
 *
 * <p>纯标记，用于代码可读性与架构守护（如 ArchUnit 规则）；不使其成为 Spring Bean。</p>
 *
 * @author ycr
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AggregateRoot {
}
