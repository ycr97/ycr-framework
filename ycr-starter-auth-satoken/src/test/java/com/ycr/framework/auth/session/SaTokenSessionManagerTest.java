package com.ycr.framework.auth.session;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SaTokenSessionManagerTest {

    private final SaTokenSessionManager sessionManager = new SaTokenSessionManager();

    @AfterEach
    void clearUserContext() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("登录应建立会话、保存用户上下文并返回Token信息")
    void loginShouldCreateSessionAndReturnTokenInfo() {
        SaSession session = mock(SaSession.class);
        SaTokenInfo tokenInfo = mock(SaTokenInfo.class);
        StpLogic stpLogic = mock(StpLogic.class);
        SaLoginParameter loginParameter = new SaLoginParameter();
        when(tokenInfo.getTokenValue()).thenReturn("token-1");
        when(stpLogic.getTokenSessionByToken("token-1", true)).thenReturn(session);
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getTokenInfo).thenReturn(tokenInfo);
            stpUtil.when(StpUtil::getStpLogic).thenReturn(stpLogic);

            SaTokenInfo actual = sessionManager.login(user(), loginParameter);

            stpUtil.verify(() -> StpUtil.login(1001L, loginParameter));
            verify(session).set(SaTokenSessionManager.SESSION_KEY_USER_CONTEXT, user());
            assertSame(tokenInfo, actual);
            assertEquals(1001L, UserContextHolder.getUserId());
        }
    }

    @Test
    @DisplayName("登录参数不完整时应拒绝建立会话")
    void loginShouldRejectInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login(null));
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login(new UserContext()));
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login(user(), null));
    }

    @Test
    @DisplayName("有效Token应从账号会话恢复用户上下文")
    void findUserContextShouldRestoreContextFromAccountSession() {
        StpLogic stpLogic = mock(StpLogic.class);
        SaSession session = mock(SaSession.class);
        UserContext expected = user();
        when(stpLogic.getLoginIdByToken("token-1")).thenReturn(1001L);
        when(stpLogic.getTokenSessionByToken("token-1", false)).thenReturn(session);
        when(session.getModel(SaTokenSessionManager.SESSION_KEY_USER_CONTEXT, UserContext.class))
                .thenReturn(expected);
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getStpLogic).thenReturn(stpLogic);

            assertEquals(expected, sessionManager.findUserContext("token-1"));
        }
    }

    @Test
    @DisplayName("无效Token不应创建账号会话")
    void findUserContextShouldNotCreateSessionForInvalidToken() {
        StpLogic stpLogic = mock(StpLogic.class);
        when(stpLogic.getLoginIdByToken("invalid")).thenReturn(null);
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getStpLogic).thenReturn(stpLogic);

            assertNull(sessionManager.findUserContext("invalid"));
        }
    }

    @Test
    @DisplayName("注销异常时仍应清理线程用户上下文")
    void logoutShouldClearContextWhenSaTokenFails() {
        UserContextHolder.set(user());
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::logout).thenThrow(new RuntimeException("logout failed"));

            assertThrows(RuntimeException.class, sessionManager::logout);
            assertNull(UserContextHolder.get());
        }
    }

    private UserContext user() {
        UserContext userContext = new UserContext();
        userContext.setUserId(1001L);
        userContext.setUsername("admin");
        userContext.setRoles(Set.of("admin"));
        return userContext;
    }
}
