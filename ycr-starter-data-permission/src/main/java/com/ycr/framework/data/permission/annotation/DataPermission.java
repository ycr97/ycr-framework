package com.ycr.framework.data.permission.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解 - 标记需要数据权限过滤的方法或类
 *
 * @author ycr
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {
}
