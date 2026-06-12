package com.ycr.framework.excel.handler;

import cn.idev.excel.annotation.ExcelProperty;
import com.ycr.framework.excel.annotation.ExcelExport;
import com.ycr.framework.excel.util.ExcelUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExcelExportReturnValueHandler 真实行为测试
 *
 * @author ycr
 */
class ExcelExportReturnValueHandlerTest {

    private final ExcelExportReturnValueHandler handler = new ExcelExportReturnValueHandler();

    @ExcelExport(value = "用户", sheetName = "用户")
    public List<Demo> exportMethod() {
        return List.of();
    }

    public List<Demo> plainMethod() {
        return List.of();
    }

    private MethodParameter returnParam(String methodName) throws NoSuchMethodException {
        Method method = getClass().getMethod(methodName);
        return new MethodParameter(method, -1);
    }

    @Test
    void 注解方法应被支持() throws Exception {
        assertTrue(handler.supportsReturnType(returnParam("exportMethod")));
    }

    @Test
    void 无注解方法不应被支持() throws Exception {
        assertFalse(handler.supportsReturnType(returnParam("plainMethod")));
    }

    @Test
    void 应写出xlsx并标记请求已处理() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        ModelAndViewContainer mavContainer = new ModelAndViewContainer();

        List<Demo> data = List.of(new Demo("张三", 18), new Demo("李四", 30));
        handler.handleReturnValue(data, returnParam("exportMethod"), mavContainer, webRequest);

        assertTrue(mavContainer.isRequestHandled(), "应标记请求已处理");
        byte[] bytes = response.getContentAsByteArray();
        assertTrue(bytes.length > 0);
        assertTrue(response.getHeader("Content-Disposition").contains(".xlsx"));

        // 字节可被读回，证明确实是有效 xlsx
        List<Demo> read = ExcelUtils.read(new ByteArrayInputStream(bytes), Demo.class, 1);
        assertEquals(2, read.size());
        assertEquals("张三", read.get(0).getName());
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
