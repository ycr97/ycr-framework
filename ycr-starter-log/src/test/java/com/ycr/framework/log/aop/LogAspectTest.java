package com.ycr.framework.log.aop;

import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.log.annotation.Log;
import com.ycr.framework.log.autoconfigure.LogProperties;
import com.ycr.framework.log.enums.Include;
import com.ycr.framework.log.handler.LogHandler;
import com.ycr.framework.log.model.LogRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * LogAspect 真实 AspectJ 织入行为测试
 *
 * @author ycr
 */
class LogAspectTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    /** 用真实切面织入目标对象 */
    private DemoService weave(LogHandler handler, LogProperties properties) {
        DemoService target = new DemoService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new LogAspect(handler, properties, null));
        return factory.getProxy();
    }

    @Test
    void 正常路径应填充方法信息与状态() {
        LogHandler handler = mock(LogHandler.class);
        DemoService proxy = weave(handler, new LogProperties());

        String result = proxy.create("张三");

        assertEquals("ok:张三", result);
        ArgumentCaptor<LogRecord> captor = ArgumentCaptor.forClass(LogRecord.class);
        verify(handler).handle(captor.capture());
        LogRecord record = captor.getValue();
        assertEquals("创建用户", record.getDescription());
        assertEquals("用户管理", record.getModule());
        assertEquals("create", record.getMethodName());
        assertTrue(record.getClassName().contains("DemoService"));
        assertEquals(200, record.getStatus());
        assertNotNull(record.getOperateTime());
        assertTrue(record.getElapsedTime() >= 0);
    }

    @Test
    void 应从UserContext填充操作人() {
        LogHandler handler = mock(LogHandler.class);
        DemoService proxy = weave(handler, new LogProperties());

        try (MockedStatic<UserContextHolder> ms = mockStatic(UserContextHolder.class)) {
            ms.when(UserContextHolder::get).thenReturn(mock(UserContext.class));
            ms.when(UserContextHolder::getUserId).thenReturn(1001L);
            ms.when(UserContextHolder::getUsername).thenReturn("张三");

            proxy.create("x");

            ArgumentCaptor<LogRecord> captor = ArgumentCaptor.forClass(LogRecord.class);
            verify(handler).handle(captor.capture());
            assertEquals(1001L, captor.getValue().getOperatorId());
            assertEquals("张三", captor.getValue().getOperatorName());
        }
    }

    @Test
    void 异常路径应置500并原样抛出() {
        LogHandler handler = mock(LogHandler.class);
        DemoService proxy = weave(handler, new LogProperties());

        IllegalStateException ex = assertThrows(IllegalStateException.class, proxy::boom);
        assertEquals("炸了", ex.getMessage());

        ArgumentCaptor<LogRecord> captor = ArgumentCaptor.forClass(LogRecord.class);
        verify(handler).handle(captor.capture());
        assertEquals(500, captor.getValue().getStatus());
        assertEquals("炸了", captor.getValue().getErrorMsg());
    }

    @Test
    void ignore方法应跳过记录() {
        LogHandler handler = mock(LogHandler.class);
        DemoService proxy = weave(handler, new LogProperties());

        assertEquals("x", proxy.ignored());

        verify(handler, never()).handle(any());
    }

    @Test
    void 处理器异常不应影响业务返回() {
        LogHandler handler = mock(LogHandler.class);
        doThrow(new RuntimeException("handler 坏了")).when(handler).handle(any());
        DemoService proxy = weave(handler, new LogProperties());

        assertEquals("ok:李四", proxy.create("李四"));
    }

    @Test
    void 应采集请求参数且脱敏敏感键() {
        LogHandler handler = mock(LogHandler.class);
        DemoService proxy = weave(handler, new LogProperties());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/users");
        request.addParameter("name", "张三");
        request.addParameter("password", "secret123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        proxy.create("张三");

        ArgumentCaptor<LogRecord> captor = ArgumentCaptor.forClass(LogRecord.class);
        verify(handler).handle(captor.capture());
        LogRecord record = captor.getValue();
        assertEquals("POST", record.getRequestMethod());
        assertEquals("/api/users", record.getRequestUrl());
        String params = record.getRequestParams();
        assertTrue(params.contains("name=张三"), "普通参数应保留");
        assertTrue(params.contains("password=******"), "敏感参数应脱敏");
        assertFalse(params.contains("secret123"), "敏感明文不应出现");
    }

    /** 测试目标：方法级 @Log 标注 */
    public static class DemoService {

        @Log(value = "创建用户", module = "用户管理", includes = {Include.REQUEST_PARAMS})
        public String create(String name) {
            return "ok:" + name;
        }

        @Log("会抛异常")
        public void boom() {
            throw new IllegalStateException("炸了");
        }

        @Log(value = "忽略", ignore = true)
        public String ignored() {
            return "x";
        }
    }
}
