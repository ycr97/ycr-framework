package com.ycr.framework.data.permission.scope;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据范围：维度名 → 当前主体可见值集合。
 *
 * <p>维度缺键表示「不适用」（规则应返回 Skip）；维度存在但值为空表示「适用但无授权」
 * （规则应返回 Deny → 1=0）。</p>
 *
 * @author ycr
 */
public final class DataScope {

    private final Map<String, Collection<?>> dimensions;

    private DataScope(Map<String, Collection<?>> dimensions) {
        this.dimensions = dimensions;
    }

    public static DataScope empty() {
        return new DataScope(Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    /** 维度是否适用（键是否存在）。 */
    public boolean has(String dimension) {
        return dimensions.containsKey(dimension);
    }

    /** 维度可见值；缺键返回空集合。 */
    public Collection<?> values(String dimension) {
        Collection<?> v = dimensions.get(dimension);
        return v == null ? List.of() : v;
    }

    public static final class Builder {

        private final Map<String, Collection<?>> dimensions = new HashMap<>();

        public Builder dimension(String name, Collection<?> values) {
            dimensions.put(name, values == null ? List.of() : values);
            return this;
        }

        public DataScope build() {
            return new DataScope(Map.copyOf(dimensions));
        }
    }
}
