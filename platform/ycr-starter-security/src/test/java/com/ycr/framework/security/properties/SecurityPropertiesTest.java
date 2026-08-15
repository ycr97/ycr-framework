package com.ycr.framework.security.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityPropertiesTest {

    @Test
    @DisplayName("默认应关闭鉴权切面并使用上下文权限模式")
    void shouldMatchExpectedBehavior001() {
        SecurityProperties properties = new SecurityProperties();
        assertFalse(properties.isEnabled());
        assertEquals(SecurityProperties.PermissionMode.CONTEXT, properties.getPermission().getMode());
        assertTrue(properties.getPermission().getSensitivePermissions().isEmpty());
    }
}
