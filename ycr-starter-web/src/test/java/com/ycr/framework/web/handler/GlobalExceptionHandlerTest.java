package com.ycr.framework.web.handler;

import com.ycr.framework.core.exception.BizException;
import com.ycr.framework.core.exception.SysException;
import com.ycr.framework.core.model.R;
import com.ycr.framework.security.exception.AuthException;
import com.ycr.framework.security.exception.ForbiddenException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void 处理BizException_应返回HTTP400与业务错误码() {
        ResponseEntity<R<Void>> response = handler.handleBizException(new BizException("USER_001", "用户不存在"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("USER_001", response.getBody().getCode());
        assertEquals("用户不存在", response.getBody().getMsg());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void 处理带自定义HTTP状态的BizException_状态应跟随() {
        ResponseEntity<R<Void>> response = handler.handleBizException(new TooManyRequests());

        assertEquals(429, response.getStatusCode().value());
        assertEquals("429", response.getBody().getCode());
    }

    @Test
    void 处理AuthException_应返回HTTP401() {
        ResponseEntity<R<Void>> response = handler.handleBizException(new AuthException());

        assertEquals(401, response.getStatusCode().value());
        assertEquals("AUTH_UNAUTHORIZED", response.getBody().getCode());
    }

    @Test
    void 处理ForbiddenException_应返回HTTP403() {
        ResponseEntity<R<Void>> response = handler.handleBizException(new ForbiddenException());

        assertEquals(403, response.getStatusCode().value());
        assertEquals("AUTH_FORBIDDEN", response.getBody().getCode());
    }

    /** 模拟限流类异常：携带 HTTP 429（验证 handler 动态状态路径，不跨依赖引 ratelimiter 模块） */
    static class TooManyRequests extends BizException {
        TooManyRequests() {
            super(429, "429", "操作过于频繁");
        }
    }

    @Test
    void 处理SysException_应返回系统错误() {
        R<Void> response = handler.handleSysException(new SysException("SYS_001", "数据库异常"));

        assertEquals("500", response.getCode());
        assertEquals("数据库异常", response.getMsg());
        assertFalse(response.isSuccess());
    }

    @Test
    void 处理未知异常_应返回通用错误() {
        R<Void> response = handler.handleException(new RuntimeException("未知异常"));

        assertEquals("500", response.getCode());
    }

    @Test
    void 业务异常日志应包含稳定事件字段(CapturedOutput output) {
        MDC.put("traceId", "trace-001");
        MDC.put("userId", "1001");

        handler.handleBizException(new BizException("USER_001", "用户不存在"));

        assertThat(output).contains("event=biz_exception")
                .contains("traceId=trace-001")
                .contains("userId=1001")
                .contains("code=USER_001");
    }
}
