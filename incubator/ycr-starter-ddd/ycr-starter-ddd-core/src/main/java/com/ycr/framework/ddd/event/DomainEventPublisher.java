package com.ycr.framework.ddd.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 领域事件发布器
 *
 * <p>两种模式：{@link #publish} 立即发布；{@link #publishAfterCommit} 在当前事务提交后发布
 * （无活动事务时退化为立即发布，避免静默丢事件）。</p>
 *
 * @author ycr
 */
public class DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 立即发布领域事件
     */
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * 事务提交后发布；无活动事务时立即发布
     */
    public void publishAfterCommit(DomainEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    applicationEventPublisher.publishEvent(event);
                }
            });
        } else {
            publish(event);
        }
    }
}
