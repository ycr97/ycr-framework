package com.ycr.framework.excel.autoconfigure;

import com.ycr.framework.excel.handler.ExcelExportReturnValueHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Excel 自动配置
 *
 * <p>注册 {@link ExcelExportReturnValueHandler} 使 {@code @ExcelExport} 注解导出真正生效——
 * 这是本自动配置的真实职责。通过 {@code ycr.excel.enabled=false} 关闭。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(ExcelProperties.class)
@ConditionalOnProperty(prefix = "ycr.excel", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ExcelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExcelExportReturnValueHandler excelExportReturnValueHandler() {
        return new ExcelExportReturnValueHandler();
    }

    /**
     * 把导出处理器追加到 MVC 返回值处理器链
     */
    @Bean
    public WebMvcConfigurer ycrExcelWebMvcConfigurer(ExcelExportReturnValueHandler handler) {
        return new WebMvcConfigurer() {
            @Override
            public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
                handlers.add(handler);
            }
        };
    }
}
