package com.ycr.framework.log.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.log.annotation.Log;
import com.ycr.framework.log.autoconfigure.LogProperties;
import com.ycr.framework.log.enums.Include;
import com.ycr.framework.log.handler.IpRegionResolver;
import com.ycr.framework.log.handler.LogHandler;
import com.ycr.framework.log.model.LogRecord;
import com.ycr.framework.log.util.LogJsonSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;
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

    private final LogJsonSupport jsonSupport =
            new LogJsonSupport(new ObjectMapper(), new LogProperties().getSensitiveKeys());
    private final IpRegionResolver noopRegion = ip -> null;

    /** 用真实切面织入目标对象 */
    private DemoService weave(LogHandler handler, LogProperties properties) {
        return weave(handler, properties, noopRegion);
    }

    private DemoService weave(LogHandler handler, LogProperties properties, IpRegionResolver region) {
        DemoService target = new DemoService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new LogAspect(handler, properties, null, jsonSupport, region));
        return factory.getProxy();
    }

    /** 用真实切面织入类级 @Log 目标对象 */
    private ClassLevelDemoService weaveClassLevel(LogHandler handler, LogProperties properties) {
        ClassLevelDemoService target = new ClassLevelDemoService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new LogAspect(handler, properties, null, jsonSupport, noopRegion));
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

    @Test
    void 应采集请求体响应体并脱敏() {
        LogHandler handler = mock(LogHandler.class);
        DemoService proxy = weave(handler, new LogProperties());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/users");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        proxy.save(new DemoService.UserReq("张三", "secret123"));

        ArgumentCaptor<LogRecord> captor = ArgumentCaptor.forClass(LogRecord.class);
        verify(handler).handle(captor.capture());
        LogRecord r = captor.getValue();
        assertTrue(r.getRequestBody().contains("张三"), r.getRequestBody());
        assertTrue(r.getRequestBody().contains("******"), "密码应脱敏");
        assertFalse(r.getRequestBody().contains("secret123"));
        assertTrue(r.getResponseBody().contains("张三"), r.getResponseBody());
    }

    @Test
    void 应采集请求头并强制脱敏Authorization() {
        LogHandler handler = mock(LogHandler.class);
        DemoService proxy = weave(handler, new LogProperties());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-xyz");
        request.addHeader("X-Trace", "t1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        proxy.save(new DemoService.UserReq("张三", "p"));

        ArgumentCaptor<LogRecord> captor = ArgumentCaptor.forClass(LogRecord.class);
        verify(handler).handle(captor.capture());
        String headers = captor.getValue().getRequestHeaders();
        assertTrue(headers.contains("X-Trace=t1"), headers);
        assertTrue(headers.contains("Authorization=******"), headers);
        assertFalse(headers.contains("token-xyz"));
    }

    @Test
    void 应解析UA浏览器与操作系统() {
        LogHandler handler = mock(LogHandler.class);
        DemoService proxy = weave(handler, new LogProperties());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        proxy.save(new DemoService.UserReq("张三", "p"));

        ArgumentCaptor<LogRecord> captor = ArgumentCaptor.forClass(LogRecord.class);
        verify(handler).handle(captor.capture());
        assertTrue(captor.getValue().getBrowser().contains("Chrome"), captor.getValue().getBrowser());
        assertTrue(captor.getValue().getOs().contains("Windows"), captor.getValue().getOs());
    }

    @Test
    void 应经IpRegionResolver填充归属地() {
        LogHandler handler = mock(LogHandler.class);
        DemoService proxy = weave(handler, new LogProperties(), ip -> "中国-浙江-杭州");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        proxy.save(new DemoService.UserReq("张三", "p"));

        ArgumentCaptor<LogRecord> captor = ArgumentCaptor.forClass(LogRecord.class);
        verify(handler).handle(captor.capture());
        assertEquals("中国-浙江-杭州", captor.getValue().getIpRegion());
    }

    @Test
    void 类级Log应记录未标注方法() {
        LogHandler handler = mock(LogHandler.class);
        ClassLevelDemoService proxy = weaveClassLevel(handler, new LogProperties());

        assertEquals("listed", proxy.list());

        ArgumentCaptor<LogRecord> captor = ArgumentCaptor.forClass(LogRecord.class);
        verify(handler).handle(captor.capture());
        LogRecord record = captor.getValue();
        assertEquals("list", record.getDescription());
        assertEquals("类级模块", record.getModule());
        assertEquals("list", record.getMethodName());
    }

    @Test
    void 类级Log下方法级Log应优先() {
        LogHandler handler = mock(LogHandler.class);
        ClassLevelDemoService proxy = weaveClassLevel(handler, new LogProperties());

        assertEquals("detail", proxy.detail());

        ArgumentCaptor<LogRecord> captor = ArgumentCaptor.forClass(LogRecord.class);
        verify(handler).handle(captor.capture());
        LogRecord record = captor.getValue();
        assertEquals("查看详情", record.getDescription());
        assertEquals("方法级模块", record.getModule());
    }

    /** 测试目标：方法级 @Log 标注 */
    public static class DemoService {

        @Log(value = "创建用户", module = "用户管理", includes = {Include.REQUEST_PARAMS})
        public String create(String name) {
            return "ok:" + name;
        }

        @Log(value = "保存", module = "用户管理",
                includes = {Include.REQUEST_BODY, Include.RESPONSE_BODY, Include.REQUEST_HEADERS,
                        Include.BROWSER, Include.OS, Include.IP_REGION})
        public UserResp save(@RequestBody UserReq req) {
            return new UserResp(req.getName());
        }

        @Log("会抛异常")
        public void boom() {
            throw new IllegalStateException("炸了");
        }

        @Log(value = "忽略", ignore = true)
        public String ignored() {
            return "x";
        }

        public record UserReq(String name, String password) {
            public String getName() { return name; }
            public String getPassword() { return password; }
        }

        public record UserResp(String name) {
            public String getName() { return name; }
        }
    }

    @Log(module = "类级模块")
    public static class ClassLevelDemoService {

        public String list() {
            return "listed";
        }

        @Log(value = "查看详情", module = "方法级模块")
        public String detail() {
            return "detail";
        }
    }
}
