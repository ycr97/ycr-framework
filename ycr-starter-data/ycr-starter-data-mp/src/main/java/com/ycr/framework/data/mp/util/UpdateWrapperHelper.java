package com.ycr.framework.data.mp.util;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 按「已知变更字段集合」构建 MyBatis-Plus {@link UpdateWrapper} 的工具。
 *
 * <p>变更字段的检测交由聚合层（{@code ycr-starter-ddd-core} 的 {@code DataObjectUtils}/{@code Aggregate}）完成，
 * 本工具只负责把变更字段映射为精准的 set 子句，并以主键（{@link TableId}）作为更新条件，实现按字段精准落库。</p>
 *
 * <p>列名解析优先走 MyBatis-Plus 表元数据（{@link TableInfoHelper}），这是生产路径，能正确处理驼峰转下划线、
 * 全局列前缀、{@code @TableField} 自定义列名以及父类继承字段。当 {@link TableInfo} 尚未被 MyBatis-Plus
 * 懒加载初始化时（例如 mapper 未注册、纯单元测试场景），降级到反射 + 驼峰转下划线兜底，保证列名仍然正确。</p>
 *
 * @author ycr
 */
public final class UpdateWrapperHelper {

    private UpdateWrapperHelper() {
    }

    /**
     * 构建更新包装器。
     *
     * @param changedFields 变更字段名（实体属性名），不可为空，且每个字段都须是实体可更新字段
     * @param entity        待更新实体，不可为空，且须含 {@link TableId} 主键且主键值非空
     * @param <T>           实体类型
     * @return 仅 set 变更字段、以主键作条件的 UpdateWrapper
     */
    public static <T> UpdateWrapper<T> build(Collection<String> changedFields, T entity) {
        Assert.notNull(changedFields, "changedFields 不能为空");
        Assert.notNull(entity, "entity 不能为空");
        Assert.isTrue(!changedFields.isEmpty(), "changedFields 不能为空集合");

        TableInfo tableInfo = TableInfoHelper.getTableInfo(entity.getClass());
        return tableInfo != null
                ? buildByTableInfo(changedFields, entity, tableInfo)
                : buildByReflection(changedFields, entity);
    }

    /**
     * 生产路径：基于 MyBatis-Plus 表元数据解析列名。
     */
    private static <T> UpdateWrapper<T> buildByTableInfo(Collection<String> changedFields, T entity, TableInfo tableInfo) {
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();

        String keyProperty = tableInfo.getKeyProperty();
        Assert.hasText(keyProperty, "实体缺少 @TableId 主键，无法构建更新条件");
        Object idValue = readField(entity, keyProperty);
        Assert.notNull(idValue, "主键值为空，无法构建更新条件");
        wrapper.eq(tableInfo.getKeyColumn(), idValue);

        Map<String, String> columnByProperty = new LinkedHashMap<>();
        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
            columnByProperty.put(fieldInfo.getProperty(), fieldInfo.getColumn());
        }

        for (String property : changedFields) {
            if (property.equals(keyProperty)) {
                continue;
            }
            String column = columnByProperty.get(property);
            Assert.hasText(column, "变更字段不是可更新表字段: " + property);
            wrapper.set(column, readField(entity, property));
        }
        return wrapper;
    }

    /**
     * 兜底路径：反射遍历类层次（含父类继承字段）+ 驼峰转下划线。
     */
    private static <T> UpdateWrapper<T> buildByReflection(Collection<String> changedFields, T entity) {
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();

        Field idField = findIdField(entity.getClass());
        Assert.notNull(idField, "实体缺少 @TableId 主键，无法构建更新条件");
        ReflectionUtils.makeAccessible(idField);
        Object idValue = ReflectionUtils.getField(idField, entity);
        Assert.notNull(idValue, "主键值为空，无法构建更新条件");
        wrapper.eq(columnName(idField), idValue);

        String idProperty = idField.getName();
        for (String property : changedFields) {
            if (property.equals(idProperty)) {
                continue;
            }
            Field field = ReflectionUtils.findField(entity.getClass(), property);
            Assert.notNull(field, "变更字段在实体中不存在: " + property);
            ReflectionUtils.makeAccessible(field);
            wrapper.set(columnName(field), ReflectionUtils.getField(field, entity));
        }
        return wrapper;
    }

    /**
     * 按属性名读取字段值，沿类层次向上查找（覆盖父类继承字段）。
     */
    private static Object readField(Object entity, String property) {
        Field field = ReflectionUtils.findField(entity.getClass(), property);
        Assert.notNull(field, "实体不存在字段: " + property);
        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, entity);
    }

    /**
     * 沿类层次向上查找首个标注 {@link TableId} 的字段，支持主键声明在父类。
     */
    private static Field findIdField(Class<?> type) {
        Field[] holder = new Field[1];
        ReflectionUtils.doWithFields(type, field -> {
            if (holder[0] == null && field.isAnnotationPresent(TableId.class)) {
                holder[0] = field;
            }
        });
        return holder[0];
    }

    /**
     * 反射兜底下的列名解析：优先取注解显式列名，否则按 MyBatis-Plus 默认规则驼峰转下划线。
     */
    private static String columnName(Field field) {
        TableId tableId = field.getAnnotation(TableId.class);
        if (tableId != null && StringUtils.isNotBlank(tableId.value())) {
            return tableId.value();
        }
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField != null && StringUtils.isNotBlank(tableField.value())) {
            return tableField.value();
        }
        return StringUtils.camelToUnderline(field.getName());
    }
}
