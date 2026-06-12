package com.ycr.framework.excel.autoconfigure;

import com.ycr.framework.excel.handler.ExcelExportReturnValueHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ExcelAutoConfiguration 装配与开关测试
 *
 * @author ycr
 */
class ExcelAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExcelAutoConfiguration.class));

    @Test
    void 默认应装配导出返回值处理器() {
        runner.run(context -> assertThat(context).hasSingleBean(ExcelExportReturnValueHandler.class));
    }

    @Test
    void 关闭开关时不装配() {
        runner.withPropertyValues("ycr.excel.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(ExcelExportReturnValueHandler.class));
    }
}
