package com.ycr.framework.tenant.util;

import java.util.function.Supplier;

/**
 * 多租户动态旁路工具
 *
 * <p>为定时任务、登录前流程、系统级跨租户操作等提供「在作用域内临时关闭租户隔离」的逃生口，
 * 区别于 {@code TenantProperties.ignoreTables} 的静态表级忽略。作用域内 {@code YcrTenantLineHandler.ignoreTable}
 * 返回 {@code true}，MyBatis-Plus 不再注入租户条件、也不会触发 fail-closed。</p>
 *
 * <p>基于计数器 {@link ThreadLocal}，支持嵌套；务必在 {@code finally} 中成对退出（{@link #run}/{@link #call} 已保证）。</p>
 *
 * @author ycr
 */
public final class TenantHelper {

    private static final ThreadLocal<Integer> IGNORE_DEPTH = ThreadLocal.withInitial(() -> 0);

    private TenantHelper() {
    }

    /**
     * 当前线程是否处于忽略租户作用域内
     */
    public static boolean isIgnored() {
        return IGNORE_DEPTH.get() > 0;
    }

    /**
     * 在忽略租户隔离的作用域内执行无返回值逻辑
     */
    public static void run(Runnable runnable) {
        enter();
        try {
            runnable.run();
        } finally {
            exit();
        }
    }

    /**
     * 在忽略租户隔离的作用域内执行并返回结果
     */
    public static <T> T call(Supplier<T> supplier) {
        enter();
        try {
            return supplier.get();
        } finally {
            exit();
        }
    }

    private static void enter() {
        IGNORE_DEPTH.set(IGNORE_DEPTH.get() + 1);
    }

    private static void exit() {
        int depth = IGNORE_DEPTH.get() - 1;
        if (depth <= 0) {
            IGNORE_DEPTH.remove();
        } else {
            IGNORE_DEPTH.set(depth);
        }
    }
}
