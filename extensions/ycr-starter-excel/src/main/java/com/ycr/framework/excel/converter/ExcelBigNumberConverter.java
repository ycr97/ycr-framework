package com.ycr.framework.excel.converter;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;

/**
 * 大数字转换器 —— Long 写为字符串，避免 Excel 对超过 15 位数字的精度丢失
 *
 * <p>在模型字段上用 {@code @ExcelProperty(converter = ExcelBigNumberConverter.class)} 启用。</p>
 *
 * @author ycr
 */
public class ExcelBigNumberConverter implements Converter<Long> {

    @Override
    public Class<Long> supportJavaTypeKey() {
        return Long.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Long convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                  GlobalConfiguration globalConfiguration) {
        return Long.parseLong(cellData.getStringValue());
    }

    @Override
    public WriteCellData<?> convertToExcelData(Long value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        return new WriteCellData<>(String.valueOf(value));
    }
}
