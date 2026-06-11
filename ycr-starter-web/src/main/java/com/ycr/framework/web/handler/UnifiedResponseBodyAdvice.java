package com.ycr.framework.web.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.core.model.R;
import com.ycr.framework.web.autoconfigure.WebResponseProperties;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.InputStream;
import java.util.List;

/**
 * 统一响应包装。
 *
 * @author ycr
 */
@RestControllerAdvice
public class UnifiedResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final WebResponseProperties properties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public UnifiedResponseBodyAdvice(WebResponseProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (!properties.isEnabled()) {
            return false;
        }
        Class<?> parameterType = returnType.getParameterType();
        return !R.class.isAssignableFrom(parameterType)
                && !ResponseEntity.class.isAssignableFrom(parameterType)
                && !isStreamingType(parameterType);
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (!shouldWrap(request) || body instanceof R<?> || isStreamingBody(body)) {
            return body;
        }
        R<Object> wrapped = R.ok(body);
        if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType) || body instanceof String) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            try {
                return objectMapper.writeValueAsString(wrapped);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to serialize unified response", e);
            }
        }
        return wrapped;
    }

    private boolean shouldWrap(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return matches(properties.getIncludePaths(), path, true)
                && !matches(properties.getExcludePaths(), path, false);
    }

    private boolean matches(List<String> patterns, String path, boolean defaultWhenEmpty) {
        if (patterns == null || patterns.isEmpty()) {
            return defaultWhenEmpty;
        }
        return patterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private boolean isStreamingType(Class<?> type) {
        return Resource.class.isAssignableFrom(type)
                || byte[].class.isAssignableFrom(type)
                || InputStream.class.isAssignableFrom(type)
                || StreamingResponseBody.class.isAssignableFrom(type)
                || ResponseBodyEmitter.class.isAssignableFrom(type)
                || SseEmitter.class.isAssignableFrom(type);
    }

    private boolean isStreamingBody(@Nullable Object body) {
        return body != null && isStreamingType(body.getClass());
    }
}
