package com.ycr.framework.tenant.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TenantHelper 动态旁路作用域测试
 *
 * @author ycr
 */
class TenantHelperTest {

    @Test
    void run作用域内忽略_退出后恢复() {
        assertFalse(TenantHelper.isIgnored());
        TenantHelper.run(() -> assertTrue(TenantHelper.isIgnored()));
        assertFalse(TenantHelper.isIgnored());
    }

    @Test
    void call返回结果且作用域内忽略() {
        String r = TenantHelper.call(() -> {
            assertTrue(TenantHelper.isIgnored());
            return "ok";
        });
        assertEquals("ok", r);
        assertFalse(TenantHelper.isIgnored());
    }

    @Test
    void 嵌套作用域计数正确_内层退出不误关外层() {
        TenantHelper.run(() -> {
            assertTrue(TenantHelper.isIgnored());
            TenantHelper.run(() -> assertTrue(TenantHelper.isIgnored()));
            // 内层退出后，外层仍在忽略作用域
            assertTrue(TenantHelper.isIgnored());
        });
        assertFalse(TenantHelper.isIgnored());
    }

    @Test
    void 异常时也能成对退出() {
        try {
            TenantHelper.run(() -> {
                throw new RuntimeException("boom");
            });
        } catch (RuntimeException ignored) {
            // 预期
        }
        assertFalse(TenantHelper.isIgnored());
    }
}
