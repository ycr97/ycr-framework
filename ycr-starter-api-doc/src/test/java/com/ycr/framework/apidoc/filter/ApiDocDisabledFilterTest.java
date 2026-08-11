package com.ycr.framework.apidoc.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ApiDocDisabledFilterTest {

    private final ApiDocDisabledFilter filter = new ApiDocDisabledFilter();

    @Test
    @DisplayName("文档入口应被阻断")
    void documentationEntryShouldBeBlocked() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/doc.html");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    @DisplayName("非文档WebJar资源不应被总开关误伤")
    void unrelatedWebJarShouldContinueThroughFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/webjars/example/app.js");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
    }
}
