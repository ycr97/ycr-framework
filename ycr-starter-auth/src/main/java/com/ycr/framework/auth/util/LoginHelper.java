package com.ycr.framework.auth.util;

import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;

/**
 * 登录辅助工具类
 * 封装 SaToken 登录操作和 UserContext 联动
 *
 * @author ycr
 */
public final class LoginHelper {

    private LoginHelper() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    /**
     * 设置当前用户上下文
     */
    public static void setUserContext(UserContext userContext) {
        UserContextHolder.set(userContext);
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getUserId() {
        return UserContextHolder.getUserId();
    }

    /**
     * 获取当前登录用户名
     */
    public static String getUsername() {
        return UserContextHolder.getUsername();
    }

    /**
     * 获取当前用户上下文
     */
    public static UserContext getUserContext() {
        return UserContextHolder.get();
    }

    /**
     * 清除当前用户上下文
     */
    public static void clearContext() {
        UserContextHolder.clear();
    }
}
