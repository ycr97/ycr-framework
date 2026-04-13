package com.ycr.framework.data.mp.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ycr.framework.data.annotation.Query;
import com.ycr.framework.data.enums.QueryType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryWrapperHelperTest {

    @Test
    void shouldBuildWrapperFromAnnotatedFields() {
        SampleQuery query = new SampleQuery();

        QueryWrapper<Object> wrapper = QueryWrapperHelper.build(query);

        String sqlSegment = wrapper.getSqlSegment();
        assertTrue(sqlSegment.contains("user_name"));
        assertTrue(sqlSegment.contains("LIKE"));
        assertTrue(sqlSegment.contains("age"));
        assertTrue(sqlSegment.contains("IN"));
        assertTrue(sqlSegment.contains("deleted_at"));
        assertTrue(sqlSegment.contains("IS NULL"));
    }

    @Test
    void shouldBuildBetweenCondition() {
        BetweenQuery query = new BetweenQuery();

        QueryWrapper<Object> wrapper = QueryWrapperHelper.build(query);

        String sqlSegment = wrapper.getSqlSegment();
        assertTrue(sqlSegment.contains("created_at"));
        assertTrue(sqlSegment.contains("BETWEEN"));
    }

    static class SampleQuery {
        @Query(column = "user_name", type = QueryType.LIKE)
        private final String userName = "alice";

        @Query(type = QueryType.IN)
        private final List<Long> ids = List.of(1L, 2L);

        @Query(type = QueryType.IS_NULL, column = "deleted_at")
        private final Object deletedAt = new Object();

        @Query(type = QueryType.GE)
        private final Integer age = 18;
    }

    static class BetweenQuery {
        @Query(type = QueryType.BETWEEN, column = "created_at")
        private final List<Integer> createdAt = List.of(1, 2);
    }
}
