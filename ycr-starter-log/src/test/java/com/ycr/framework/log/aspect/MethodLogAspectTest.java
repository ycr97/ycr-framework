package com.ycr.framework.log.aspect;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.log.annotation.MethodLog;
import com.ycr.framework.log.autoconfigure.LogProperties;
import com.ycr.framework.log.enums.Level;
import com.ycr.framework.log.util.LogJsonSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MethodLogAspect 打印/级别门控/异常测试
 *
 * @author ycr
 */
class MethodLogAspectTest {

    private final LogJsonSupport jsonSupport =
            new LogJsonSupport(new ObjectMapper(), Set.of("password"));
    private Logger aspectLogger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        aspectLogger = (Logger) LoggerFactory.getLogger(MethodLogAspect.class);
        appender = new ListAppender<>();
        appender.start();
        aspectLogger.addAppender(appender);
        aspectLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        aspectLogger.detachAppender(appender);
    }

    private DemoService weave(LogProperties.Method config) {
        DemoService target = new DemoService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new MethodLogAspect(jsonSupport, config));
        return factory.getProxy();
    }

    private LogProperties.Method config(Level level) {
        LogProperties.Method c = new LogProperties.Method();
        c.setLevel(level);
        c.setMaxLength(2000);
        return c;
    }

    @Test
    void 应打印入参与出参() {
        DemoService proxy = weave(config(Level.DEBUG));
        assertEquals("ok:张三", proxy.create("张三"));

        boolean hasArgs = appender.list.stream().anyMatch(e -> e.getFormattedMessage().contains("入参"));
        boolean hasResult = appender.list.stream().anyMatch(e -> e.getFormattedMessage().contains("出参"));
        assertTrue(hasArgs, "应打印入参");
        assertTrue(hasResult, "应打印出参");
    }

    @Test
    void 级别未开启应跳过不打印() {
        aspectLogger.setLevel(ch.qos.logback.classic.Level.INFO);  // 高于配置的 DEBUG
        DemoService proxy = weave(config(Level.DEBUG));
        proxy.create("张三");
        assertTrue(appender.list.isEmpty(), "DEBUG 未开启时不应打印");
    }

    @Test
    void 异常应打印异常段并原样抛出() {
        DemoService proxy = weave(config(Level.DEBUG));
        assertThrows(IllegalStateException.class, proxy::boom);
        assertTrue(appender.list.stream().anyMatch(e -> e.getFormattedMessage().contains("异常")),
                "应打印异常段");
    }

    /** 测试目标 */
    public static class DemoService {

        @MethodLog("创建")
        public String create(String name) {
            return "ok:" + name;
        }

        @MethodLog("炸")
        public void boom() {
            throw new IllegalStateException("炸了");
        }
    }
}
