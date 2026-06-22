package com.ycr.framework.protect.xss.filter;

import com.ycr.framework.protect.xss.enums.XssMode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * XSS 请求包装器测试：参数按模式清理，请求头保持原值。
 *
 * @author ycr
 */
class XssRequestWrapperTest {

    private final HttpServletRequest delegate = mock(HttpServletRequest.class);

    @Test
    void getParameter_ESCAPE模式转义() {
        when(delegate.getParameter("q")).thenReturn("<script>x</script>");
        XssRequestWrapper wrapper = new XssRequestWrapper(delegate, XssMode.ESCAPE);
        assertEquals("&lt;script&gt;x&lt;&#x2F;script&gt;", wrapper.getParameter("q"));
    }

    @Test
    void getParameter_CLEAN模式去标签() {
        when(delegate.getParameter("q")).thenReturn("<script>x</script>safe");
        XssRequestWrapper wrapper = new XssRequestWrapper(delegate, XssMode.CLEAN);
        assertEquals("safe", wrapper.getParameter("q"));
    }

    @Test
    void getParameterValues_逐个清理() {
        when(delegate.getParameterValues("a")).thenReturn(new String[]{"<b>", "ok"});
        XssRequestWrapper wrapper = new XssRequestWrapper(delegate, XssMode.ESCAPE);
        assertArrayEquals(new String[]{"&lt;b&gt;", "ok"}, wrapper.getParameterValues("a"));
    }

    @ParameterizedTest
    @CsvSource({
            "Authorization, 'Bearer abc/def+ghi=='",
            "Content-Type, 'application/json;charset=UTF-8'",
            "X-Context-Signature, 'sha256/abc+def=='",
            "X-Trace-Id, 'trace/001'"
    })
    void 协议敏感请求头必须保持原值(String name, String value) {
        when(delegate.getHeader(name)).thenReturn(value);
        XssRequestWrapper wrapper = new XssRequestWrapper(delegate, XssMode.ESCAPE);
        assertEquals(value, wrapper.getHeader(name));
    }
}
