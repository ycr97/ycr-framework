package com.ycr.framework.excel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel 导出注解
 *
 * <p>标注在返回 {@code List<T>} 的 Controller 方法上，由 {@code ExcelExportReturnValueHandler}
 * 在返回阶段自动写出 xlsx 到响应（浏览器下载），无需在方法体内手写导出逻辑。</p>
 *
 * @author ycr
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelExport {

    /**
     * 文件名（不含后缀）
     */
    String value() default "export";

    /**
     * Sheet 名称
     */
    String sheetName() default "Sheet1";
}
