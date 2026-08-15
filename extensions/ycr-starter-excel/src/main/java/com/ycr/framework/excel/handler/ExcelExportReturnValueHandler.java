package com.ycr.framework.excel.handler;

import com.ycr.framework.excel.annotation.ExcelExport;
import com.ycr.framework.excel.util.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;
import java.util.List;

/**
 * {@code @ExcelExport} 导出返回值处理器
 *
 * <p>当 Controller 方法标注 {@link ExcelExport} 时接管其返回值：从返回类型 {@code List<T>} 的泛型
 * 解析元素类型 {@code T}，调用 {@link ExcelUtils} 写出 xlsx 到响应，并标记请求已处理。</p>
 *
 * @author ycr
 */
public class ExcelExportReturnValueHandler implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.hasMethodAnnotation(ExcelExport.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleReturnValue(Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        // 接管响应渲染，阻止后续视图解析
        mavContainer.setRequestHandled(true);

        ExcelExport annotation = returnType.getMethodAnnotation(ExcelExport.class);
        Class<?> elementType = ResolvableType.forMethodParameter(returnType).getGeneric(0).resolve();
        if (annotation == null || elementType == null) {
            throw new IllegalStateException("@ExcelExport 方法必须返回带具体元素类型的 List<T>");
        }

        List<Object> data = returnValue == null ? Collections.emptyList() : (List<Object>) returnValue;
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        if (response == null) {
            throw new IllegalStateException("无法获取 HttpServletResponse，@ExcelExport 仅适用于 Servlet Web 环境");
        }
        ExcelUtils.export(response, annotation.value(), annotation.sheetName(),
                (Class<Object>) elementType, data);
    }
}
