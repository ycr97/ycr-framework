package com.ycr.framework.crud.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ycr.framework.core.model.R;
import com.ycr.framework.data.annotation.Query;
import com.ycr.framework.data.enums.QueryType;
import com.ycr.framework.data.model.BaseDO;
import com.ycr.framework.data.model.PageQuery;
import com.ycr.framework.data.model.PageResult;
import com.ycr.framework.crud.service.CrudService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AbstractCrudController 端点行为测试：R 包装、服务委托、Query→Wrapper 构建
 *
 * @author ycr
 */
class AbstractCrudControllerTest {

    static class Demo extends BaseDO {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    static class DemoQuery {
        @Query(type = QueryType.LIKE)
        private String name;
        DemoQuery(String name) { this.name = name; }
    }

    static class DemoController extends AbstractCrudController<Demo, Long, DemoQuery> {
    }

    @SuppressWarnings("unchecked")
    private DemoController controllerWith(CrudService<Demo, Long> service) throws Exception {
        DemoController controller = new DemoController();
        Field f = AbstractCrudController.class.getDeclaredField("crudService");
        f.setAccessible(true);
        f.set(controller, service);
        return controller;
    }

    @Test
    @SuppressWarnings("unchecked")
    void page_构建wrapper并R包装() throws Exception {
        CrudService<Demo, Long> service = mock(CrudService.class);
        PageResult<Demo> pr = new PageResult<>(List.of(new Demo()), 1, 1, 10);
        when(service.page(any(), any())).thenReturn(pr);

        DemoController controller = controllerWith(service);
        R<PageResult<Demo>> r = controller.page(new DemoQuery("张"), new PageQuery());

        assertEquals("200", r.getCode());
        assertTrue(r.isSuccess());
        assertEquals(pr, r.getData());
        // 验证 wrapper 由 @Query(LIKE name) 构建（含 like 片段）
        ArgumentCaptor<QueryWrapper<Demo>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(service).page(any(), captor.capture());
        assertTrue(captor.getValue().getSqlSegment().toLowerCase().contains("like"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void get_委托服务并R包装() throws Exception {
        CrudService<Demo, Long> service = mock(CrudService.class);
        Demo demo = new Demo();
        when(service.get(5L)).thenReturn(demo);

        R<Demo> r = controllerWith(service).get(5L);

        assertEquals(demo, r.getData());
        verify(service).get(5L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_update_delete委托服务并透出布尔结果() throws Exception {
        CrudService<Demo, Long> service = mock(CrudService.class);
        Demo demo = new Demo();
        when(service.create(demo)).thenReturn(true);
        when(service.update(demo)).thenReturn(false);   // 影响 0 行
        when(service.delete(9L)).thenReturn(true);

        DemoController controller = controllerWith(service);

        assertEquals(Boolean.TRUE, controller.create(demo).getData());
        // 假成功被修复：update 影响 0 行 -> data=false
        assertEquals(Boolean.FALSE, controller.update(demo).getData());
        assertEquals(Boolean.TRUE, controller.delete(9L).getData());

        verify(service).create(demo);
        verify(service).update(demo);
        verify(service).delete(9L);
    }
}
