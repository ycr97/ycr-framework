package com.ycr.framework.security.util;

import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.core.util.ServletUtils;
import com.ycr.framework.core.util.SpringContextHolder;
import com.ycr.framework.security.checker.ContextPermissionChecker;
import com.ycr.framework.security.checker.PermissionChecker;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 安全工具类
 *
 * <p>基于 ycr 用户上下文和 {@link PermissionChecker} 提供登录态、角色、权限的判定方法。</p>
 *
 * @author ycr
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 判断当前是否已登录
     */
    public static boolean isLogin() {
        return UserContextHolder.get() != null;
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getUserId() {
        return UserContextHolder.getUserId();
    }

    /**
     * 获取当前请求的 Authorization 原始值
     */
    public static String getTokenValue() {
        return ServletUtils.getRequest()
                .map(request -> request.getHeader(HttpHeaders.AUTHORIZATION))
                .orElse(null);
    }

    /**
     * 检查是否有指定权限
     */
    public static boolean hasPermission(String permission) {
        return checker().hasPermission(permission);
    }

    /**
     * 是否拥有以下任一权限
     */
    public static boolean hasPermissionOr(String... permissions) {
        return checker().hasAnyPermission(List.of(permissions));
    }

    /**
     * 是否同时拥有以下全部权限
     */
    public static boolean hasPermissionAnd(String... permissions) {
        return allMatch(List.of(permissions), SecurityUtils::hasPermission);
    }

    /**
     * 检查是否有指定角色
     */
    public static boolean hasRole(String role) {
        return checker().hasRole(role);
    }

    /**
     * 是否拥有以下任一角色
     */
    public static boolean hasRoleOr(String... roles) {
        return checker().hasAnyRole(List.of(roles));
    }

    /**
     * 是否同时拥有以下全部角色
     */
    public static boolean hasRoleAnd(String... roles) {
        return allMatch(List.of(roles), SecurityUtils::hasRole);
    }

    /**
     * 获取当前登录用户的角色列表
     */
    public static List<String> getRoleList() {
        UserContext userContext = UserContextHolder.get();
        return toList(userContext == null ? Set.of() : userContext.getRoles());
    }

    /**
     * 获取当前登录用户的权限列表
     */
    public static List<String> getPermissionList() {
        UserContext userContext = UserContextHolder.get();
        return toList(userContext == null ? Set.of() : userContext.getPermissions());
    }

    private static PermissionChecker checker() {
        try {
            return SpringContextHolder.getBean(PermissionChecker.class);
        } catch (RuntimeException e) {
            return new ContextPermissionChecker();
        }
    }

    private static boolean allMatch(Collection<String> values, java.util.function.Predicate<String> predicate) {
        return values != null && !values.isEmpty() && values.stream().allMatch(predicate);
    }

    private static List<String> toList(Set<String> values) {
        return values == null ? List.of() : new ArrayList<>(values);
    }
}
