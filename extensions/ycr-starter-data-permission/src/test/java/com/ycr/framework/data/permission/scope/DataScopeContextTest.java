package com.ycr.framework.data.permission.scope;

import com.ycr.framework.data.permission.exception.DataPermissionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * DataScopeContext 请求级缓存与 fail-closed 测试
 *
 * @author ycr
 */
class DataScopeContextTest {

    @AfterEach
    void tearDown() {
        DataScopeContext.clear();
    }

    @Test
    @DisplayName("同请求内只解析一次")
    void shouldMatchExpectedBehavior001() {
        AtomicInteger calls = new AtomicInteger();
        DataScopeResolver resolver = () -> {
            calls.incrementAndGet();
            return DataScope.builder().dimension("factory", List.of(1)).build();
        };

        DataScopeContext.get(resolver);
        DataScopeContext.get(resolver);
        assertEquals(1, calls.get());
    }

    @Test
    @DisplayName("clear后重新解析")
    void shouldMatchExpectedBehavior002() {
        AtomicInteger calls = new AtomicInteger();
        DataScopeResolver resolver = () -> {
            calls.incrementAndGet();
            return DataScope.empty();
        };

        DataScopeContext.get(resolver);
        DataScopeContext.clear();
        DataScopeContext.get(resolver);
        assertEquals(2, calls.get());
    }

    @Test
    @DisplayName("resolver异常_包装为DataPermissionException")
    void shouldMatchExpectedBehavior003() {
        DataScopeResolver resolver = () -> {
            throw new IllegalStateException("远程取数失败");
        };
        assertThrows(DataPermissionException.class, () -> DataScopeContext.get(resolver));
    }
}
