package com.ycr.framework.security.checker;

import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ContextPermissionChecker 测试。
 *
 * @author ycr
 */
class ContextPermissionCheckerTest {

    private final ContextPermissionChecker checker = new ContextPermissionChecker();

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("应从UserContext判断角色和权限")
    void shouldMatchExpectedBehavior001() {
        UserContext userContext = new UserContext();
        userContext.setRoles(Set.of("admin"));
        userContext.setPermissions(Set.of("order:create"));
        UserContextHolder.set(userContext);

        assertTrue(checker.hasRole("admin"));
        assertTrue(checker.hasAnyRole(List.of("user", "admin")));
        assertTrue(checker.hasPermission("order:create"));
        assertTrue(checker.hasAnyPermission(List.of("order:update", "order:create")));
        assertFalse(checker.hasPermission("order:delete"));
    }

    @Test
    @DisplayName("无用户上下文时应返回false")
    void shouldMatchExpectedBehavior002() {
        assertFalse(checker.hasRole("admin"));
        assertFalse(checker.hasPermission("order:create"));
    }
}
