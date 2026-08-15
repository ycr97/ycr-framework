package com.ycr.framework.data.permission.autoconfigure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataPermissionPropertiesTest {

    @Test
    @DisplayName("默认应关闭数据权限")
    void shouldMatchExpectedBehavior001() {
        DataPermissionProperties properties = new DataPermissionProperties();
        assertFalse(properties.isEnabled());
    }
}
