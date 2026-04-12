package com.ycr.framework.context.holder;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.ycr.framework.context.model.TenantContext;

/**
 * 租户上下文持有者
 *
 * @author ycr
 */
public final class TenantContextHolder {

    private static final TransmittableThreadLocal<TenantContext> CONTEXT = new TransmittableThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void set(TenantContext tenantContext) {
        CONTEXT.set(tenantContext);
    }

    public static TenantContext get() {
        return CONTEXT.get();
    }

    public static Long getTenantId() {
        TenantContext ctx = get();
        return ctx != null ? ctx.getTenantId() : null;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
