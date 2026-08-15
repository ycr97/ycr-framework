package com.ycr.framework.context.holder;

import com.ycr.framework.context.model.AppContext;

/**
 * 应用上下文持有者
 *
 * @author ycr
 */
public final class AppContextHolder {

    private static final ThreadLocal<AppContext> CONTEXT = new ThreadLocal<>();

    private AppContextHolder() {
    }

    public static void set(AppContext appContext) {
        CONTEXT.set(appContext);
    }

    public static AppContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
