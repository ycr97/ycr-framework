package com.ycr.framework.security.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityPropertiesTest {

    @Test
    void 默认应关闭鉴权切面并使用上下文权限模式() {
        SecurityProperties properties = new SecurityProperties();
        assertFalse(properties.isEnabled());
        assertEquals(SecurityProperties.PermissionMode.CONTEXT, properties.getPermission().getMode());
        assertTrue(properties.getPermission().getSensitivePermissions().isEmpty());
    }
}
