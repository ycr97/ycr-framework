package com.ycr.framework.ddd.event;

import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("publish委托ApplicationEventPublisher")
    void shouldMatchExpectedBehavior001() {
        ApplicationEventPublisher spring = mock(ApplicationEventPublisher.class);
        DomainEventPublisher publisher = new DomainEventPublisher(spring);
        SampleEvent event = new SampleEvent("src");

        publisher.publish(event);

        verify(spring).publishEvent(event);
    }

    @Test
    @DisplayName("无事务时publishAfterCommit退化为立即发布")
    void shouldMatchExpectedBehavior002() {
        ApplicationEventPublisher spring = mock(ApplicationEventPublisher.class);
        DomainEventPublisher publisher = new DomainEventPublisher(spring);
        SampleEvent event = new SampleEvent("src");

        // 测试线程无活动事务同步 -> 立即发布
        publisher.publishAfterCommit(event);

        verify(spring).publishEvent(event);
    }

    @Test
    @DisplayName("事件携带eventId与occurredOn")
    void shouldMatchExpectedBehavior003() {
        SampleEvent a = new SampleEvent("s");
        SampleEvent b = new SampleEvent("s");
        assertNotNull(a.getEventId());
        assertNotNull(a.getOccurredOn());
        assertNotEquals(a.getEventId(), b.getEventId());
    }
}
