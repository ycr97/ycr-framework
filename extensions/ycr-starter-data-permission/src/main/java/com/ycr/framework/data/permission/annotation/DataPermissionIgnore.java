package com.ycr.framework.data.permission.annotation;

import java.lang.annotation.*;

/**
 * 忽略数据权限注解 - 标注的方法或类在执行查询时跳过数据权限过滤
 *
 * <p>常用于管理员查询、定时任务、内部 RPC 等不应受行级权限约束的场景。
 * 标注在类上时，类内方法可用 {@link DataPermission} 单独重新启用（方法级优先于类级）。
 * 语义与优先级详见 {@code com.ycr.framework.data.permission.aspect.DataPermissionAspect}。</p>
 *
 * @author ycr
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermissionIgnore {
}
