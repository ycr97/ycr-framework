package com.ycr.framework.core.util;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class ServletUtils {

    private ServletUtils() {
    }

    public static Optional<HttpServletRequest> getRequest() {
        return getServletRequestAttributes().map(ServletRequestAttributes::getRequest);
    }

    public static Optional<HttpServletResponse> getResponse() {
        return getServletRequestAttributes().map(ServletRequestAttributes::getResponse);
    }

    private static Optional<ServletRequestAttributes> getServletRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return Optional.of(servletRequestAttributes);
        }
        return Optional.empty();
    }
}
