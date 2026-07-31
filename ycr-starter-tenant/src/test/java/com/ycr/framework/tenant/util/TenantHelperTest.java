package com.ycr.framework.tenant.util;

import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("run作用域内忽略_退出后恢复")
    void shouldMatchExpectedBehavior001() {
        assertFalse(TenantHelper.isIgnored());
        TenantHelper.run(() -> assertTrue(TenantHelper.isIgnored()));
        assertFalse(TenantHelper.isIgnored());
    }

    @Test
    @DisplayName("call返回结果且作用域内忽略")
    void shouldMatchExpectedBehavior002() {
        String r = TenantHelper.call(() -> {
            assertTrue(TenantHelper.isIgnored());
            return "ok";
        });
        assertEquals("ok", r);
        assertFalse(TenantHelper.isIgnored());
    }

    @Test
    @DisplayName("嵌套作用域计数正确_内层退出不误关外层")
    void shouldMatchExpectedBehavior003() {
        TenantHelper.run(() -> {
            assertTrue(TenantHelper.isIgnored());
            TenantHelper.run(() -> assertTrue(TenantHelper.isIgnored()));
            // 内层退出后，外层仍在忽略作用域
            assertTrue(TenantHelper.isIgnored());
        });
        assertFalse(TenantHelper.isIgnored());
    }

    @Test
    @DisplayName("异常时也能成对退出")
    void shouldMatchExpectedBehavior004() {
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
