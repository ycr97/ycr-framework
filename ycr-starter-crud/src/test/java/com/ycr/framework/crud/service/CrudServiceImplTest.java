package com.ycr.framework.crud.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ycr.framework.data.model.PageQuery;
import com.ycr.framework.data.model.PageResult;
import com.ycr.framework.data.mp.mapper.BaseMapperX;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * CrudServiceImpl 委托与分页转换测试
 *
 * @author ycr
 */
class CrudServiceImplTest {

    interface DemoMapper extends BaseMapperX<String> {
    }

    static class DemoService extends CrudServiceImpl<DemoMapper, String, Long> {
    }

    @SuppressWarnings("unchecked")
    private DemoService serviceWith(DemoMapper mapper) throws Exception {
        DemoService service = new DemoService();
        Field f = CrudServiceImpl.class.getDeclaredField("baseMapper");
        f.setAccessible(true);
        f.set(service, mapper);
        return service;
    }

    @Test
    @DisplayName("get_create_update_delete透传与布尔语义")
    void shouldMatchExpectedBehavior001() throws Exception {
        DemoMapper mapper = mock(DemoMapper.class);
        when(mapper.selectById(1L)).thenReturn("row");
        when(mapper.insert("a")).thenReturn(1);
        when(mapper.updateById("b")).thenReturn(0);
        when(mapper.deleteById(2L)).thenReturn(1);

        DemoService service = serviceWith(mapper);

        assertEquals("row", service.get(1L));
        assertTrue(service.create("a"));
        assertFalse(service.update("b"));   // 影响 0 行 -> false
        assertTrue(service.delete(2L));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("page_经selectPage并转PageResult")
    void shouldMatchExpectedBehavior002() throws Exception {
        DemoMapper mapper = mock(DemoMapper.class);
        Page<String> mpResult = new Page<>(2, 10, 30);
        mpResult.setRecords(List.of("x", "y"));
        when(mapper.selectPage(any(IPage.class), any())).thenReturn(mpResult);

        DemoService service = serviceWith(mapper);
        PageQuery pq = new PageQuery();
        pq.setPage(2);
        pq.setSize(10);

        PageResult<String> result = service.page(pq, new QueryWrapper<>());

        assertEquals(List.of("x", "y"), result.getList());
        assertEquals(30, result.getTotal());
        assertEquals(2, result.getPage());
    }

    @Test
    @DisplayName("list_透传wrapper")
    void shouldMatchExpectedBehavior003() throws Exception {
        DemoMapper mapper = mock(DemoMapper.class);
        QueryWrapper<String> wrapper = new QueryWrapper<>();
        List<String> rows = List.of("a", "b");
        when(mapper.selectList(wrapper)).thenReturn(rows);

        assertSame(rows, serviceWith(mapper).list(wrapper));
    }
}
