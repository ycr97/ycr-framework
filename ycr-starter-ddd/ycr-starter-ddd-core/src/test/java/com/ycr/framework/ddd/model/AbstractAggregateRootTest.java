package com.ycr.framework.ddd.model;

import com.ycr.framework.ddd.event.DomainEvent;
import com.ycr.framework.ddd.event.DomainEventPublisher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * 聚合根事件收集与发布测试
 *
 * @author ycr
 */
class AbstractAggregateRootTest {

    static class OrderCreated extends DomainEvent {
        OrderCreated(Object source) {
            super(source);
        }
    }

    static class OrderAggregate extends AbstractAggregateRoot<Long> {
        private final Long id;

        OrderAggregate(Long id) {
            this.id = id;
        }

        @Override
        public Long getId() {
            return id;
        }

        void create() {
            registerEvent(new OrderCreated(id));
        }
    }

    @Test
    void 注册事件后可读且只读_清空生效() {
        OrderAggregate order = new OrderAggregate(1L);
        order.create();

        List<DomainEvent> events = order.getDomainEvents();
        assertEquals(1, events.size());
        assertThrows(UnsupportedOperationException.class, () -> events.add(null));

        order.clearDomainEvents();
        assertTrue(order.getDomainEvents().isEmpty());
    }

    @Test
    void publishEvents逐个发布并清空() {
        OrderAggregate order = new OrderAggregate(2L);
        order.create();
        order.create();
        DomainEventPublisher publisher = mock(DomainEventPublisher.class);

        order.publishEvents(publisher);

        inOrder(publisher).verify(publisher, org.mockito.Mockito.times(2))
                .publish(org.mockito.ArgumentMatchers.any(DomainEvent.class));
        assertTrue(order.getDomainEvents().isEmpty());
    }
}
