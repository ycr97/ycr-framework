package com.ycr.framework.data.permission.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解 - 显式启用数据权限过滤
 *
 * <p>数据权限默认对所有查询生效，本注解用于在被 {@link DataPermissionIgnore} 标注的类中，
 * 对个别方法强制重新启用数据权限（方法级优先于类级）。语义与优先级详见
 * {@code com.ycr.framework.data.permission.aspect.DataPermissionAspect}。</p>
 *
 * @author ycr
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {
}
