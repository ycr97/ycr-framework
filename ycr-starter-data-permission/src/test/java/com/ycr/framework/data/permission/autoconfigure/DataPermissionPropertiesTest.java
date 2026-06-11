package com.ycr.framework.data.permission.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataPermissionPropertiesTest {

    @Test
    void 默认应启用数据权限() {
        DataPermissionProperties properties = new DataPermissionProperties();
        assertTrue(properties.isEnabled(), "数据权限属限制性能力，默认应开启取安全侧");
    }
}
