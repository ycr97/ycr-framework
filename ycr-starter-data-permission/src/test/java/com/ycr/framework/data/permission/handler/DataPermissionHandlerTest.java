package com.ycr.framework.data.permission.handler;

import com.ycr.framework.data.permission.rule.DataPermissionRule;
import com.ycr.framework.data.permission.rule.Predicate;
import com.ycr.framework.data.permission.scope.DataScope;
import net.sf.jsqlparser.expression.Expression;
import org.apache.ibatis.mapping.SqlCommandType;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * DataPermissionHandler 谓词合并与 fail-closed 测试
 *
 * @author ycr
 */
class DataPermissionHandlerTest {

    private DataPermissionRule rule(String table, Function<DataScope, Predicate> fn) {
        return rule(table, EnumSet.of(SqlCommandType.SELECT, SqlCommandType.UPDATE, SqlCommandType.DELETE), fn);
    }

    private DataPermissionRule rule(String table, Set<SqlCommandType> cmds, Function<DataScope, Predicate> fn) {
        return new DataPermissionRule() {
            @Override public String table() { return table; }
            @Override public Predicate predicate(DataScope scope) { return fn.apply(scope); }
            @Override public Set<SqlCommandType> commands() { return cmds; }
        };
    }

    private DataPermissionHandler handlerWith(DataPermissionRule... rules) {
        DataPermissionHandler h = new DataPermissionHandler();
        for (DataPermissionRule r : rules) {
            h.addRule(r);
        }
        return h;
    }

    private final DataScope scope = DataScope.builder()
            .dimension("factory", List.of(1, 2)).dimension("brand", List.of(9)).build();

    @Test
    void Column谓词渲染为IN条件() {
        DataPermissionHandler h = handlerWith(rule("biz_order", s -> Predicate.in("factory_id", s.values("factory"))));
        Expression e = h.buildExpression("biz_order", scope, SqlCommandType.SELECT, "m");
        assertEquals("factory_id IN (1, 2)", e.toString());
    }

    @Test
    void 同表多规则AND合并() {
        DataPermissionHandler h = handlerWith(
                rule("biz_order", s -> Predicate.in("factory_id", s.values("factory"))),
                rule("biz_order", s -> Predicate.in("brand_id", s.values("brand"))));
        Expression e = h.buildExpression("biz_order", scope, SqlCommandType.SELECT, "m");
        assertEquals("factory_id IN (1, 2) AND brand_id IN (9)", e.toString());
    }

    @Test
    void Deny渲染为1等于0() {
        DataPermissionHandler h = handlerWith(rule("biz_order", s -> Predicate.deny()));
        Expression e = h.buildExpression("biz_order", scope, SqlCommandType.SELECT, "m");
        assertEquals("1 = 0", e.toString());
    }

    @Test
    void 受治理表在效规则全Skip时fail_closed() {
        DataPermissionHandler h = handlerWith(rule("biz_order", s -> Predicate.skip()));
        Expression e = h.buildExpression("biz_order", scope, SqlCommandType.SELECT, "m");
        assertEquals("1 = 0", e.toString());
    }

    @Test
    void 非受治理表不改写返回null() {
        DataPermissionHandler h = handlerWith(rule("biz_order", s -> Predicate.skip()));
        assertNull(h.buildExpression("other_table", scope, SqlCommandType.SELECT, "m"));
    }

    @Test
    void 命令不匹配的规则不在效_无在效规则则不改写() {
        DataPermissionHandler h = handlerWith(
                rule("biz_order", EnumSet.of(SqlCommandType.SELECT), s -> Predicate.in("factory_id", List.of(1))));
        assertNull(h.buildExpression("biz_order", scope, SqlCommandType.UPDATE, "m"));
    }

    @Test
    void Raw谓词原样解析() {
        DataPermissionHandler h = handlerWith(rule("biz_order", s -> new Predicate.Raw("factory_id = 5")));
        assertEquals("factory_id = 5", h.buildExpression("biz_order", scope, SqlCommandType.SELECT, "m").toString());
    }

    @Test
    void 字符串值转义渲染() {
        DataPermissionHandler h = handlerWith(rule("biz_order", s -> Predicate.in("code", List.of("A", "B'x"))));
        assertEquals("code IN ('A', 'B''x')", h.buildExpression("biz_order", scope, SqlCommandType.SELECT, "m").toString());
    }
}
