package com.ycr.framework.security.util;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 安全工具测试
 *
 * <p>StpUtil 依赖运行期上下文，单测中以 Mockito 静态打桩，验证 SecurityUtils 的转发是否正确。</p>
 */
class SecurityUtilsTest {

    @Test
    void 登录态与用户ID转发() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);
            stp.when(StpUtil::getTokenValue).thenReturn("tk-123");

            assertTrue(SecurityUtils.isLogin());
            assertEquals(1001L, SecurityUtils.getUserId());
            assertEquals("tk-123", SecurityUtils.getTokenValue());
        }
    }

    @Test
    void 角色判定转发() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.hasRole("admin")).thenReturn(true);
            stp.when(() -> StpUtil.hasRoleOr("admin", "user")).thenReturn(true);
            stp.when(() -> StpUtil.hasRoleAnd("admin", "user")).thenReturn(false);
            stp.when(StpUtil::getRoleList).thenReturn(List.of("admin"));

            assertTrue(SecurityUtils.hasRole("admin"));
            assertTrue(SecurityUtils.hasRoleOr("admin", "user"));
            assertFalse(SecurityUtils.hasRoleAnd("admin", "user"));
            assertEquals(List.of("admin"), SecurityUtils.getRoleList());
        }
    }

    @Test
    void 权限判定转发() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.hasPermission("user:add")).thenReturn(true);
            stp.when(() -> StpUtil.hasPermissionOr("user:add", "user:edit")).thenReturn(true);
            stp.when(() -> StpUtil.hasPermissionAnd("user:add", "user:edit")).thenReturn(false);
            stp.when(StpUtil::getPermissionList).thenReturn(List.of("user:add"));

            assertTrue(SecurityUtils.hasPermission("user:add"));
            assertTrue(SecurityUtils.hasPermissionOr("user:add", "user:edit"));
            assertFalse(SecurityUtils.hasPermissionAnd("user:add", "user:edit"));
            assertEquals(List.of("user:add"), SecurityUtils.getPermissionList());
        }
    }
}
