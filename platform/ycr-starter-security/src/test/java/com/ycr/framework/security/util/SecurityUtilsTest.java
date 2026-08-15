package com.ycr.framework.security.util;

import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.core.util.SpringContextHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 安全工具测试
 *
 * <p>验证 SecurityUtils 基于 ycr UserContext 的读取与权限判断。</p>
 */
class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
        new SpringContextHolder().setApplicationContext(null);
    }

    @Test
    @DisplayName("登录态与用户ID转发")
    void shouldMatchExpectedBehavior001() {
        UserContext userContext = new UserContext();
        userContext.setUserId(1001L);
        UserContextHolder.set(userContext);

        assertTrue(SecurityUtils.isLogin());
        assertEquals(1001L, SecurityUtils.getUserId());
    }

    @Test
    @DisplayName("角色判定转发")
    void shouldMatchExpectedBehavior002() {
        UserContext userContext = new UserContext();
        userContext.setRoles(Set.of("admin"));
        UserContextHolder.set(userContext);

        assertTrue(SecurityUtils.hasRole("admin"));
        assertTrue(SecurityUtils.hasRoleOr("admin", "user"));
        assertFalse(SecurityUtils.hasRoleAnd("admin", "user"));
        assertEquals(List.of("admin"), SecurityUtils.getRoleList());
    }

    @Test
    @DisplayName("权限判定转发")
    void shouldMatchExpectedBehavior003() {
        UserContext userContext = new UserContext();
        userContext.setPermissions(Set.of("user:add"));
        UserContextHolder.set(userContext);

        assertTrue(SecurityUtils.hasPermission("user:add"));
        assertTrue(SecurityUtils.hasPermissionOr("user:add", "user:edit"));
        assertFalse(SecurityUtils.hasPermissionAnd("user:add", "user:edit"));
        assertEquals(List.of("user:add"), SecurityUtils.getPermissionList());
    }

    @Test
    @DisplayName("Spring容器存在但权限检查器获取失败时不得降级")
    void shouldMatchExpectedBehavior004() {
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(com.ycr.framework.security.checker.PermissionChecker.class))
                .thenThrow(new NoSuchBeanDefinitionException(
                        com.ycr.framework.security.checker.PermissionChecker.class));
        new SpringContextHolder().setApplicationContext(context);

        assertThrows(NoSuchBeanDefinitionException.class,
                () -> SecurityUtils.hasPermission("payment:refund"));
    }
}
