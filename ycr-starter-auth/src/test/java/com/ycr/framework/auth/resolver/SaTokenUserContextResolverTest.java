package com.ycr.framework.auth.resolver;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.ycr.framework.auth.util.LoginHelper;
import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.enums.SecurityMode;
import com.ycr.framework.context.enums.UserContextSource;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.context.resolver.UserContextResolveRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SaTokenUserContextResolver 测试。
 *
 * @author ycr
 */
class SaTokenUserContextResolverTest {

    private final SaTokenUserContextResolver resolver = new SaTokenUserContextResolver();

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("tokenVerify模式应从SaToken会话还原上下文并忽略身份头")
    void shouldMatchExpectedBehavior001() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ContextHeaderConstants.HEADER_USER_ID, "999");

        SaSession session = mock(SaSession.class);
        when(session.getModel(LoginHelper.SESSION_KEY_USER_CONTEXT, UserContext.class)).thenReturn(user(1001L));

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getSession).thenReturn(session);

            UserContext userContext = resolver.resolve(new UserContextResolveRequest(
                    request,
                    SecurityMode.TOKEN_VERIFY,
                    "trace"));

            assertNotNull(userContext);
            assertEquals(1001L, userContext.getUserId());
            assertEquals(UserContextSource.TOKEN.name(), userContext.getSource());
        }
    }

    @Test
    @DisplayName("gatewayTrust模式不支持token解析")
    void shouldMatchExpectedBehavior002() {
        assertFalse(resolver.supports(new UserContextResolveRequest(
                new MockHttpServletRequest(),
                SecurityMode.GATEWAY_TRUST,
                "trace")));
    }

    private UserContext user(Long userId) {
        UserContext userContext = new UserContext();
        userContext.setUserId(userId);
        userContext.setUsername("admin");
        return userContext;
    }
}
