package com.ycr.framework.data.annotation;

import com.ycr.framework.data.enums.QueryType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 查询条件注解 - 标记 Query DTO 字段的查询方式
 *
 * @author ycr
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    /** 查询类型 */
    QueryType type() default QueryType.EQ;

    /** 对应的数据库列名（默认取字段名的下划线形式） */
    String column() default "";
}
