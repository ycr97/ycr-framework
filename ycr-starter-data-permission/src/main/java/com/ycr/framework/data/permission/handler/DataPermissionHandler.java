package com.ycr.framework.data.permission.handler;

import com.ycr.framework.data.permission.rule.DataPermissionRule;
import com.ycr.framework.data.permission.rule.Op;
import com.ycr.framework.data.permission.rule.Predicate;
import com.ycr.framework.data.permission.scope.DataScope;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限规则注册表 + 谓词合并引擎。
 *
 * <p>同表「在效规则」（命令匹配且 {@code appliesTo}）的谓词按 AND 合并成单个条件表达式：
 * Skip 跳过、Deny 即 {@code 1=0}（fail-closed，AND 下短路）、Column 转义构造、Raw 原样解析。
 * 受治理表在效规则全 Skip → fail-closed；无在效规则 → 返回 {@code null}（不改写）。</p>
 *
 * @author ycr
 */
public class DataPermissionHandler {

    private final List<DataPermissionRule> rules = new ArrayList<>();
    private final Set<String> governedTables;

    public DataPermissionHandler() {
        this(List.of());
    }

    public DataPermissionHandler(Collection<String> governedTables) {
        this.governedTables = governedTables.stream()
                .map(this::normalize)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public void addRule(DataPermissionRule rule) {
        rules.add(rule);
    }

    /**
     * 构建某表在给定 scope/命令/方法下的合并条件。
     *
     * @return 需追加的条件表达式；{@code null} 表示该表无在效规则、不改写
     */
    public Expression buildExpression(String table, DataScope scope, SqlCommandType cmd, String mappedStatementId) {
        String bare = strip(table);
        boolean governed = governedTables.isEmpty()
                ? rules.stream().anyMatch(rule -> strip(rule.table()).equalsIgnoreCase(bare))
                : governedTables.contains(normalize(bare));
        if (governed && cmd == SqlCommandType.UNKNOWN) {
            return deny();
        }
        List<Expression> exprs = new ArrayList<>();
        boolean anyInPlay = false;
        for (DataPermissionRule rule : rules) {
            if (!strip(rule.table()).equalsIgnoreCase(bare)) {
                continue;
            }
            if (!rule.commands().contains(cmd) || !rule.appliesTo(mappedStatementId)) {
                continue;
            }
            anyInPlay = true;
            Predicate predicate = rule.predicate(scope);
            if (predicate instanceof Predicate.Control control) {
                if (control == Predicate.Control.SKIP) {
                    continue;
                }
                return deny();                       // DENY：AND 下短路
            } else if (predicate instanceof Predicate.Column column) {
                exprs.add(buildColumn(column));
            } else if (predicate instanceof Predicate.Raw raw) {
                exprs.add(parse(raw.sql()));
            }
        }
        if (!anyInPlay) {
            return null;                             // 无在效规则 → 不改写
        }
        if (exprs.isEmpty()) {
            return deny();                           // 在效规则全 Skip → fail-closed
        }
        return exprs.stream().reduce((a, b) -> new AndExpression(a, b)).orElse(null);
    }

    private Expression buildColumn(Predicate.Column column) {
        Collection<?> values = column.values();
        if ((column.op() == Op.IN || column.op() == Op.NOT_IN) && values.isEmpty()) {
            return deny();                           // 适用但空集 → 闭合（规则一般应直接返 Deny）
        }
        String sql = switch (column.op()) {
            case IN -> column.column() + " IN (" + literals(values) + ")";
            case NOT_IN -> column.column() + " NOT IN (" + literals(values) + ")";
            case EQ -> column.column() + " = " + literal(values.iterator().next());
            case NE -> column.column() + " <> " + literal(values.iterator().next());
        };
        return parse(sql);
    }

    private String literals(Collection<?> values) {
        return values.stream().map(this::literal).collect(Collectors.joining(", "));
    }

    /** 数值/布尔原样；其余按字符串单引号转义（防注入）。 */
    private String literal(Object value) {
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return "'" + String.valueOf(value).replace("'", "''") + "'";
    }

    private Expression deny() {
        return parse("1 = 0");
    }

    private Expression parse(String sql) {
        try {
            return CCJSqlParserUtil.parseCondExpression(sql);
        } catch (JSQLParserException e) {
            throw new IllegalStateException("数据权限条件解析失败: " + sql, e);
        }
    }

    private String strip(String name) {
        return name == null ? "" : name.replace("`", "").replace("\"", "");
    }

    private String normalize(String name) {
        return strip(name).toLowerCase(Locale.ROOT);
    }
}
