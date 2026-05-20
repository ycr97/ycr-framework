package com.ycr.framework.data.permission.rule;

/**
 * 数据权限规则接口
 *
 * @author ycr
 */
public interface DataPermissionRule {

    /**
     * 获取适用的表名
     */
    String getTableName();

    /**
     * 获取 SQL 过滤片段（如 "dept_id IN (1,2,3)"）
     */
    String getSqlSegment();

    /**
     * 当前规则是否适用
     */
    boolean isApplicable();
}
