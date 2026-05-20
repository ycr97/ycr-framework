package com.ycr.framework.data.permission.handler;

import com.ycr.framework.data.permission.rule.DataPermissionRule;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DataPermissionHandlerTest {

    @Test
    void 规则接口应包含必要方法() {
        DataPermissionRule rule = new DataPermissionRule() {
            @Override
            public String getTableName() { return "sys_user"; }

            @Override
            public String getSqlSegment() { return "dept_id = 1"; }

            @Override
            public boolean isApplicable() { return true; }
        };

        assertEquals("sys_user", rule.getTableName());
        assertEquals("dept_id = 1", rule.getSqlSegment());
        assertTrue(rule.isApplicable());
    }

    @Test
    void Handler应管理规则列表() {
        DataPermissionHandler handler = new DataPermissionHandler();

        DataPermissionRule rule = new DataPermissionRule() {
            @Override
            public String getTableName() { return "sys_user"; }
            @Override
            public String getSqlSegment() { return "dept_id IN (1, 2)"; }
            @Override
            public boolean isApplicable() { return true; }
        };

        handler.addRule(rule);
        assertEquals(1, handler.getRules().size());
        assertEquals("sys_user", handler.getRules().get(0).getTableName());
    }
}
