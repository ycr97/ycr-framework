package com.ycr.framework.auth.oauth2.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.core.model.R;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * YCR OAuth2 Bearer 认证失败处理器。
 *
 * @author ycr
 */
public class YcrBearerAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public YcrBearerAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        boolean unavailable = isIntrospectionUnavailable(authException);
        int status = unavailable ? 503 : 401;
        String message = unavailable ? "认证服务暂不可用" : "未登录或登录已过期";
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE,
                unavailable ? "Bearer error=\"temporarily_unavailable\"" : "Bearer");
        objectMapper.writeValue(response.getOutputStream(), R.fail(status, message));
    }

    private boolean isIntrospectionUnavailable(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof BadOpaqueTokenException) {
                return false;
            }
            if (current instanceof OAuth2IntrospectionException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
