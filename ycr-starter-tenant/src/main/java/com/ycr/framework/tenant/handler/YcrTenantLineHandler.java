package com.ycr.framework.tenant.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.tenant.autoconfigure.TenantProperties;
import com.ycr.framework.tenant.util.TenantHelper;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.List;

/**
 * 租户行处理器 —— 为 SQL 自动注入租户条件
 *
 * <p>租户 ID 取自 {@link TenantContextHolder}；上下文为空时 fail-closed 抛异常，拒绝越权全表查询。
 * 列名与忽略表由 {@link TenantProperties} 配置。</p>
 *
 * @author ycr
 */
public class YcrTenantLineHandler implements TenantLineHandler {

    private final TenantProperties properties;

    public YcrTenantLineHandler(TenantProperties properties) {
        this.properties = properties;
    }

    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("当前无租户上下文，拒绝执行租户隔离查询");
        }
        return new LongValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return properties.getTenantIdColumn();
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 动态旁路（TenantHelper 作用域）优先于静态忽略表配置
        if (TenantHelper.isIgnored()) {
            return true;
        }
        List<String> ignoreTables = properties.getIgnoreTables();
        return ignoreTables != null && ignoreTables.contains(tableName);
    }
}
