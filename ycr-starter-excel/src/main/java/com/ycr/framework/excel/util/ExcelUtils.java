package com.ycr.framework.excel.util;

import cn.idev.excel.FastExcel;
import cn.idev.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel 工具类
 *
 * <p>基于 FastExcel 的流式读写。导出自动挂列宽自适应；大数字段请在模型上用
 * {@code @ExcelProperty(converter = ExcelBigNumberConverter.class)} 防 Excel 精度丢失。</p>
 *
 * @author ycr
 */
public final class ExcelUtils {

    /** xlsx 的 MIME 类型 */
    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private ExcelUtils() {
    }

    /**
     * 导出到输出流
     *
     * @param outputStream 输出流
     * @param fileName     文件名（不含后缀，此处仅用于语义，实际命名由调用方处理）
     * @param sheetName    Sheet 名称
     * @param clazz        数据类型
     * @param data         数据列表
     */
    public static <T> void export(OutputStream outputStream, String fileName, String sheetName,
                                  Class<T> clazz, List<T> data) {
        FastExcel.write(outputStream, clazz)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet(sheetName)
                .doWrite(data);
    }

    /**
     * 导出到 HTTP 响应（浏览器下载）
     *
     * @param response  HTTP 响应
     * @param fileName  文件名（不含后缀），中文会做 URL 编码避免乱码
     * @param sheetName Sheet 名称
     * @param clazz     数据类型
     * @param data      数据列表
     */
    public static <T> void export(HttpServletResponse response, String fileName, String sheetName,
                                  Class<T> clazz, List<T> data) throws IOException {
        response.setContentType(XLSX_CONTENT_TYPE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ".xlsx");
        export(response.getOutputStream(), fileName, sheetName, clazz, data);
    }

    /**
     * 从输入流读取
     *
     * @param inputStream   输入流
     * @param clazz         数据类型
     * @param headRowNumber 表头行数
     * @return 数据列表
     */
    public static <T> List<T> read(InputStream inputStream, Class<T> clazz, int headRowNumber) {
        return FastExcel.read(inputStream, clazz, null)
                .headRowNumber(headRowNumber)
                .sheet()
                .doReadSync();
    }
}
