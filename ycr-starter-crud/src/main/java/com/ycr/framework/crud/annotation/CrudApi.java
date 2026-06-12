package com.ycr.framework.crud.annotation;

import com.ycr.framework.crud.enums.Api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CRUD 端点治理注解
 *
 * <p>标在继承 {@code AbstractCrudController} 的子类上，{@link #disable()} 列出的 {@link Api} 对应端点
 * 将不被注册（请求返回 404）。仅治理基类内置的 CRUD 端点。</p>
 *
 * @author ycr
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CrudApi {

    /**
     * 需关闭的端点
     */
    Api[] disable() default {};
}
