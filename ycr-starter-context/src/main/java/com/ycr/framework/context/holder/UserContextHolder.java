package com.ycr.framework.context.holder;

import com.ycr.framework.context.model.UserContext;

/**
 * 用户上下文持有者。跨线程传播由框架 TaskDecorator 负责，避免线程创建时意外继承请求身份。
 *
 * @author ycr
 */
public final class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(UserContext userContext) {
        CONTEXT.set(userContext);
    }

    public static UserContext get() {
        return CONTEXT.get();
    }

    public static Long getUserId() {
        UserContext ctx = get();
        return ctx != null ? ctx.getUserId() : null;
    }

    public static String getUsername() {
        UserContext ctx = get();
        return ctx != null ? ctx.getUsername() : null;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
