package com.ycr.framework.security.util;

import cn.dev33.satoken.stp.StpUtil;

import java.util.List;

/**
 * 安全工具类
 *
 * <p>对 SaToken {@link StpUtil} 鉴权能力的便捷封装，提供登录态、角色、权限的判定方法。</p>
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
        return StpUtil.isLogin();
    }

    /**
     * 获取当前登录用户ID（未登录将抛出 SaToken 未登录异常）
     */
    public static Long getUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前登录用户的 Token 值
     */
    public static String getTokenValue() {
        return StpUtil.getTokenValue();
    }

    /**
     * 检查是否有指定权限
     */
    public static boolean hasPermission(String permission) {
        return StpUtil.hasPermission(permission);
    }

    /**
     * 是否拥有以下任一权限
     */
    public static boolean hasPermissionOr(String... permissions) {
        return StpUtil.hasPermissionOr(permissions);
    }

    /**
     * 是否同时拥有以下全部权限
     */
    public static boolean hasPermissionAnd(String... permissions) {
        return StpUtil.hasPermissionAnd(permissions);
    }

    /**
     * 检查是否有指定角色
     */
    public static boolean hasRole(String role) {
        return StpUtil.hasRole(role);
    }

    /**
     * 是否拥有以下任一角色
     */
    public static boolean hasRoleOr(String... roles) {
        return StpUtil.hasRoleOr(roles);
    }

    /**
     * 是否同时拥有以下全部角色
     */
    public static boolean hasRoleAnd(String... roles) {
        return StpUtil.hasRoleAnd(roles);
    }

    /**
     * 获取当前登录用户的角色列表
     */
    public static List<String> getRoleList() {
        return StpUtil.getRoleList();
    }

    /**
     * 获取当前登录用户的权限列表
     */
    public static List<String> getPermissionList() {
        return StpUtil.getPermissionList();
    }
}
