package com.ycr.framework.protect.xss.filter;

import com.ycr.framework.protect.xss.XssCleaner;
import com.ycr.framework.protect.xss.enums.XssMode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * XSS 请求包装器：对请求参数按 {@link XssMode} 清理。
 *
 * <p>覆盖 {@code getParameter*}，使下游读取到的是已清理值。请求头保留原始值，避免破坏认证、签名和内容协商。</p>
 *
 * @author ycr
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    private final XssMode mode;

    public XssRequestWrapper(HttpServletRequest request, XssMode mode) {
        super(request);
        this.mode = mode;
    }

    @Override
    public String getParameter(String name) {
        return XssCleaner.sanitize(super.getParameter(name), mode);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] cleaned = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            cleaned[i] = XssCleaner.sanitize(values[i], mode);
        }
        return cleaned;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> source = super.getParameterMap();
        Map<String, String[]> cleaned = new LinkedHashMap<>(source.size());
        source.forEach((key, values) -> {
            String[] copy = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                copy[i] = XssCleaner.sanitize(values[i], mode);
            }
            cleaned.put(key, copy);
        });
        return cleaned;
    }

}
