package com.ycr.framework.data.mp.util;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ycr.framework.data.model.PageQuery;
import com.ycr.framework.data.model.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MpPageHelper 转换与排序守卫测试
 *
 * @author ycr
 */
class MpPageHelperTest {

    @Test
    @DisplayName("toPage_页码与size正确_无排序时无OrderItem")
    void shouldMatchExpectedBehavior001() {
        PageQuery pq = new PageQuery();
        pq.setPage(3);
        pq.setSize(20);

        Page<Object> page = MpPageHelper.toPage(pq);

        assertEquals(3, page.getCurrent());
        assertEquals(20, page.getSize());
        assertTrue(page.orders().isEmpty());
    }

    @Test
    @DisplayName("toPage_排序字段转下划线列名_默认asc")
    void shouldMatchExpectedBehavior002() {
        PageQuery pq = new PageQuery();
        pq.setSortField("createTime");

        Page<Object> page = MpPageHelper.toPage(pq);

        List<OrderItem> orders = page.orders();
        assertEquals(1, orders.size());
        assertEquals("create_time", orders.get(0).getColumn());
        assertTrue(orders.get(0).isAsc());
    }

    @Test
    @DisplayName("toPage_desc识别")
    void shouldMatchExpectedBehavior003() {
        PageQuery pq = new PageQuery();
        pq.setSortField("userName");
        pq.setSortOrder("DESC");

        OrderItem order = MpPageHelper.toPage(pq).orders().get(0);
        assertEquals("user_name", order.getColumn());
        assertTrue(!order.isAsc());
    }

    @Test
    @DisplayName("toPage_非法排序字段抛异常_拦注入")
    void shouldMatchExpectedBehavior004() {
        PageQuery pq = new PageQuery();
        pq.setSortField("name; drop table user");

        assertThrows(IllegalArgumentException.class, () -> MpPageHelper.toPage(pq));
    }

    @Test
    @DisplayName("toResult_字段映射")
    void shouldMatchExpectedBehavior005() {
        Page<String> page = new Page<>(2, 10, 57);
        page.setRecords(List.of("a", "b"));

        PageResult<String> result = MpPageHelper.toResult(page);

        assertEquals(List.of("a", "b"), result.getList());
        assertEquals(57, result.getTotal());
        assertEquals(2, result.getPage());
        assertEquals(10, result.getSize());
    }
}
