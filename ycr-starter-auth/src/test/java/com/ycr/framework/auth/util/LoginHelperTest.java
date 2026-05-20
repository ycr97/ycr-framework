package com.ycr.framework.auth.util;

import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginHelperTest {

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void 填充用户上下文() {
        UserContext ctx = new UserContext();
        ctx.setUserId(1001L);
        ctx.setUsername("admin");
        ctx.setRoles("ROLE_ADMIN");

        LoginHelper.setUserContext(ctx);

        assertEquals(1001L, UserContextHolder.getUserId());
        assertEquals("admin", UserContextHolder.getUsername());
    }

    @Test
    void 获取当前用户ID() {
        UserContext ctx = new UserContext();
        ctx.setUserId(2002L);
        LoginHelper.setUserContext(ctx);

        assertEquals(2002L, LoginHelper.getUserId());
    }
}
