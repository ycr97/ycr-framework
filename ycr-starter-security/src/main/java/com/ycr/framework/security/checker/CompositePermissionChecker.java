package com.ycr.framework.security.checker;

import com.ycr.framework.security.properties.SecurityProperties;

import java.util.Collection;

/**
 * 组合权限校验器。
 *
 * @author ycr
 */
public class CompositePermissionChecker implements PermissionChecker {

    private final ContextPermissionChecker contextPermissionChecker;

    private final RemotePermissionChecker remotePermissionChecker;

    private final SecurityProperties properties;

    public CompositePermissionChecker(ContextPermissionChecker contextPermissionChecker,
                                      RemotePermissionChecker remotePermissionChecker,
                                      SecurityProperties properties) {
        this.contextPermissionChecker = contextPermissionChecker;
        this.remotePermissionChecker = remotePermissionChecker;
        this.properties = properties;
    }

    @Override
    public boolean hasPermission(String permission) {
        if (useRemote(permission)) {
            return remoteHasPermission(permission);
        }
        return contextPermissionChecker.hasPermission(permission);
    }

    @Override
    public boolean hasAnyPermission(Collection<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        return permissions.stream().anyMatch(this::hasPermission);
    }

    @Override
    public boolean hasRole(String role) {
        if (properties.getPermission().getMode() == SecurityProperties.PermissionMode.REMOTE) {
            return remoteHasRole(role);
        }
        return contextPermissionChecker.hasRole(role);
    }

    @Override
    public boolean hasAnyRole(Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream().anyMatch(this::hasRole);
    }

    private boolean useRemote(String permission) {
        SecurityProperties.PermissionMode mode = properties.getPermission().getMode();
        return mode == SecurityProperties.PermissionMode.REMOTE
                || (mode == SecurityProperties.PermissionMode.MIXED
                && properties.getPermission().getSensitivePermissions().contains(permission));
    }

    private boolean remoteHasPermission(String permission) {
        if (remotePermissionChecker == null) {
            return false;
        }
        try {
            return remotePermissionChecker.hasPermission(permission);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private boolean remoteHasRole(String role) {
        if (remotePermissionChecker == null) {
            return false;
        }
        try {
            return remotePermissionChecker.hasRole(role);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
