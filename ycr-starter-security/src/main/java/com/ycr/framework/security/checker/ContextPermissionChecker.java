package com.ycr.framework.security.checker;

import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Set;

/**
 * 基于 UserContext 快照的权限校验器。
 *
 * @author ycr
 */
public class ContextPermissionChecker implements PermissionChecker {

    @Override
    public boolean hasPermission(String permission) {
        return contains(permissions(), permission);
    }

    @Override
    public boolean hasAnyPermission(Collection<String> permissions) {
        return anyContains(permissions(), permissions);
    }

    @Override
    public boolean hasRole(String role) {
        return contains(roles(), role);
    }

    @Override
    public boolean hasAnyRole(Collection<String> roles) {
        return anyContains(roles(), roles);
    }

    private Set<String> roles() {
        UserContext userContext = UserContextHolder.get();
        return userContext == null ? Set.of() : nullToEmpty(userContext.getRoles());
    }

    private Set<String> permissions() {
        UserContext userContext = UserContextHolder.get();
        return userContext == null ? Set.of() : nullToEmpty(userContext.getPermissions());
    }

    private boolean contains(Set<String> values, String expected) {
        return StringUtils.hasText(expected) && values.contains(expected);
    }

    private boolean anyContains(Set<String> values, Collection<String> expectedValues) {
        if (expectedValues == null || expectedValues.isEmpty()) {
            return false;
        }
        return expectedValues.stream().anyMatch(expected -> contains(values, expected));
    }

    private Set<String> nullToEmpty(Set<String> values) {
        return values == null ? Set.of() : values;
    }
}
