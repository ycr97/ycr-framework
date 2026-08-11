package com.ycr.framework.context.holder;

import com.ycr.framework.context.model.UserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserContextHolderTest {

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("设置和获取用户上下文")
    void shouldMatchExpectedBehavior001() {
        UserContext ctx = new UserContext();
        ctx.setUserId(1001L);
        ctx.setUsername("张三");
        ctx.setRoles(Set.of("admin", "user"));

        UserContextHolder.set(ctx);

        UserContext result = UserContextHolder.get();
        assertEquals(1001L, result.getUserId());
        assertEquals("张三", result.getUsername());
        assertEquals(Set.of("admin", "user"), result.getRoles());
    }

    @Test
    @DisplayName("清除上下文后应返回null")
    void shouldMatchExpectedBehavior002() {
        UserContext ctx = new UserContext();
        ctx.setUserId(1001L);
        UserContextHolder.set(ctx);
        UserContextHolder.clear();

        assertNull(UserContextHolder.get());
    }

}
