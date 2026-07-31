package com.ycr.framework.security.checker;

import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.security.properties.SecurityProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CompositePermissionChecker 测试。
 *
 * @author ycr
 */
class CompositePermissionCheckerTest {

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("mixed模式敏感权限走远程校验")
    void shouldMatchExpectedBehavior001() {
        SecurityProperties properties = new SecurityProperties();
        properties.getPermission().setMode(SecurityProperties.PermissionMode.MIXED);
        properties.getPermission().setSensitivePermissions(java.util.List.of("payment:refund"));

        UserContext userContext = new UserContext();
        userContext.setPermissions(Set.of("payment:refund", "order:create"));
        UserContextHolder.set(userContext);

        CompositePermissionChecker checker = new CompositePermissionChecker(
                new ContextPermissionChecker(),
                remotePermissionChecker(true),
                properties);

        assertTrue(checker.hasPermission("payment:refund"));
        assertTrue(checker.hasPermission("order:create"));
    }

    @Test
    @DisplayName("远程校验异常应failClosed")
    void shouldMatchExpectedBehavior002() {
        SecurityProperties properties = new SecurityProperties();
        properties.getPermission().setMode(SecurityProperties.PermissionMode.MIXED);
        properties.getPermission().setSensitivePermissions(java.util.List.of("payment:refund"));

        CompositePermissionChecker checker = new CompositePermissionChecker(
                new ContextPermissionChecker(),
                remotePermissionCheckerThrows(),
                properties);

        assertFalse(checker.hasPermission("payment:refund"));
    }

    private RemotePermissionChecker remotePermissionChecker(boolean result) {
        return new RemotePermissionChecker() {
            @Override
            public boolean hasPermission(String permission) {
                return result;
            }

            @Override
            public boolean hasAnyPermission(java.util.Collection<String> permissions) {
                return result;
            }

            @Override
            public boolean hasRole(String role) {
                return result;
            }

            @Override
            public boolean hasAnyRole(java.util.Collection<String> roles) {
                return result;
            }
        };
    }

    private RemotePermissionChecker remotePermissionCheckerThrows() {
        return new RemotePermissionChecker() {
            @Override
            public boolean hasPermission(String permission) {
                throw new RuntimeException("remote down");
            }

            @Override
            public boolean hasAnyPermission(java.util.Collection<String> permissions) {
                throw new RuntimeException("remote down");
            }

            @Override
            public boolean hasRole(String role) {
                throw new RuntimeException("remote down");
            }

            @Override
            public boolean hasAnyRole(java.util.Collection<String> roles) {
                throw new RuntimeException("remote down");
            }
        };
    }
}
