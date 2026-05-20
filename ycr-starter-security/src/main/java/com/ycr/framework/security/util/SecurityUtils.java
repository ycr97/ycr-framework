package com.ycr.framework.security.util;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 安全工具类
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
     * 检查是否有指定角色
     */
    public static boolean hasRole(String role) {
        return StpUtil.hasRole(role);
    }
}
