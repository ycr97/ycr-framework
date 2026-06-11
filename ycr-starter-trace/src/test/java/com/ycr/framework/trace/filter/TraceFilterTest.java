package com.ycr.framework.trace.filter;

import com.ycr.framework.trace.util.TraceUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

    private final TraceFilter filter = new TraceFilter(HEADER);

    @AfterEach
    void tearDown() {
        TraceUtils.removeTraceId();
    }

    @Test
    void 上游头存在时应复用并回写响应头() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HEADER, "up-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> insideChain = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> insideChain.set(TraceUtils.getTraceId()));

        assertEquals("up-123", insideChain.get(), "链内应复用上游 traceId");
        assertEquals("up-123", response.getHeader(HEADER), "响应头应回写 traceId");
    }

    @Test
    void 上游头缺失时应自动生成() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> insideChain = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> insideChain.set(TraceUtils.getTraceId()));

        assertNotNull(insideChain.get());
        assertFalse(insideChain.get().isEmpty());
        assertEquals(insideChain.get(), response.getHeader(HEADER));
    }

    @Test
    void 请求结束后应清理traceId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> assertNotNull(TraceUtils.getTraceId()));

        assertNull(TraceUtils.getTraceId(), "请求结束应清理");
    }

    @Test
    void 链内抛异常仍应清理traceId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain boom = (req, res) -> {
            throw new ServletException("炸了");
        };

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, boom));
        assertNull(TraceUtils.getTraceId(), "异常路径仍应清理");
    }
}
