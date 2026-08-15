package com.ycr.framework.auth.resolver;

import cn.dev33.satoken.config.SaTokenConfig;
import com.ycr.framework.auth.session.SaTokenSessionManager;
import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.enums.SecurityMode;
import com.ycr.framework.context.enums.UserContextSource;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.context.resolver.UserContextResolveRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SaTokenUserContextResolverTest {

    private final SaTokenSessionManager sessionManager = mock(SaTokenSessionManager.class);

    private final SaTokenUserContextResolver resolver = new SaTokenUserContextResolver(
            sessionManager,
            new SaTokenConfig().setTokenName("Authorization").setTokenPrefix("Bearer"));

    @Test
    @DisplayName("Bearer Token应恢复会话上下文且忽略裸身份头")
    void bearerTokenShouldRestoreSessionContextAndIgnoreIdentityHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-1");
        request.addHeader(ContextHeaderConstants.HEADER_USER_ID, "999");
        UserContext expected = user(1001L);
        when(sessionManager.findUserContext("token-1")).thenReturn(expected);

        UserContext actual = resolver.resolve(resolveRequest(request, SecurityMode.TOKEN_VERIFY));

        assertSame(expected, actual);
        assertEquals(1001L, actual.getUserId());
        assertEquals(UserContextSource.TOKEN.name(), actual.getSource());
    }

    @Test
    @DisplayName("错误Token前缀不应查询会话")
    void invalidTokenPrefixShouldNotQuerySession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic token-1");

        assertNull(resolver.resolve(resolveRequest(request, SecurityMode.TOKEN_VERIFY)));
        verify(sessionManager, never()).findUserContext("token-1");
    }

    @Test
    @DisplayName("网关信任模式不支持Token解析")
    void gatewayTrustModeShouldNotBeSupported() {
        assertFalse(resolver.supports(resolveRequest(new MockHttpServletRequest(), SecurityMode.GATEWAY_TRUST)));
    }

    private UserContextResolveRequest resolveRequest(MockHttpServletRequest request, SecurityMode securityMode) {
        return new UserContextResolveRequest(request, securityMode, "trace");
    }

    private UserContext user(Long userId) {
        UserContext userContext = new UserContext();
        userContext.setUserId(userId);
        userContext.setUsername("admin");
        return userContext;
    }
}
