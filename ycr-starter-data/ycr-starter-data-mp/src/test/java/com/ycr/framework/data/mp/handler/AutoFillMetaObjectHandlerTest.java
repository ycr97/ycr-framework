package com.ycr.framework.data.mp.handler;

import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.data.model.BaseDO;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AutoFillMetaObjectHandlerTest {

    private final AutoFillMetaObjectHandler handler = new AutoFillMetaObjectHandler();

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void insertFillShouldPopulateCreateAndUpdateFields() {
        UserContext userContext = new UserContext();
        userContext.setUserId(1001L);
        userContext.setUsername("tester");
        UserContextHolder.set(userContext);
        TestEntity entity = new TestEntity();

        handler.insertFill(SystemMetaObject.forObject(entity));

        assertNotNull(entity.getCreateTime());
        assertNotNull(entity.getUpdateTime());
        assertEquals(1001L, entity.getCreateUser());
        assertEquals(1001L, entity.getUpdateUser());
    }

    @Test
    void updateFillShouldOnlyPopulateUpdateFields() {
        UserContext userContext = new UserContext();
        userContext.setUserId(1002L);
        userContext.setUsername("tester");
        UserContextHolder.set(userContext);
        TestEntity entity = new TestEntity();
        LocalDateTime createTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        entity.setCreateTime(createTime);
        entity.setCreateUser(2001L);

        handler.updateFill(SystemMetaObject.forObject(entity));

        assertEquals(createTime, entity.getCreateTime());
        assertEquals(2001L, entity.getCreateUser());
        assertNotNull(entity.getUpdateTime());
        assertEquals(1002L, entity.getUpdateUser());
    }

    static class TestEntity extends BaseDO {
    }
}
