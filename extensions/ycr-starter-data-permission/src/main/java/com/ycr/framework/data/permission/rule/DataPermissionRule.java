package com.ycr.framework.data.permission.rule;

import com.ycr.framework.data.permission.scope.DataScope;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.EnumSet;
import java.util.Set;

/**
 * 数据权限规则 SPI：消费已解析的 {@link DataScope} 产出 {@link Predicate}。
 *
 * @author ycr
 */
public interface DataPermissionRule {

    /** 作用表（裸名，匹配不区分大小写）。 */
    String table();

    /** 按 scope 决定本次谓词（Column/Raw/Skip/Deny）。 */
    Predicate predicate(DataScope scope);

    /** 作用语句，默认 SELECT/UPDATE/DELETE 三种。 */
    default Set<SqlCommandType> commands() {
        return EnumSet.of(SqlCommandType.SELECT, SqlCommandType.UPDATE, SqlCommandType.DELETE);
    }

    /** 细粒度按 mapper 方法生效/取反，默认全生效。 */
    default boolean appliesTo(String mappedStatementId) {
        return true;
    }
}
