package com.ycr.framework.ddd.event;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * DomainEventPublisher 测试
 *
 * @author ycr
 */
class DomainEventPublisherTest {

    static class SampleEvent extends DomainEvent {
        SampleEvent(Object source) {
            super(source);
        }
    }

    @Test
    void publish委托ApplicationEventPublisher() {
        ApplicationEventPublisher spring = mock(ApplicationEventPublisher.class);
        DomainEventPublisher publisher = new DomainEventPublisher(spring);
        SampleEvent event = new SampleEvent("src");

        publisher.publish(event);

        verify(spring).publishEvent(event);
    }

    @Test
    void 无事务时publishAfterCommit退化为立即发布() {
        ApplicationEventPublisher spring = mock(ApplicationEventPublisher.class);
        DomainEventPublisher publisher = new DomainEventPublisher(spring);
        SampleEvent event = new SampleEvent("src");

        // 测试线程无活动事务同步 -> 立即发布
        publisher.publishAfterCommit(event);

        verify(spring).publishEvent(event);
    }

    @Test
    void 事件携带eventId与occurredOn() {
        SampleEvent a = new SampleEvent("s");
        SampleEvent b = new SampleEvent("s");
        assertNotNull(a.getEventId());
        assertNotNull(a.getOccurredOn());
        assertNotEquals(a.getEventId(), b.getEventId());
    }
}
