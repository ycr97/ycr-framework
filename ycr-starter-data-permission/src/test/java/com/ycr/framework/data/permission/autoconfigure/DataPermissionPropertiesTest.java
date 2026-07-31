package com.ycr.framework.data.permission.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataPermissionPropertiesTest {

    @Test
    void 默认应关闭数据权限() {
        DataPermissionProperties properties = new DataPermissionProperties();
        assertFalse(properties.isEnabled());
    }
}
