package com.ycr.framework.auth.util;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 登录辅助工具测试
 *
 * <p>SaToken 的 {@link StpUtil} 依赖运行期上下文，单测中无 Web 环境，故用 Mockito 静态打桩，
 * 聚焦验证 LoginHelper 自身的「登录联动会话与线程上下文」「懒加载还原」「登出清理」逻辑。</p>
 */
class LoginHelperTest {

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    private UserContext newUser() {
        UserContext ctx = new UserContext();
        ctx.setUserId(1001L);
        ctx.setUsername("admin");
        ctx.setRoles(Set.of("ROLE_ADMIN"));
        return ctx;
    }

    @Test
    void 登录应签发token并写入会话与线程上下文() {
        SaSession session = mock(SaSession.class);
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getSession).thenReturn(session);

            LoginHelper.login(newUser());

            // 以 userId 登录
            stp.verify(() -> StpUtil.login(1001L));
            // 完整上下文写入会话
            verify(session).set(eq(LoginHelper.SESSION_KEY_USER_CONTEXT), any(UserContext.class));
            // 当前线程已填充
            assertEquals(1001L, UserContextHolder.getUserId());
            assertEquals("admin", UserContextHolder.getUsername());
        }
    }

    @Test
    void 登录上下文为空应抛异常() {
        assertThrows(IllegalArgumentException.class, () -> LoginHelper.login(null));
        UserContext noId = new UserContext();
        assertThrows(IllegalArgumentException.class, () -> LoginHelper.login(noId));
    }

    @Test
    void 线程已有上下文时获取无需触碰SaToken() {
        LoginHelper.setUserContext(newUser());
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            UserContext ctx = LoginHelper.getUserContext();

            assertEquals(1001L, ctx.getUserId());
            // 线程内已有，不应调用 isLogin
            stp.verify(StpUtil::isLogin, never());
        }
    }

    @Test
    void 线程无上下文且已登录时应从会话懒加载还原() {
        SaSession session = mock(SaSession.class);
        when(session.getModel(LoginHelper.SESSION_KEY_USER_CONTEXT, UserContext.class)).thenReturn(newUser());
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getSession).thenReturn(session);

            UserContext ctx = LoginHelper.getUserContext();

            assertNotNull(ctx, "已登录应能从会话还原上下文");
            assertEquals(1001L, ctx.getUserId());
            // 还原后应回填线程，便于后续重复读取
            assertEquals(1001L, UserContextHolder.getUserId());
        }
    }

    @Test
    void 未登录时获取上下文应为空() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(false);

            assertNull(LoginHelper.getUserContext());
            assertNull(LoginHelper.getUserId());
        }
    }

    @Test
    void 登出应注销并清理线程上下文() {
        LoginHelper.setUserContext(newUser());
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            LoginHelper.logout();

            stp.verify(StpUtil::logout);
            assertNull(UserContextHolder.get(), "登出后线程上下文应被清理");
        }
    }

    @Test
    void 登出时SaToken异常也应清理线程上下文() {
        LoginHelper.setUserContext(newUser());
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::logout).thenThrow(new RuntimeException("注销异常"));

            assertThrows(RuntimeException.class, LoginHelper::logout);
            assertNull(UserContextHolder.get(), "异常路径下线程上下文也必须被清理");
        }
    }
}
