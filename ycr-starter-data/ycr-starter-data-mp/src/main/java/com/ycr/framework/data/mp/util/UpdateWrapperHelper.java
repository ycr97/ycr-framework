package com.ycr.framework.data.mp.util;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * 按「已知变更字段集合」构建 MyBatis-Plus {@link UpdateWrapper} 的工具。
 *
 * <p>变更字段的检测交由聚合层（{@code ycr-starter-ddd-core} 的 {@code DataObjectUtils}/{@code Aggregate}）完成，
 * 本工具只负责把变更字段映射为精准的 set 子句，并以主键（{@link TableId}）作为更新条件，实现按字段精准落库。</p>
 *
 * @author ycr
 */
public final class UpdateWrapperHelper {

    private UpdateWrapperHelper() {
    }

    /**
     * 构建更新包装器。
     *
     * @param changedFields 变更字段名（实体属性名），不可为空
     * @param entity        待更新实体，不可为空，且须含 {@link TableId} 主键且主键值非空
     * @param <T>           实体类型
     * @return 仅 set 变更字段、以主键作条件的 UpdateWrapper
     */
    public static <T> UpdateWrapper<T> build(Collection<String> changedFields, T entity) {
        Assert.notNull(changedFields, "changedFields 不能为空");
        Assert.notNull(entity, "entity 不能为空");
        Assert.isTrue(!changedFields.isEmpty(), "changedFields 不能为空集合");

        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        Object idValue = null;
        for (Field field : entity.getClass().getDeclaredFields()) {
            boolean isId = field.isAnnotationPresent(TableId.class);
            if (!isId && !changedFields.contains(field.getName())) {
                continue;
            }
            ReflectionUtils.makeAccessible(field);
            Object value = ReflectionUtils.getField(field, entity);
            if (isId) {
                wrapper.eq(columnName(field), value);
                idValue = value;
            } else {
                wrapper.set(columnName(field), value);
            }
        }
        Assert.notNull(idValue, "实体缺少 @TableId 主键或主键值为空，无法构建更新条件");
        return wrapper;
    }

    private static String columnName(Field field) {
        if (field.isAnnotationPresent(TableId.class)) {
            String value = field.getAnnotation(TableId.class).value();
            return StringUtils.hasText(value) ? value : field.getName();
        }
        if (field.isAnnotationPresent(TableField.class)) {
            String value = field.getAnnotation(TableField.class).value();
            return StringUtils.hasText(value) ? value : field.getName();
        }
        return field.getName();
    }
}
