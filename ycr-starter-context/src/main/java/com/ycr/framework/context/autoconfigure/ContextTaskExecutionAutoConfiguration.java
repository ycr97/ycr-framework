package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.propagation.ContextTaskDecorator;
import com.ycr.framework.context.propagation.CoreThreadContextAccessor;
import com.ycr.framework.context.propagation.ThreadContextAccessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;

import java.util.List;

/** 线程池上下文传播自动配置。 */
@AutoConfiguration
@ConditionalOnClass(TaskDecorator.class)
public class ContextTaskExecutionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "coreThreadContextAccessor")
    public ThreadContextAccessor coreThreadContextAccessor() {
        return new CoreThreadContextAccessor();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(ContextTaskDecorator.class)
    public ContextTaskDecorator contextTaskDecorator(List<ThreadContextAccessor> accessors,
                                                     ObjectProvider<TaskDecorator> taskDecorators) {
        return new ContextTaskDecorator(accessors, taskDecorators);
    }
}
