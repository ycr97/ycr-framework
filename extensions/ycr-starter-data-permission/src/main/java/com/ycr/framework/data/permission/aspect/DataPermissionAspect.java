package com.ycr.framework.data.permission.aspect;

import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.ycr.framework.data.permission.annotation.DataPermission;
import com.ycr.framework.data.permission.annotation.DataPermissionIgnore;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;

/**
 * 数据权限注解切面
 *
 * <p>在标注了 {@link DataPermission} / {@link DataPermissionIgnore} 的方法或类上生效，
 * 通过 MyBatis-Plus 的 {@link InterceptorIgnoreHelper} 在方法执行期间动态开关数据权限。
 * 切面只负责"是否忽略"的决策，真正的 SQL 改写仍由数据权限 InnerInterceptor 完成。</p>
 *
 * <p>同一次调用的优先级（高 → 低）：</p>
 * <ol>
 *     <li>方法上 {@link DataPermissionIgnore} → 忽略数据权限</li>
 *     <li>方法上 {@link DataPermission} → 强制启用（可覆盖类级忽略）</li>
 *     <li>类上 {@link DataPermissionIgnore} → 忽略数据权限</li>
 *     <li>以上都没有 → 维持默认（数据权限生效）</li>
 * </ol>
 *
 * @author ycr
 */
@Aspect
public class DataPermissionAspect {

    /**
     * 拦截方法或所在类标注了数据权限相关注解的调用
     */
    @Around(
            "@annotation(com.ycr.framework.data.permission.annotation.DataPermission) "
                    + "|| @annotation(com.ycr.framework.data.permission.annotation.DataPermissionIgnore) "
                    + "|| @within(com.ycr.framework.data.permission.annotation.DataPermission) "
                    + "|| @within(com.ycr.framework.data.permission.annotation.DataPermissionIgnore)"
    )
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 仅当本次调用需要忽略数据权限时，才压入忽略策略，避免对默认场景产生副作用
        if (!shouldIgnore(joinPoint)) {
            return joinPoint.proceed();
        }
        InterceptorIgnoreHelper.handle(IgnoreStrategy.builder().dataPermission(true).build());
        try {
            return joinPoint.proceed();
        } finally {
            // 无论方法是否抛异常，都必须清理线程级忽略策略，防止污染后续请求
            InterceptorIgnoreHelper.clearIgnoreStrategy();
        }
    }

    /**
     * 按优先级判定本次调用是否应忽略数据权限
     */
    private boolean shouldIgnore(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> targetClass = joinPoint.getTarget() != null
                ? joinPoint.getTarget().getClass()
                : method.getDeclaringClass();
        // 取代理背后最具体的方法实现，确保能读到实现类上的注解
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

        if (AnnotatedElementUtils.hasAnnotation(specificMethod, DataPermissionIgnore.class)) {
            return true;
        }
        if (AnnotatedElementUtils.hasAnnotation(specificMethod, DataPermission.class)) {
            // 方法级显式启用，覆盖类级忽略
            return false;
        }
        return AnnotatedElementUtils.hasAnnotation(targetClass, DataPermissionIgnore.class);
    }
}
