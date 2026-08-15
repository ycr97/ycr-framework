package com.ycr.framework.data.permission.scope;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DataScope 维度语义测试
 *
 * @author ycr
 */
class DataScopeTest {

    @Test
    @DisplayName("缺键维度不适用_存在维度可取值")
    void shouldMatchExpectedBehavior001() {
        DataScope scope = DataScope.builder()
                .dimension("factory", List.of(1, 2))
                .dimension("brand", List.of())
                .build();

        assertTrue(scope.has("factory"));
        assertEquals(List.of(1, 2), scope.values("factory"));

        assertTrue(scope.has("brand"));               // 适用
        assertTrue(scope.values("brand").isEmpty());  // 但空

        assertFalse(scope.has("region"));             // 缺键=不适用
        assertTrue(scope.values("region").isEmpty());
    }

    @Test
    @DisplayName("empty_无任何维度")
    void shouldMatchExpectedBehavior002() {
        DataScope scope = DataScope.empty();
        assertFalse(scope.has("factory"));
    }
}
