package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.propagation.ContextTaskDecorator;
import com.ycr.framework.context.propagation.CoreThreadContextAccessor;
import com.ycr.framework.context.propagation.ThreadContextAccessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;

import java.util.Arrays;
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

    @Bean
    public SmartInitializingSingleton contextTaskDecoratorPrimaryValidator(
            ConfigurableListableBeanFactory beanFactory) {
        return () -> {
            List<String> decoratorNames = Arrays.stream(
                            beanFactory.getBeanNamesForType(TaskDecorator.class, false, false))
                    .toList();
            if (decoratorNames.size() <= 1) {
                return;
            }
            List<String> primaryDecorators = decoratorNames.stream()
                    .filter(name -> beanFactory.getMergedBeanDefinition(name).isPrimary())
                    .toList();
            boolean contextDecoratorIsSolePrimary = primaryDecorators.size() == 1
                    && beanFactory.getBean(primaryDecorators.get(0)) instanceof ContextTaskDecorator;
            if (!contextDecoratorIsSolePrimary) {
                throw new IllegalStateException(
                        "When multiple TaskDecorator beans exist, ContextTaskDecorator must be the sole primary; "
                                + "primary decorators: " + primaryDecorators);
            }
        };
    }
}
