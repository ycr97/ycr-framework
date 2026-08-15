package com.ycr.framework.log.model;

import com.ycr.framework.log.autoconfigure.LogProperties;
import com.ycr.framework.log.enums.Include;
import com.ycr.framework.log.handler.IpRegionResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 采集模型契约测试：新 Include 值、LogRecord 新字段、IpRegionResolver SPI
 *
 * @author ycr
 */
class LogModelTest {

    @Test
    @DisplayName("Include应包含新增采集项")
    void shouldMatchExpectedBehavior001() {
        // valueOf 不抛即存在
        Include.valueOf("REQUEST_BODY");
        Include.valueOf("RESPONSE_BODY");
        Include.valueOf("REQUEST_HEADERS");
        Include.valueOf("BROWSER");
        Include.valueOf("OS");
        Include.valueOf("IP_REGION");
    }

    @Test
    @DisplayName("LogRecord新字段应可读写")
    void shouldMatchExpectedBehavior002() {
        LogRecord r = new LogRecord();
        r.setRequestBody("rb");
        r.setResponseBody("resp");
        r.setRequestHeaders("h");
        r.setBrowser("Chrome");
        r.setOs("Windows");
        r.setIpRegion("中国-浙江");
        assertEquals("rb", r.getRequestBody());
        assertEquals("resp", r.getResponseBody());
        assertEquals("h", r.getRequestHeaders());
        assertEquals("Chrome", r.getBrowser());
        assertEquals("Windows", r.getOs());
        assertEquals("中国-浙江", r.getIpRegion());
    }

    @Test
    @DisplayName("IpRegionResolver应可实现")
    void shouldMatchExpectedBehavior003() {
        IpRegionResolver r = ip -> "中国-浙江-杭州";
        assertEquals("中国-浙江-杭州", r.resolve("1.2.3.4"));
    }

    @Test
    @DisplayName("maxBodyLength默认2000")
    void shouldMatchExpectedBehavior004() {
        assertEquals(2000, new LogProperties().getMaxBodyLength());
    }
}
