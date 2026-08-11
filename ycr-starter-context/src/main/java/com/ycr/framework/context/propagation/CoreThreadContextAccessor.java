package com.ycr.framework.context.propagation;

import com.ycr.framework.context.holder.AppContextHolder;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.AppContext;
import com.ycr.framework.context.model.TenantContext;
import com.ycr.framework.context.model.UserContext;
import org.slf4j.MDC;

import java.util.Map;

/** 用户、租户、应用与 MDC 上下文访问器。 */
public class CoreThreadContextAccessor implements ThreadContextAccessor {

    @Override
    public Object capture() {
        return new Snapshot(UserContextHolder.get(), TenantContextHolder.get(), AppContextHolder.get(),
                MDC.getCopyOfContextMap());
    }

    @Override
    public void restore(Object captured) {
        if (captured == null) {
            clear();
            return;
        }
        Snapshot snapshot = (Snapshot) captured;
        restoreUser(snapshot.userContext());
        restoreTenant(snapshot.tenantContext());
        restoreApp(snapshot.appContext());
        MDC.clear();
        if (snapshot.mdc() != null && !snapshot.mdc().isEmpty()) {
            MDC.setContextMap(snapshot.mdc());
        }
    }

    private void clear() {
        UserContextHolder.clear();
        TenantContextHolder.clear();
        AppContextHolder.clear();
        MDC.clear();
    }

    private void restoreUser(UserContext context) {
        if (context == null) {
            UserContextHolder.clear();
        } else {
            UserContextHolder.set(context);
        }
    }

    private void restoreTenant(TenantContext context) {
        if (context == null) {
            TenantContextHolder.clear();
        } else {
            TenantContextHolder.set(context);
        }
    }

    private void restoreApp(AppContext context) {
        if (context == null) {
            AppContextHolder.clear();
        } else {
            AppContextHolder.set(context);
        }
    }

    private record Snapshot(UserContext userContext, TenantContext tenantContext, AppContext appContext,
                            Map<String, String> mdc) {
    }
}
