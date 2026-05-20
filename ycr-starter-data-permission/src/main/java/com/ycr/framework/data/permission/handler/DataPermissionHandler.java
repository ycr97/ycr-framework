package com.ycr.framework.data.permission.handler;

import com.ycr.framework.data.permission.rule.DataPermissionRule;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限处理器 - 管理数据权限规则
 *
 * @author ycr
 */
public class DataPermissionHandler {

    private final List<DataPermissionRule> rules = new ArrayList<>();

    public void addRule(DataPermissionRule rule) {
        rules.add(rule);
    }

    public List<DataPermissionRule> getRules() {
        return rules;
    }

    /**
     * 根据表名获取适用的 SQL 片段
     */
    public String getSqlSegment(String tableName) {
        return rules.stream()
                .filter(DataPermissionRule::isApplicable)
                .filter(rule -> rule.getTableName().equalsIgnoreCase(tableName))
                .map(DataPermissionRule::getSqlSegment)
                .findFirst()
                .orElse(null);
    }
}
