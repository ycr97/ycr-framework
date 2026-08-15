package com.ycr.framework.data.permission.rule;

import java.util.Collection;
import java.util.List;

/**
 * 数据权限谓词：规则按已解析的 scope 决定本次产出哪种条件。
 *
 * <p>{@link Column} 结构化（框架转义构造）；{@link Raw} 逃生口（禁内插用户输入）；
 * {@link Control#SKIP} 不加约束；{@link Control#DENY} 即 {@code 1=0}（fail-closed）。</p>
 *
 * @author ycr
 */
public sealed interface Predicate {

    record Column(String column, Op op, Collection<?> values) implements Predicate {}

    record Raw(String sql) implements Predicate {}

    enum Control implements Predicate { SKIP, DENY }

    static Predicate skip() {
        return Control.SKIP;
    }

    static Predicate deny() {
        return Control.DENY;
    }

    static Predicate in(String column, Collection<?> values) {
        return new Column(column, Op.IN, values);
    }

    static Predicate eq(String column, Object value) {
        return new Column(column, Op.EQ, List.of(value));
    }
}
