package com.ycr.framework.data.permission.annotation;

import java.lang.annotation.*;

/**
 * 忽略数据权限注解
 *
 * @author ycr
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermissionIgnore {
}
