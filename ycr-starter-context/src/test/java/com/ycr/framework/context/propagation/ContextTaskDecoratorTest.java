package com.ycr.framework.context.propagation;

import com.ycr.framework.context.holder.AppContextHolder;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.AppContext;
import com.ycr.framework.context.model.TenantContext;
import com.ycr.framework.context.model.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ContextTaskDecoratorTest {

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
        TenantContextHolder.clear();
        AppContextHolder.clear();
        MDC.clear();
    }

    @Test
    @DisplayName("应按任务传播上下文并在执行后清理")
    void shouldPropagateAndClearContextPerTask() throws Exception {
        UserContext user = new UserContext();
        user.setUserId(1001L);
        TenantContext tenant = new TenantContext();
        tenant.setTenantId(2001L);
        AppContext app = new AppContext();
        app.setAppId("order-service");
        UserContextHolder.set(user);
        TenantContextHolder.set(tenant);
        AppContextHolder.set(app);
        MDC.put("traceId", "trace-1");

        ContextTaskDecorator decorator = new ContextTaskDecorator(List.of(new CoreThreadContextAccessor()));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicReference<ObservedContext> observed = new AtomicReference<>();
        try {
            executor.submit(decorator.decorate(() -> observed.set(observe())))
                    .get(5, TimeUnit.SECONDS);

            assertThat(observed.get()).isEqualTo(new ObservedContext(1001L, 2001L, "order-service", "trace-1"));
            assertThat(executor.submit(this::observe).get(5, TimeUnit.SECONDS))
                    .isEqualTo(new ObservedContext(null, null, null, null));
        } finally {
            executor.shutdownNow();
        }
    }

    private ObservedContext observe() {
        return new ObservedContext(UserContextHolder.getUserId(), TenantContextHolder.getTenantId(),
                AppContextHolder.get() == null ? null : AppContextHolder.get().getAppId(), MDC.get("traceId"));
    }

    private record ObservedContext(Long userId, Long tenantId, String appId, String traceId) {
    }
}
