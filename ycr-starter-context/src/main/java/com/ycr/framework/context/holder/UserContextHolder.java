package com.ycr.framework.context.holder;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.ycr.framework.context.model.UserContext;

/**
 * 用户上下文持有者 - 基于 TransmittableThreadLocal 支持线程池透传
 *
 * @author ycr
 */
public final class UserContextHolder {

    private static final TransmittableThreadLocal<UserContext> CONTEXT = new TransmittableThreadLocal<>();

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
