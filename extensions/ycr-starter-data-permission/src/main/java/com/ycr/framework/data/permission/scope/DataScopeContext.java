package com.ycr.framework.data.permission.scope;

import com.ycr.framework.data.permission.exception.DataPermissionException;

/**
 * 数据范围请求级缓存：一次请求内最多解析一次，多表/多语句复用；请求结束清理。
 *
 * <p>跨线程传播由框架 TaskDecorator 统一捕获和清理。</p>
 *
 * @author ycr
 */
public final class DataScopeContext {

    private static final ThreadLocal<DataScope> CACHE = new ThreadLocal<>();

    private DataScopeContext() {
    }

    /**
     * 取当前请求的数据范围；首次调用 resolver，之后复用缓存。
     *
     * <p>resolver 抛错视为系统级失败，fail-closed 且 fail-loud 中止本次查询。</p>
     */
    public static DataScope get(DataScopeResolver resolver) {
        DataScope cached = CACHE.get();
        if (cached != null) {
            return cached;
        }
        DataScope resolved;
        try {
            resolved = resolver.resolve();
        } catch (Exception e) {
            throw new DataPermissionException("数据权限范围解析失败，已 fail-closed 中止本次查询", e);
        }
        if (resolved == null) {
            resolved = DataScope.empty();
        }
        CACHE.set(resolved);
        return resolved;
    }

    public static void clear() {
        CACHE.remove();
    }

    static DataScope capture() {
        return CACHE.get();
    }

    static void restore(DataScope scope) {
        if (scope == null) {
            clear();
        } else {
            CACHE.set(scope);
        }
    }
}
