package com.ycr.framework.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全配置属性
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.security")
public class SecurityProperties {

    /** 是否启用安全拦截，默认关闭 */
    private boolean enabled = false;

    /** 权限校验配置 */
    private Permission permission = new Permission();

    /**
     * 权限校验模式。
     */
    public enum PermissionMode {
        /** 只使用 UserContext 快照 */
        CONTEXT,
        /** 只使用远程实时校验 */
        REMOTE,
        /** 普通权限走上下文快照，敏感权限走远程实时校验 */
        MIXED
    }

    /**
     * 权限校验配置项。
     */
    @Data
    public static class Permission {

        /** 权限校验模式 */
        private PermissionMode mode = PermissionMode.CONTEXT;

        /** mixed 模式下需要远程二次校验的敏感权限 */
        private List<String> sensitivePermissions = new ArrayList<>();
    }
}
