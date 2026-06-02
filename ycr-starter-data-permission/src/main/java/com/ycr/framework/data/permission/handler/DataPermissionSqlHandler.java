package com.ycr.framework.data.permission.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import org.springframework.util.StringUtils;

/**
 * 数据权限 SQL 处理器
 *
 * <p>作为 MyBatis-Plus {@link MultiDataPermissionHandler} 的适配实现，桥接框架自定义的
 * {@link DataPermissionHandler} 规则体系。MyBatis-Plus 在解析每条 SQL 时会按出现的表逐个回调本处理器，
 * 本处理器根据表名查找匹配的数据权限规则，并把规则给出的 SQL 片段（如 {@code dept_id IN (1, 2, 3)}）
 * 解析为 JSqlParser 条件表达式返回，由 MyBatis-Plus 自动合并进该表的 WHERE 条件，从而实现行级数据权限过滤。</p>
 *
 * <p>SQL 改写的解析与合并完全交给 MyBatis-Plus 内置拦截器完成，框架层只负责"哪张表 + 追加什么条件"的规则决策。</p>
 *
 * @author ycr
 */
public class DataPermissionSqlHandler implements MultiDataPermissionHandler {

    private final DataPermissionHandler dataPermissionHandler;

    public DataPermissionSqlHandler(DataPermissionHandler dataPermissionHandler) {
        this.dataPermissionHandler = dataPermissionHandler;
    }

    /**
     * 针对单张表返回需要追加的数据权限条件表达式
     *
     * @param table             当前 SQL 中出现的表
     * @param where             该表原有的 WHERE 条件（此处未使用，由 MyBatis-Plus 负责与返回值合并）
     * @param mappedStatementId 当前执行的 Mapper 方法全限定名
     * @return 需追加的条件表达式；返回 {@code null} 表示该表无适用规则，保持原 SQL 不变
     */
    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        // 去除表名可能携带的反引号/双引号，以便与规则中配置的表名做不区分大小写的匹配
        String tableName = stripQuotes(table.getName());
        String sqlSegment = dataPermissionHandler.getSqlSegment(tableName);
        // 该表没有适用规则：返回 null，MyBatis-Plus 不会改写这张表的查询条件
        if (!StringUtils.hasText(sqlSegment)) {
            return null;
        }
        try {
            // 将规则的 SQL 片段解析为条件表达式，交由 MyBatis-Plus 追加到 WHERE 条件
            return CCJSqlParserUtil.parseCondExpression(sqlSegment);
        } catch (JSQLParserException e) {
            throw new IllegalStateException(
                    "数据权限 SQL 片段解析失败，表[" + tableName + "]，片段[" + sqlSegment + "]", e);
        }
    }

    /**
     * 去除标识符两侧的反引号或双引号，统一为裸表名
     */
    private String stripQuotes(String name) {
        if (name == null) {
            return null;
        }
        return name.replace("`", "").replace("\"", "");
    }
}
