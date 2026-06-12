package com.ycr.framework.excel.converter;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import com.ycr.framework.core.enums.BaseEnum;

import java.lang.reflect.ParameterizedType;

/**
 * 对接框架 {@link BaseEnum} 的枚举转换器基类
 *
 * <p>Excel 列读写使用枚举的 {@code getDescription()} 文本。用户只需声明一个空的无参子类即可，
 * 例如 {@code class StatusConverter extends ExcelBaseEnumConverter<Status> {}}，
 * 再在字段上 {@code @ExcelProperty(converter = StatusConverter.class)}。</p>
 *
 * <p>枚举类型由子类的泛型参数反射解析，无需用户重复实现描述映射（复用 BaseEnum 既有语义）。</p>
 *
 * @param <E> 实现了 {@link BaseEnum} 的枚举类型
 * @author ycr
 */
public abstract class ExcelBaseEnumConverter<E extends Enum<E> & BaseEnum<?>> implements Converter<E> {

    private final Class<E> enumType;

    @SuppressWarnings("unchecked")
    protected ExcelBaseEnumConverter() {
        this.enumType = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    @Override
    public Class<E> supportJavaTypeKey() {
        return enumType;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public E convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                               GlobalConfiguration globalConfiguration) {
        String description = cellData.getStringValue();
        for (E e : enumType.getEnumConstants()) {
            if (e.getDescription().equals(description)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public WriteCellData<?> convertToExcelData(E value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        return new WriteCellData<>(value == null ? "" : value.getDescription());
    }
}
