package com.ycr.framework.web.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.core.model.R;
import com.ycr.framework.web.autoconfigure.WebResponseProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UnifiedResponseBodyAdviceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("普通对象应包装为统一响应")
    void shouldMatchExpectedBehavior001() throws Exception {
        UnifiedResponseBodyAdvice advice = newAdvice(new WebResponseProperties());

        Object body = advice.beforeBodyWrite(Map.of("name", "ycr"), methodParameter("objectResult"),
                MediaType.APPLICATION_JSON, MappingJackson2HttpMessageConverter.class,
                request("/api/users"), response());

        assertThat(body).isInstanceOf(R.class);
        R<?> response = (R<?>) body;
        assertThat(response.getCode()).isEqualTo("200");
        assertThat(response.getData()).isEqualTo(Map.of("name", "ycr"));
    }

    @Test
    @DisplayName("字符串响应应序列化为Json字符串")
    void shouldMatchExpectedBehavior002() throws Exception {
        UnifiedResponseBodyAdvice advice = newAdvice(new WebResponseProperties());
        ServletServerHttpResponse response = response();

        Object body = advice.beforeBodyWrite("ok", methodParameter("stringResult"),
                MediaType.TEXT_PLAIN, StringHttpMessageConverter.class,
                request("/api/ping"), response);

        assertThat(body).isInstanceOf(String.class);
        JsonNode node = objectMapper.readTree((String) body);
        assertThat(node.get("code").asText()).isEqualTo("200");
        assertThat(node.get("data").asText()).isEqualTo("ok");
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("已包装响应不应重复包装")
    void shouldMatchExpectedBehavior003() throws Exception {
        UnifiedResponseBodyAdvice advice = newAdvice(new WebResponseProperties());
        R<String> original = R.ok("ok");

        Object body = advice.beforeBodyWrite(original, methodParameter("responseResult"),
                MediaType.APPLICATION_JSON, MappingJackson2HttpMessageConverter.class,
                request("/api/ping"), response());

        assertThat(body).isSameAs(original);
    }

    @Test
    @DisplayName("排除路径不应包装")
    void shouldMatchExpectedBehavior004() throws Exception {
        WebResponseProperties properties = new WebResponseProperties();
        properties.setExcludePaths(List.of("/open/**"));
        UnifiedResponseBodyAdvice advice = newAdvice(properties);

        Object body = advice.beforeBodyWrite(Map.of("name", "raw"), methodParameter("objectResult"),
                MediaType.APPLICATION_JSON, MappingJackson2HttpMessageConverter.class,
                request("/open/users"), response());

        assertThat(body).isEqualTo(Map.of("name", "raw"));
    }

    @Test
    @DisplayName("未命中包含路径不应包装")
    void shouldMatchExpectedBehavior005() throws Exception {
        WebResponseProperties properties = new WebResponseProperties();
        properties.setIncludePaths(List.of("/api/**"));
        UnifiedResponseBodyAdvice advice = newAdvice(properties);

        Object body = advice.beforeBodyWrite(Map.of("name", "raw"), methodParameter("objectResult"),
                MediaType.APPLICATION_JSON, MappingJackson2HttpMessageConverter.class,
                request("/internal/users"), response());

        assertThat(body).isEqualTo(Map.of("name", "raw"));
    }

    @Test
    @DisplayName("关闭开关时不支持包装")
    void shouldMatchExpectedBehavior006() throws Exception {
        WebResponseProperties properties = new WebResponseProperties();
        properties.setEnabled(false);
        UnifiedResponseBodyAdvice advice = newAdvice(properties);

        assertThat(advice.supports(methodParameter("objectResult"),
                MappingJackson2HttpMessageConverter.class)).isFalse();
    }

    @Test
    @DisplayName("流式响应不支持包装")
    void shouldMatchExpectedBehavior007() throws Exception {
        UnifiedResponseBodyAdvice advice = newAdvice(new WebResponseProperties());

        assertThat(advice.supports(methodParameter("streamingResult"),
                MappingJackson2HttpMessageConverter.class)).isFalse();
    }

    private UnifiedResponseBodyAdvice newAdvice(WebResponseProperties properties) {
        return new UnifiedResponseBodyAdvice(properties, objectMapper);
    }

    private ServletServerHttpRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        return new ServletServerHttpRequest(request);
    }

    private ServletServerHttpResponse response() {
        return new ServletServerHttpResponse(new MockHttpServletResponse());
    }

    private MethodParameter methodParameter(String methodName) throws NoSuchMethodException {
        Method method = TestController.class.getDeclaredMethod(methodName);
        return new MethodParameter(method, -1);
    }

    @SuppressWarnings("unused")
    private static class TestController {
        Object objectResult() {
            return null;
        }

        String stringResult() {
            return null;
        }

        R<String> responseResult() {
            return null;
        }

        StreamingResponseBody streamingResult() {
            return null;
        }
    }
}
