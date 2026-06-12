package com.ycr.framework.tenant.handler;

import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.model.TenantContext;
import com.ycr.framework.tenant.autoconfigure.TenantProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * YcrTenantLineHandler 行为测试
 *
 * @author ycr
 */
class YcrTenantLineHandlerTest {

    private final TenantProperties properties = new TenantProperties();
    private final YcrTenantLineHandler handler = new YcrTenantLineHandler(properties);

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void 有租户上下文应注入租户ID() {
        TenantContext ctx = new TenantContext();
        ctx.setTenantId(100L);
        TenantContextHolder.set(ctx);

        assertEquals("100", handler.getTenantId().toString());
    }

    @Test
    void 无租户上下文应抛异常_failClosed() {
        assertThrows(IllegalStateException.class, handler::getTenantId);
    }

    @Test
    void 应返回配置的租户列名() {
        properties.setTenantIdColumn("tenant_no");
        assertEquals("tenant_no", handler.getTenantIdColumn());
    }

    @Test
    void 忽略表判断应按配置() {
        properties.setIgnoreTables(List.of("sys_config", "sys_dict"));

        assertTrue(handler.ignoreTable("sys_config"));
        assertFalse(handler.ignoreTable("biz_order"));
    }
}
