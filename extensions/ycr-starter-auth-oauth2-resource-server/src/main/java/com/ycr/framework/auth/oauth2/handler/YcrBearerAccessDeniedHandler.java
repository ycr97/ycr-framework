package com.ycr.framework.auth.oauth2.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.core.model.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * YCR OAuth2 Bearer 授权失败处理器。
 *
 * @author ycr
 */
public class YcrBearerAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public YcrBearerAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"insufficient_scope\"");
        objectMapper.writeValue(response.getOutputStream(), R.fail(403, "权限不足"));
    }
}
