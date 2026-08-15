package com.ycr.framework.excel.util;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExcelUtils 真实导入导出往返测试
 *
 * @author ycr
 */
class ExcelUtilsTest {

    @Test
    @DisplayName("导出再读回应得到相同数据")
    void shouldMatchExpectedBehavior001() throws Exception {
        List<Demo> data = List.of(new Demo("张三", 18), new Demo("李四", 30));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ExcelUtils.export(baos, "用户", "Sheet1", Demo.class, data);

        List<Demo> read = ExcelUtils.read(new ByteArrayInputStream(baos.toByteArray()), Demo.class, 1);

        assertEquals(2, read.size());
        assertEquals("张三", read.get(0).getName());
        assertEquals(18, read.get(0).getAge());
        assertEquals("李四", read.get(1).getName());
        assertEquals(30, read.get(1).getAge());
    }

    @Test
    @DisplayName("导出到响应应设置下载头与字节")
    void shouldMatchExpectedBehavior002() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        ExcelUtils.export(response, "用户列表", "Sheet1", Demo.class, List.of(new Demo("张三", 18)));

        String disposition = response.getHeader("Content-Disposition");
        assertNotNull(disposition);
        assertTrue(disposition.contains("attachment"));
        assertTrue(disposition.contains(".xlsx"));
        assertTrue(response.getContentType().contains("spreadsheetml"));
        assertTrue(response.getContentAsByteArray().length > 0);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Demo {
        @ExcelProperty("姓名")
        private String name;
        @ExcelProperty("年龄")
        private Integer age;
    }
}
