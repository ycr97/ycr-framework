package com.ycr.framework.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class BizExceptionTest {

    @Test
    void 创建业务异常_应包含错误码和消息() {
        BizException exception = new BizException("USER_NOT_FOUND", "用户不存在");

        assertEquals("USER_NOT_FOUND", exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void 通过ErrorCode创建异常() {
        ErrorCode errorCode = new ErrorCode() {
            @Override
            public String getCode() {
                return "400";
            }

            @Override
            public String getMessage() {
                return "参数错误";
            }
        };

        BizException exception = new BizException(errorCode);

        assertEquals("400", exception.getCode());
        assertEquals("参数错误", exception.getMessage());
    }

    @Test
    void 系统异常_应包含原始异常() {
        RuntimeException cause = new RuntimeException("数据库连接失败");

        SysException exception = new SysException("DB_ERROR", "系统错误", cause);

        assertEquals("DB_ERROR", exception.getCode());
        assertSame(cause, exception.getCause());
    }
}
