package com.ycr.framework.data.permission.handler;

import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.ycr.framework.data.permission.rule.DataPermissionRule;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据权限 SQL 处理器与 MyBatis-Plus 拦截器集成测试
 *
 * <p>{@code 适配器*} 用例验证表名匹配与 SQL 片段解析；{@code SQL改写*} 用例直接驱动 MyBatis-Plus
 * 的 {@link DataPermissionInterceptor#parserSingle(String, Object)} 验证真实的 WHERE 条件改写结果。</p>
 */
class DataPermissionSqlHandlerTest {

    /** 构造一条简单规则 */
    private DataPermissionRule rule(String table, String segment, boolean applicable) {
        return new DataPermissionRule() {
            @Override
            public String getTableName() {
                return table;
            }

            @Override
            public String getSqlSegment() {
                return segment;
            }

            @Override
            public boolean isApplicable() {
                return applicable;
            }
        };
    }

    private DataPermissionSqlHandler handlerWith(DataPermissionRule... rules) {
        DataPermissionHandler registry = new DataPermissionHandler();
        for (DataPermissionRule r : rules) {
            registry.addRule(r);
        }
        return new DataPermissionSqlHandler(registry);
    }

    @Test
    void 适配器_命中表应返回解析后的条件表达式() {
        DataPermissionSqlHandler handler = handlerWith(rule("sys_user", "dept_id IN (1, 2, 3)", true));

        Expression expression = handler.getSqlSegment(new Table("sys_user"), null, "anyMsId");

        assertNotNull(expression);
        assertEquals("dept_id IN (1, 2, 3)", expression.toString());
    }

    @Test
    void 适配器_表名带反引号也应匹配() {
        DataPermissionSqlHandler handler = handlerWith(rule("sys_user", "dept_id = 1", true));

        Expression expression = handler.getSqlSegment(new Table("`sys_user`"), null, "anyMsId");

        assertNotNull(expression);
        assertEquals("dept_id = 1", expression.toString());
    }

    @Test
    void 适配器_未命中表应返回null表示不改写() {
        DataPermissionSqlHandler handler = handlerWith(rule("sys_user", "dept_id = 1", true));

        assertNull(handler.getSqlSegment(new Table("sys_role"), null, "anyMsId"));
    }

    @Test
    void 适配器_规则不适用时应返回null() {
        DataPermissionSqlHandler handler = handlerWith(rule("sys_user", "dept_id = 1", false));

        assertNull(handler.getSqlSegment(new Table("sys_user"), null, "anyMsId"));
    }

    @Test
    void 适配器_非法SQL片段应抛出明确异常() {
        DataPermissionSqlHandler handler = handlerWith(rule("sys_user", "and and", true));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> handler.getSqlSegment(new Table("sys_user"), null, "anyMsId"));
        assertTrue(ex.getMessage().contains("sys_user"));
    }

    @Test
    void SQL改写_已有WHERE时应以AND合并权限条件() {
        DataPermissionInterceptor interceptor =
                new DataPermissionInterceptor(handlerWith(rule("sys_user", "dept_id IN (1, 2, 3)", true)));

        String result = interceptor.parserSingle(
                "SELECT * FROM sys_user WHERE status = 1", "com.demo.UserMapper.selectList");

        assertTrue(result.contains("status = 1"), "原有条件应保留: " + result);
        assertTrue(result.contains("dept_id IN (1, 2, 3)"), "应追加权限条件: " + result);
        assertTrue(result.toUpperCase().contains("AND"), "应以 AND 合并: " + result);
    }

    @Test
    void SQL改写_无WHERE时应自动追加WHERE子句() {
        DataPermissionInterceptor interceptor =
                new DataPermissionInterceptor(handlerWith(rule("sys_user", "dept_id = 9", true)));

        String result = interceptor.parserSingle("SELECT * FROM sys_user", "com.demo.UserMapper.selectAll");

        assertTrue(result.toUpperCase().contains("WHERE"), "应补充 WHERE: " + result);
        assertTrue(result.contains("dept_id = 9"), "应追加权限条件: " + result);
    }

    @Test
    void SQL改写_无适用规则的表应保持原样() {
        DataPermissionInterceptor interceptor =
                new DataPermissionInterceptor(handlerWith(rule("sys_user", "dept_id = 1", true)));

        String original = "SELECT * FROM sys_role WHERE status = 1";
        String result = interceptor.parserSingle(original, "com.demo.RoleMapper.selectList");

        assertFalse(result.contains("dept_id"), "无规则的表不应被改写: " + result);
    }
}
