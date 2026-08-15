package com.ycr.framework.feign.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 读取当前线程绑定的 Servlet 请求头。无请求上下文（如异步线程、定时任务发起的 Feign 调用）时返回 null。
 *
 * @author ycr
 */
final class CurrentRequestHelper {

    private CurrentRequestHelper() {
    }

    /**
     * 取当前请求指定头的值。
     *
     * @param name 头名
     * @return 头值；无请求上下文时返回 null
     */
    static String header(String name) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            return request.getHeader(name);
        }
        return null;
    }
}
