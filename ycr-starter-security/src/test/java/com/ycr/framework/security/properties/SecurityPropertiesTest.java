package com.ycr.framework.security.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityPropertiesTest {

    @Test
    void 默认放行路径应包含常用静态资源() {
        SecurityProperties properties = new SecurityProperties();
        assertFalse(properties.isEnabled());
        assertNotNull(properties.getExcludePaths());
        assertTrue(properties.getExcludePaths().contains("/doc.html"));
        assertTrue(properties.getExcludePaths().contains("/swagger-resources/**"));
        assertEquals(SecurityProperties.PermissionMode.CONTEXT, properties.getPermission().getMode());
        assertTrue(properties.getPermission().getSensitivePermissions().isEmpty());
    }
}
