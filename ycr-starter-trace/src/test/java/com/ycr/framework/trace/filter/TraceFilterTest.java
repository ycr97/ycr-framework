package com.ycr.framework.trace.filter;

import com.ycr.framework.trace.util.TraceUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TraceFilter 真实行为测试
 *
 * @author ycr
 */
class TraceFilterTest {

    private static final String HEADER = "X-Trace-Id";
    private static final String REQUEST_HEADER = "X-Request-Id";

    private final TraceFilter filter = new TraceFilter(HEADER, REQUEST_HEADER);

    @AfterEach
    void tearDown() {
        TraceUtils.removeTraceId();
        TraceUtils.removeRequestId();
    }

    @Test
    @DisplayName("上游头存在时应复用并回写响应头")
    void shouldMatchExpectedBehavior001() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HEADER, "up-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> insideTraceId = new AtomicReference<>();
        AtomicReference<String> insideRequestId = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> {
            insideTraceId.set(TraceUtils.getTraceId());
            insideRequestId.set(TraceUtils.getRequestId());
        });

        assertEquals("up-123", insideTraceId.get(), "链内应复用上游 traceId");
        assertNotNull(insideRequestId.get(), "链内应生成 requestId");
        assertEquals("up-123", response.getHeader(HEADER), "响应头应回写 traceId");
        assertEquals(insideRequestId.get(), response.getHeader(REQUEST_HEADER), "响应头应回写 requestId");
    }

    @Test
    @DisplayName("上游头缺失时应自动生成")
    void shouldMatchExpectedBehavior002() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> insideChain = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> insideChain.set(TraceUtils.getTraceId()));

        assertNotNull(insideChain.get());
        assertFalse(insideChain.get().isEmpty());
        assertEquals(insideChain.get(), response.getHeader(HEADER));
    }

    @Test
    @DisplayName("请求结束后应清理traceId")
    void shouldMatchExpectedBehavior003() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertNotNull(TraceUtils.getTraceId());
            assertNotNull(TraceUtils.getRequestId());
        });

        assertNull(TraceUtils.getTraceId(), "请求结束应清理");
        assertNull(TraceUtils.getRequestId(), "请求结束应清理 requestId");
    }

    @Test
    @DisplayName("链内抛异常仍应清理traceId")
    void shouldMatchExpectedBehavior004() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain boom = (req, res) -> {
            throw new ServletException("炸了");
        };

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, boom));
        assertNull(TraceUtils.getTraceId(), "异常路径仍应清理");
        assertNull(TraceUtils.getRequestId(), "异常路径仍应清理 requestId");
    }

    @Test
    @DisplayName("上游requestId存在时应复用并回写响应头")
    void shouldMatchExpectedBehavior005() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(REQUEST_HEADER, "request-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> insideChain = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> insideChain.set(TraceUtils.getRequestId()));

        assertEquals("request-123", insideChain.get());
        assertEquals("request-123", response.getHeader(REQUEST_HEADER));
    }
}
