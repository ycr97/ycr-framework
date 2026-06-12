package com.ycr.framework.ddd.event;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 领域事件基类
 *
 * <p>基于 Spring {@link ApplicationEvent}，由 {@code DomainEventPublisher} 发布、Spring 事件机制消费。
 * 内置 {@code eventId}（便于去重/链路追踪）与 {@code occurredOn}。</p>
 *
 * @author ycr
 */
public abstract class DomainEvent extends ApplicationEvent {

    /** 事件唯一标识 */
    private final String eventId;

    /** 事件发生时间 */
    private final LocalDateTime occurredOn;

    protected DomainEvent(Object source) {
        super(source);
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
    }

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
