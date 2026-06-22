package com.ycr.framework.data.mp.handler;

import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.data.mp.handler.support.AuditEntity;
import com.ycr.framework.data.mp.handler.support.AuditMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = AutoFillMetaObjectHandlerIntegrationTest.TestApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:audit_fill;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.sql.init.mode=always",
                "spring.sql.init.schema-locations=classpath:audit-schema.sql",
                "spring.autoconfigure.exclude=com.ycr.framework.context.autoconfigure.ContextAutoConfiguration"
        })
class AutoFillMetaObjectHandlerIntegrationTest {

    @Autowired
    private AuditMapper auditMapper;

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
        auditMapper.delete(null);
    }

    @Test
    void insertAndUpdateShouldPersistAuditFields() throws Exception {
        setUser(1001L);
        AuditEntity entity = new AuditEntity();
        entity.setId(1L);
        entity.setName("before");

        auditMapper.insert(entity);
        AuditEntity inserted = auditMapper.selectById(1L);

        assertNotNull(inserted.getCreateTime());
        assertNotNull(inserted.getUpdateTime());
        assertEquals(1001L, inserted.getCreateUser());
        assertEquals(1001L, inserted.getUpdateUser());

        LocalDateTime firstUpdateTime = inserted.getUpdateTime();
        Thread.sleep(5L);
        setUser(1002L);
        inserted.setName("after");
        auditMapper.updateById(inserted);

        AuditEntity updated = auditMapper.selectById(1L);
        assertEquals(1001L, updated.getCreateUser());
        assertEquals(1002L, updated.getUpdateUser());
        assertTrue(updated.getUpdateTime().isAfter(firstUpdateTime));
    }

    private void setUser(Long userId) {
        UserContext context = new UserContext();
        context.setUserId(userId);
        UserContextHolder.set(context);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @MapperScan(basePackageClasses = AuditMapper.class)
    @Import(com.ycr.framework.data.mp.autoconfigure.MybatisPlusAutoConfiguration.class)
    static class TestApplication {
    }
}
