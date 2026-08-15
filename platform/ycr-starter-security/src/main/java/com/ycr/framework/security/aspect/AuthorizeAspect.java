package com.ycr.framework.security.aspect;

import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.security.annotation.RequireAnyPermission;
import com.ycr.framework.security.annotation.RequireAnyRole;
import com.ycr.framework.security.annotation.RequireLogin;
import com.ycr.framework.security.annotation.RequirePermission;
import com.ycr.framework.security.annotation.RequireRole;
import com.ycr.framework.security.checker.PermissionChecker;
import com.ycr.framework.security.exception.AuthException;
import com.ycr.framework.security.exception.ForbiddenException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

/**
 * ycr 鉴权注解切面。
 *
 * @author ycr
 */
@Aspect
public class AuthorizeAspect {

    private final PermissionChecker permissionChecker;

    public AuthorizeAspect(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }

    @Around("@within(com.ycr.framework.security.annotation.RequireLogin)"
            + " || @annotation(com.ycr.framework.security.annotation.RequireLogin)"
            + " || @within(com.ycr.framework.security.annotation.RequireRole)"
            + " || @annotation(com.ycr.framework.security.annotation.RequireRole)"
            + " || @within(com.ycr.framework.security.annotation.RequireAnyRole)"
            + " || @annotation(com.ycr.framework.security.annotation.RequireAnyRole)"
            + " || @within(com.ycr.framework.security.annotation.RequirePermission)"
            + " || @annotation(com.ycr.framework.security.annotation.RequirePermission)"
            + " || @within(com.ycr.framework.security.annotation.RequireAnyPermission)"
            + " || @annotation(com.ycr.framework.security.annotation.RequireAnyPermission)")
    public Object authorize(ProceedingJoinPoint joinPoint) throws Throwable {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        Method method = AopUtils.getMostSpecificMethod(((MethodSignature) joinPoint.getSignature()).getMethod(),
                targetClass);

        checkLogin(method, targetClass);
        checkRoles(method, targetClass);
        checkPermissions(method, targetClass);

        return joinPoint.proceed();
    }

    private void checkLogin(Method method, Class<?> targetClass) {
        if (hasAnnotation(method, targetClass, RequireLogin.class)
                || hasRoleAnnotation(method, targetClass)
                || hasPermissionAnnotation(method, targetClass)) {
            requireLogin();
        }
    }

    private void checkRoles(Method method, Class<?> targetClass) {
        RequireRole methodRole = AnnotationUtils.findAnnotation(method, RequireRole.class);
        RequireAnyRole methodAnyRole = AnnotationUtils.findAnnotation(method, RequireAnyRole.class);
        if (methodRole != null || methodAnyRole != null) {
            checkMethodRoles(methodRole, methodAnyRole);
            return;
        }
        if (hasMethodAuthorization(method)) {
            return;
        }

        RequireRole classRole = AnnotationUtils.findAnnotation(targetClass, RequireRole.class);
        RequireAnyRole classAnyRole = AnnotationUtils.findAnnotation(targetClass, RequireAnyRole.class);
        checkMethodRoles(classRole, classAnyRole);
    }

    private void checkMethodRoles(RequireRole role, RequireAnyRole anyRole) {
        if (role != null && !permissionChecker.hasRole(role.value())) {
            throw new ForbiddenException("缺少角色：" + role.value());
        }
        if (anyRole != null && !permissionChecker.hasAnyRole(Arrays.asList(anyRole.value()))) {
            throw new ForbiddenException("缺少任一角色：" + String.join(",", anyRole.value()));
        }
    }

    private void checkPermissions(Method method, Class<?> targetClass) {
        RequirePermission methodPermission = AnnotationUtils.findAnnotation(method, RequirePermission.class);
        RequireAnyPermission methodAnyPermission = AnnotationUtils.findAnnotation(method, RequireAnyPermission.class);
        if (methodPermission != null || methodAnyPermission != null) {
            checkMethodPermissions(methodPermission, methodAnyPermission);
            return;
        }
        if (hasMethodAuthorization(method)) {
            return;
        }

        RequirePermission classPermission = AnnotationUtils.findAnnotation(targetClass, RequirePermission.class);
        RequireAnyPermission classAnyPermission = AnnotationUtils.findAnnotation(targetClass, RequireAnyPermission.class);
        checkMethodPermissions(classPermission, classAnyPermission);
    }

    private void checkMethodPermissions(RequirePermission permission, RequireAnyPermission anyPermission) {
        if (permission != null && !permissionChecker.hasPermission(permission.value())) {
            throw new ForbiddenException("缺少权限：" + permission.value());
        }
        if (anyPermission != null && !permissionChecker.hasAnyPermission(Arrays.asList(anyPermission.value()))) {
            throw new ForbiddenException("缺少任一权限：" + String.join(",", anyPermission.value()));
        }
    }

    private boolean hasRoleAnnotation(Method method, Class<?> targetClass) {
        return hasAnyAnnotation(method, targetClass, RequireRole.class, RequireAnyRole.class);
    }

    private boolean hasPermissionAnnotation(Method method, Class<?> targetClass) {
        return hasAnyAnnotation(method, targetClass, RequirePermission.class, RequireAnyPermission.class);
    }

    private boolean hasMethodAuthorization(Method method) {
        return AnnotationUtils.findAnnotation(method, RequireLogin.class) != null
                || AnnotationUtils.findAnnotation(method, RequireRole.class) != null
                || AnnotationUtils.findAnnotation(method, RequireAnyRole.class) != null
                || AnnotationUtils.findAnnotation(method, RequirePermission.class) != null
                || AnnotationUtils.findAnnotation(method, RequireAnyPermission.class) != null;
    }

    @SafeVarargs
    private boolean hasAnyAnnotation(Method method, Class<?> targetClass,
                                     Class<? extends java.lang.annotation.Annotation>... annotationTypes) {
        return Arrays.stream(annotationTypes).anyMatch(type -> hasAnnotation(method, targetClass, type));
    }

    private boolean hasAnnotation(Method method, Class<?> targetClass,
                                  Class<? extends java.lang.annotation.Annotation> annotationType) {
        return AnnotationUtils.findAnnotation(method, annotationType) != null
                || AnnotationUtils.findAnnotation(targetClass, annotationType) != null;
    }

    private void requireLogin() {
        if (UserContextHolder.get() == null) {
            throw new AuthException();
        }
    }
}
