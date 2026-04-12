package com.ycr.framework.data.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageQueryTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 默认分页参数应为第一页十条() {
        PageQuery pageQuery = new PageQuery();

        assertEquals(1, pageQuery.getPage());
        assertEquals(10, pageQuery.getSize());
    }

    @Test
    void 自定义分页参数应正确保存() {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPage(3);
        pageQuery.setSize(20);

        assertEquals(3, pageQuery.getPage());
        assertEquals(20, pageQuery.getSize());
    }

    @Test
    void PageResult应保存列表和分页信息() {
        PageResult<String> pageResult = new PageResult<>(List.of("a", "b"), 2L, 1, 10);

        assertEquals(List.of("a", "b"), pageResult.getList());
        assertEquals(2L, pageResult.getTotal());
        assertEquals(1, pageResult.getPage());
        assertEquals(10, pageResult.getSize());
    }

    @Test
    void 分页参数应受最小值和最大值校验约束() {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPage(0);
        pageQuery.setSize(1001);

        assertEquals(2, validator.validate(pageQuery).size());
        assertTrue(validator.validate(pageQuery).stream().anyMatch(v -> "页码最小为1".equals(v.getMessage())));
        assertTrue(validator.validate(pageQuery).stream().anyMatch(v -> "每页条数最大为1000".equals(v.getMessage())));
    }
}
