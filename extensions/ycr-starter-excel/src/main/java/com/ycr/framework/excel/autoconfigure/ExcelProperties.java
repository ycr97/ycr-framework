package com.ycr.framework.excel.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Excel 配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.excel")
public class ExcelProperties {

    /**
     * 是否启用 Excel 支持（含 {@code @ExcelExport} 注解导出），默认启用
     */
    private boolean enabled = true;
}
