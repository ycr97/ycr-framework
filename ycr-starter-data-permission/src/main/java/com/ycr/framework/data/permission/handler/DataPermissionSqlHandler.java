package com.ycr.framework.data.permission.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.ycr.framework.data.permission.scope.CommandTypeResolver;
import com.ycr.framework.data.permission.scope.DataScope;
import com.ycr.framework.data.permission.scope.DataScopeContext;
import com.ycr.framework.data.permission.scope.DataScopeResolver;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.apache.ibatis.mapping.SqlCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * 数据权限 SQL 适配器：桥接框架规则体系与 MyBatis-Plus 数据权限拦截器。
 *
 * <p>MP 在解析 SELECT/UPDATE/DELETE 时按表回调本处理器：先取（请求级缓存的）数据范围，
 * 再按语句类型过滤在效规则，交由 {@link DataPermissionHandler} 合并为条件表达式返回。
 * SQL 改写由 MP 内置拦截器完成（ADR-002）。</p>
 *
 * @author ycr
 */
public class DataPermissionSqlHandler implements MultiDataPermissionHandler {

    private static final Logger log = LoggerFactory.getLogger(DataPermissionSqlHandler.class);

    private final DataPermissionHandler handler;
    private final DataScopeResolver resolver;
    private final CommandTypeResolver commandTypeResolver;
    private final boolean logApplied;

    public DataPermissionSqlHandler(DataPermissionHandler handler, DataScopeResolver resolver,
                                    CommandTypeResolver commandTypeResolver, boolean logApplied) {
        this.handler = handler;
        this.resolver = resolver;
        this.commandTypeResolver = commandTypeResolver;
        this.logApplied = logApplied;
    }

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        // resolver 抛错 → DataPermissionException，fail-loud 中止查询
        DataScope scope = DataScopeContext.get(resolver);
        SqlCommandType cmd = commandTypeResolver.resolve(mappedStatementId);
        Expression expression = handler.buildExpression(table.getName(), scope, cmd, mappedStatementId);
        if (expression != null && logApplied && log.isDebugEnabled()) {
            log.debug("[data-permission] table={} cmd={} msId={} condition={} traceId={}",
                    table.getName(), cmd, mappedStatementId, expression, MDC.get("traceId"));
        }
        return expression;
    }
}
