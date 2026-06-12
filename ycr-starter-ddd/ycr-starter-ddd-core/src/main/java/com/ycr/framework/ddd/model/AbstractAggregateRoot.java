package com.ycr.framework.ddd.model;

import com.ycr.framework.ddd.event.DomainEvent;
import com.ycr.framework.ddd.event.DomainEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类 —— 内置领域事件收集与发布能力
 *
 * <p>聚合在业务方法中 {@link #registerEvent} 收集事件；应用在 {@code repository.save()} 后调
 * {@link #publishEvents(DomainEventPublisher)} 发布并清空。发布器入参传入，不持静态全局态。</p>
 *
 * @param <ID> 聚合根标识类型
 * @author ycr
 */
public abstract class AbstractAggregateRoot<ID> {

    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * 聚合根标识
     */
    public abstract ID getId();

    /**
     * 注册领域事件（待发布）
     */
    protected void registerEvent(DomainEvent event) {
        if (event != null) {
            domainEvents.add(event);
        }
    }

    /**
     * 待发布的领域事件（只读快照）
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 清空领域事件
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    /**
     * 用给定发布器逐个发布已收集事件，随后清空
     */
    public void publishEvents(DomainEventPublisher publisher) {
        for (DomainEvent event : domainEvents) {
            publisher.publish(event);
        }
        clearDomainEvents();
    }
}
