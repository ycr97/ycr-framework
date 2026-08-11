package com.ycr.framework.data.permission.scope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopeThreadContextAccessorTest {

    private final DataScopeThreadContextAccessor accessor = new DataScopeThreadContextAccessor();

    @AfterEach
    void tearDown() {
        DataScopeContext.clear();
    }

    @Test
    @DisplayName("应捕获、恢复并清理数据范围缓存")
    void shouldCaptureRestoreAndClearDataScope() {
        DataScope expected = DataScope.builder().dimension("dept", List.of(10L)).build();
        DataScopeContext.get(() -> expected);

        Object captured = accessor.capture();
        DataScopeContext.clear();
        accessor.restore(captured);

        assertThat(DataScopeContext.capture()).isSameAs(expected);
        accessor.restore(null);
        assertThat(DataScopeContext.capture()).isNull();
    }
}
