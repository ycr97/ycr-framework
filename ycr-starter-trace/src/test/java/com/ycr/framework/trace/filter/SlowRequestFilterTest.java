package com.ycr.framework.trace.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 慢请求过滤器测试。
 *
 * @author ycr
 */
@ExtendWith(OutputCaptureExtension.class)
class SlowRequestFilterTest {

    @Test
    @DisplayName("超过阈值应输出结构化慢请求日志")
    void shouldMatchExpectedBehavior001(CapturedOutput output) throws Exception {
        SlowRequestFilter filter = new SlowRequestFilter(0L);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> response.setStatus(200));

        assertThat(output).contains("event=slow_request")
                .contains("method=GET")
                .contains("uri=/api/users")
                .contains("status=200")
                .contains("elapsedMs=");
    }
}
