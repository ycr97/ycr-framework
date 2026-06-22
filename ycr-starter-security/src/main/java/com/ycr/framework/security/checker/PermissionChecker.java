package com.ycr.framework.security.checker;

import java.util.Collection;

/**
 * 权限校验 SPI。
 *
 * @author ycr
 */
public interface PermissionChecker {

    boolean hasPermission(String permission);

    boolean hasAnyPermission(Collection<String> permissions);

    boolean hasRole(String role);

    boolean hasAnyRole(Collection<String> roles);
}
