package com.ycr.framework.data.mp.util;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ycr.framework.data.annotation.Query;
import com.ycr.framework.data.enums.QueryType;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * 根据 {@link Query} 注解构建 QueryWrapper
 *
 * @author ycr
 */
public final class QueryWrapperHelper {

    private QueryWrapperHelper() {
    }

    public static <T> QueryWrapper<T> build(Object queryObject) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        if (queryObject == null) {
            return wrapper;
        }

        Field[] fields = queryObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            Query query = field.getAnnotation(Query.class);
            if (query == null) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(queryObject);
                if (value == null || (value instanceof String str && StrUtil.isBlank(str))) {
                    continue;
                }
                String column = StrUtil.isNotBlank(query.column())
                        ? query.column()
                        : StrUtil.toUnderlineCase(field.getName());
                applyCondition(wrapper, query.type(), column, value);
            } catch (IllegalAccessException ignored) {
                // 忽略无法访问的字段
            }
        }
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    private static <T> void applyCondition(QueryWrapper<T> wrapper, QueryType type, String column, Object value) {
        switch (type) {
            case EQ -> wrapper.eq(column, value);
            case NE -> wrapper.ne(column, value);
            case GT -> wrapper.gt(column, value);
            case GE -> wrapper.ge(column, value);
            case LT -> wrapper.lt(column, value);
            case LE -> wrapper.le(column, value);
            case LIKE -> wrapper.like(column, value);
            case LIKE_LEFT -> wrapper.likeLeft(column, value);
            case LIKE_RIGHT -> wrapper.likeRight(column, value);
            case IN -> wrapper.in(column, (Collection<?>) value);
            case BETWEEN -> applyBetween(wrapper, column, value);
            case IS_NULL -> wrapper.isNull(column);
            case IS_NOT_NULL -> wrapper.isNotNull(column);
            default -> wrapper.eq(column, value);
        }
    }

    private static <T> void applyBetween(QueryWrapper<T> wrapper, String column, Object value) {
        if (value instanceof Collection<?> collection && collection.size() >= 2) {
            Object[] values = collection.toArray();
            wrapper.between(column, values[0], values[1]);
            return;
        }
        if (value != null && value.getClass().isArray() && Array.getLength(value) >= 2) {
            wrapper.between(column, Array.get(value, 0), Array.get(value, 1));
        }
    }
}
