package com.ycr.framework.data.permission.scope;

import com.ycr.framework.data.permission.exception.DataPermissionException;
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
    void 同请求内只解析一次() {
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
    void clear后重新解析() {
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
    void resolver异常_包装为DataPermissionException() {
        DataScopeResolver resolver = () -> {
            throw new IllegalStateException("远程取数失败");
        };
        assertThrows(DataPermissionException.class, () -> DataScopeContext.get(resolver));
    }
}
