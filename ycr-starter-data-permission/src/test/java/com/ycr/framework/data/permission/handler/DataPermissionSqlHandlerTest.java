package com.ycr.framework.data.permission.handler;

import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.ycr.framework.data.permission.exception.DataPermissionException;
import com.ycr.framework.data.permission.rule.DataPermissionRule;
import com.ycr.framework.data.permission.rule.Predicate;
import com.ycr.framework.data.permission.scope.CommandTypeResolver;
import com.ycr.framework.data.permission.scope.DataScope;
import com.ycr.framework.data.permission.scope.DataScopeContext;
import com.ycr.framework.data.permission.scope.DataScopeResolver;
import org.apache.ibatis.mapping.SqlCommandType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DataPermissionSqlHandler 与 MyBatis-Plus 拦截器集成测试（SELECT/UPDATE/DELETE 真实改写）
 *
 * @author ycr
 */
class DataPermissionSqlHandlerTest {

    @AfterEach
    void tearDown() {
        DataScopeContext.clear();
    }

    private final DataScopeResolver resolver = () ->
            DataScope.builder().dimension("factory", List.of(1, 2)).build();

    private DataPermissionRule rule(Set<SqlCommandType> cmds, Function<DataScope, Predicate> fn) {
        return new DataPermissionRule() {
            @Override public String table() { return "biz_order"; }
            @Override public Predicate predicate(DataScope scope) { return fn.apply(scope); }
            @Override public Set<SqlCommandType> commands() { return cmds; }
        };
    }

    private DataPermissionInterceptor interceptor(CommandTypeResolver cmdResolver, DataPermissionRule rule) {
        DataPermissionHandler registry = new DataPermissionHandler();
        registry.addRule(rule);
        return new DataPermissionInterceptor(
                new DataPermissionSqlHandler(registry, resolver, cmdResolver, false));
    }

    @Test
    void SELECT_追加权限条件() {
        DataPermissionInterceptor it = interceptor(id -> SqlCommandType.SELECT,
                rule(EnumSet.allOf(SqlCommandType.class), s -> Predicate.in("factory_id", s.values("factory"))));
        String sql = it.parserSingle("SELECT * FROM biz_order WHERE status = 1", "m");
        assertTrue(sql.contains("status = 1"));
        assertTrue(sql.contains("factory_id IN (1, 2)"));
    }

    @Test
    void UPDATE_追加权限条件() {
        DataPermissionInterceptor it = interceptor(id -> SqlCommandType.UPDATE,
                rule(EnumSet.allOf(SqlCommandType.class), s -> Predicate.in("factory_id", s.values("factory"))));
        String sql = it.parserSingle("UPDATE biz_order SET status = 2 WHERE id = 5", "m");
        assertTrue(sql.contains("factory_id IN (1, 2)"), sql);
    }

    @Test
    void DELETE_追加权限条件() {
        DataPermissionInterceptor it = interceptor(id -> SqlCommandType.DELETE,
                rule(EnumSet.allOf(SqlCommandType.class), s -> Predicate.in("factory_id", s.values("factory"))));
        String sql = it.parserSingle("DELETE FROM biz_order WHERE id = 5", "m");
        assertTrue(sql.contains("factory_id IN (1, 2)"), sql);
    }

    @Test
    void SELECT_only规则不改写UPDATE() {
        DataPermissionInterceptor it = interceptor(id -> SqlCommandType.UPDATE,
                rule(EnumSet.of(SqlCommandType.SELECT), s -> Predicate.in("factory_id", List.of(1))));
        String sql = it.parserSingle("UPDATE biz_order SET status = 2 WHERE id = 5", "m");
        assertFalse(sql.contains("factory_id"), sql);
    }

    @Test
    void resolver异常_抛DataPermissionException() {
        DataScopeResolver bad = () -> {
            throw new IllegalStateException("down");
        };
        DataPermissionHandler registry = new DataPermissionHandler();
        registry.addRule(rule(EnumSet.allOf(SqlCommandType.class), s -> Predicate.in("factory_id", List.of(1))));
        DataPermissionInterceptor it = new DataPermissionInterceptor(
                new DataPermissionSqlHandler(registry, bad, id -> SqlCommandType.SELECT, false));
        assertThrows(DataPermissionException.class,
                () -> it.parserSingle("SELECT * FROM biz_order", "m"));
    }
}
