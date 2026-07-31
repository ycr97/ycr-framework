package com.ycr.framework.log.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.log.aop.LogAspect;
import com.ycr.framework.log.aspect.MethodLogAspect;
import com.ycr.framework.log.handler.IpRegionResolver;
import com.ycr.framework.log.handler.LogHandler;
import com.ycr.framework.log.handler.Slf4jLogHandler;
import com.ycr.framework.log.util.LogJsonSupport;
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
     * 序列化脱敏管线；ObjectMapper 软依赖（非 web 应用无则降级）。
     */
    @Bean
    @ConditionalOnMissingBean
    public LogJsonSupport logJsonSupport(LogProperties properties, ObjectProvider<ObjectMapper> objectMapper) {
        return new LogJsonSupport(objectMapper.getIfAvailable(), properties.getSensitiveKeys());
    }

    /**
     * 默认 IP 归属地解析器：no-op，业务实现 {@link IpRegionResolver} 自动覆盖。
     */
    @Bean
    @ConditionalOnMissingBean
    public IpRegionResolver ipRegionResolver() {
        return ip -> null;
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
                               @Qualifier("ycrLogExecutor") ObjectProvider<Executor> logExecutorProvider,
                               LogJsonSupport logJsonSupport,
                               IpRegionResolver ipRegionResolver) {
        return new LogAspect(logHandler, logProperties, logExecutorProvider.getIfAvailable(),
                logJsonSupport, ipRegionResolver);
    }

    /**
     * 方法调用日志切面，{@code ycr.log.method.enabled=false} 时不装配。
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ycr.log.method", name = "enabled", havingValue = "true", matchIfMissing = true)
    public MethodLogAspect methodLogAspect(LogJsonSupport logJsonSupport, LogProperties logProperties) {
        return new MethodLogAspect(logJsonSupport, logProperties.getMethod());
    }
}
