package com.ycr.framework.data.permission.scope;

import org.apache.ibatis.mapping.SqlCommandType;

/**
 * 把 mapper 方法全限定名解析为语句类型，用于按 {@code commands()} 过滤规则。
 *
 * @author ycr
 */
@FunctionalInterface
public interface CommandTypeResolver {

    SqlCommandType resolve(String mappedStatementId);
}
