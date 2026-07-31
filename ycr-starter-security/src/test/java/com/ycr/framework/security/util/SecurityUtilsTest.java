package com.ycr.framework.security.util;

import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全工具测试
 *
 * <p>验证 SecurityUtils 基于 ycr UserContext 的读取与权限判断。</p>
 */
class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void 登录态与用户ID转发() {
        UserContext userContext = new UserContext();
        userContext.setUserId(1001L);
        UserContextHolder.set(userContext);

        assertTrue(SecurityUtils.isLogin());
        assertEquals(1001L, SecurityUtils.getUserId());
    }

    @Test
    void 角色判定转发() {
        UserContext userContext = new UserContext();
        userContext.setRoles(Set.of("admin"));
        UserContextHolder.set(userContext);

        assertTrue(SecurityUtils.hasRole("admin"));
        assertTrue(SecurityUtils.hasRoleOr("admin", "user"));
        assertFalse(SecurityUtils.hasRoleAnd("admin", "user"));
        assertEquals(List.of("admin"), SecurityUtils.getRoleList());
    }

    @Test
    void 权限判定转发() {
        UserContext userContext = new UserContext();
        userContext.setPermissions(Set.of("user:add"));
        UserContextHolder.set(userContext);

        assertTrue(SecurityUtils.hasPermission("user:add"));
        assertTrue(SecurityUtils.hasPermissionOr("user:add", "user:edit"));
        assertFalse(SecurityUtils.hasPermissionAnd("user:add", "user:edit"));
        assertEquals(List.of("user:add"), SecurityUtils.getPermissionList());
    }
}
