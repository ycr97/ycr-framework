package com.ycr.framework.web.handler;

import com.ycr.framework.core.exception.BizException;
import com.ycr.framework.core.exception.SysException;
import com.ycr.framework.core.model.R;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

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
}
