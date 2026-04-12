package com.ycr.framework.web.handler;

import com.ycr.framework.core.exception.BizException;
import com.ycr.framework.core.exception.SysException;
import com.ycr.framework.core.model.R;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void 处理BizException_应返回业务错误码() {
        R<Void> response = handler.handleBizException(new BizException("USER_001", "用户不存在"));

        assertEquals(400, response.getCode());
        assertEquals("用户不存在", response.getMsg());
        assertFalse(response.isSuccess());
    }

    @Test
    void 处理SysException_应返回系统错误() {
        R<Void> response = handler.handleSysException(new SysException("SYS_001", "数据库异常"));

        assertEquals(500, response.getCode());
        assertEquals("数据库异常", response.getMsg());
        assertFalse(response.isSuccess());
    }

    @Test
    void 处理未知异常_应返回通用错误() {
        R<Void> response = handler.handleException(new RuntimeException("未知异常"));

        assertEquals(500, response.getCode());
    }
}
