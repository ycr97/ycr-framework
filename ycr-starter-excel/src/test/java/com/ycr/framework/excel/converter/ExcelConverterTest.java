package com.ycr.framework.excel.converter;

import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import com.ycr.framework.core.enums.BaseEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Excel 转换器测试
 *
 * @author ycr
 */
class ExcelConverterTest {

    @Test
    void 大数转换器写出字符串读回Long() throws Exception {
        ExcelBigNumberConverter converter = new ExcelBigNumberConverter();
        long id = 123456789012345L;

        WriteCellData<?> write = converter.convertToExcelData(id, null, null);
        assertEquals("123456789012345", write.getStringValue());

        ReadCellData<?> cell = new ReadCellData<>(CellDataTypeEnum.STRING, "123456789012345");
        Long back = converter.convertToJavaData(cell, null, null);
        assertEquals(id, back);
    }

    @Test
    void 枚举转换器写描述读回枚举() throws Exception {
        StatusConverter converter = new StatusConverter();

        WriteCellData<?> write = converter.convertToExcelData(Status.ENABLED, null, null);
        assertEquals("启用", write.getStringValue());

        ReadCellData<?> cell = new ReadCellData<>(CellDataTypeEnum.STRING, "禁用");
        Status back = converter.convertToJavaData(cell, null, null);
        assertEquals(Status.DISABLED, back);
    }

    @Test
    void 枚举转换器应解析泛型枚举类型() {
        StatusConverter converter = new StatusConverter();
        assertEquals(Status.class, converter.supportJavaTypeKey());
    }

    /** 测试枚举：对接框架 BaseEnum */
    public enum Status implements BaseEnum<Integer> {
        ENABLED(1, "启用"),
        DISABLED(0, "禁用");

        private final Integer value;
        private final String description;

        Status(Integer value, String description) {
            this.value = value;
            this.description = description;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    /** 空子类即可使用 */
    public static class StatusConverter extends ExcelBaseEnumConverter<Status> {
    }
}
