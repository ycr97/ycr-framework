package com.ycr.framework.log.autoconfigure;

import com.ycr.framework.log.aop.LogAspect;
import com.ycr.framework.log.handler.LogHandler;
import com.ycr.framework.log.handler.Slf4jLogHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 操作日志自动配置
 *
 * <p>装配链路：{@link LogProperties} 开关 → 默认 {@link LogHandler}（业务可覆盖）→ 可选异步执行器
 * → {@link LogAspect} 切面。通过 {@code ycr.log.enabled=false} 关闭整条链路。</p>
 *
 * @author ycr
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(LogProperties.class)
@ConditionalOnProperty(prefix = "ycr.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LogAutoConfiguration {

    /**
     * 默认日志处理器，业务方实现 {@link LogHandler} 即可覆盖
     */
    @Bean
    @ConditionalOnMissingBean
    public LogHandler logHandler() {
        return new Slf4jLogHandler();
    }

    /**
     * 异步落库执行器，仅在 {@code ycr.log.async=true} 时装配。
     * 有界队列 + CallerRunsPolicy：队列满时回落到调用线程，避免日志任务无声丢弃。
     */
    @Bean("ycrLogExecutor")
    @ConditionalOnProperty(prefix = "ycr.log", name = "async", havingValue = "true")
    public Executor ycrLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(256);
        executor.setThreadNamePrefix("ycr-log-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 操作日志切面：异步执行器缺失（同步模式）时 ObjectProvider 返回 null，切面退化为同步落库
     */
    @Bean
    public LogAspect logAspect(LogHandler logHandler,
                               LogProperties logProperties,
                               @Qualifier("ycrLogExecutor") ObjectProvider<Executor> logExecutorProvider) {
        return new LogAspect(logHandler, logProperties, logExecutorProvider.getIfAvailable());
    }
}
