package com.ycr.framework.security.checker;

/**
 * 远程权限校验 SPI。
 *
 * <p>业务可通过 Bean 注入 auth-center/user-center 的实时校验实现。</p>
 *
 * @author ycr
 */
public interface RemotePermissionChecker extends PermissionChecker {
}
