package com.ycr.framework.protect.xss;

import com.ycr.framework.protect.xss.enums.XssMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * XSS 清理工具测试。
 *
 * @author ycr
 */
class XssCleanerTest {

    @Test
    void escape_转义危险字符() {
        assertEquals("&lt;script&gt;alert(1)&lt;&#x2F;script&gt;",
                XssCleaner.escape("<script>alert(1)</script>"));
        assertEquals("a&amp;b&quot;c&#x27;d", XssCleaner.escape("a&b\"c'd"));
    }

    @Test
    void clean_移除脚本块与标签与事件与危险协议() {
        assertEquals("hello", XssCleaner.clean("<script>alert(1)</script>hello"));
        assertEquals("hi", XssCleaner.clean("<b onclick=\"x()\">hi</b>"));
        assertEquals("", XssCleaner.clean("<img src=x onerror=alert(1)>"));

        String cleaned = XssCleaner.clean("<a href=\"javascript:alert(1)\">link</a>");
        assertFalse(cleaned.toLowerCase().contains("javascript:"), "应移除 javascript: 协议");
        assertFalse(cleaned.contains("<"), "应移除所有标签");
    }

    @Test
    void sanitize_按模式分派_null安全() {
        assertNull(XssCleaner.sanitize(null, XssMode.ESCAPE));
        assertEquals("&lt;b&gt;", XssCleaner.sanitize("<b>", XssMode.ESCAPE));
        assertEquals("", XssCleaner.sanitize("<b></b>", XssMode.CLEAN));
    }
}
