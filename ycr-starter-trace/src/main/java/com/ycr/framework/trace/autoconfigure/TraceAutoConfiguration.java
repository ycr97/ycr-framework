package com.ycr.framework.trace.autoconfigure;

import com.ycr.framework.trace.filter.TraceFilter;
import com.ycr.framework.trace.generator.TraceIdGenerator;
import com.ycr.framework.trace.generator.UuidTraceIdGenerator;
import com.ycr.framework.trace.util.TraceUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * 链路追踪自动配置
 *
 * <p>装配链路：{@link TraceProperties} 开关 → 默认 {@link TraceIdGenerator}（业务可覆盖）
 * → {@link TraceFilter} 注册（最外层 order）。通过 {@code ycr.trace.enabled=false} 关闭。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(TraceProperties.class)
@ConditionalOnProperty(prefix = "ycr.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TraceAutoConfiguration {

    /**
     * 默认 TraceId 生成器，业务方实现 {@link TraceIdGenerator} 即可覆盖
     */
    @Bean
    @ConditionalOnMissingBean
    public TraceIdGenerator traceIdGenerator() {
        return new UuidTraceIdGenerator();
    }

    /**
     * 注册链路追踪过滤器，并把生成器注入 {@link TraceUtils}
     */
    @Bean
    public FilterRegistrationBean<TraceFilter> ycrTraceFilterRegistration(TraceProperties properties,
                                                                          TraceIdGenerator traceIdGenerator) {
        TraceUtils.setGenerator(traceIdGenerator);
        FilterRegistrationBean<TraceFilter> registration =
                new FilterRegistrationBean<>(
                        new TraceFilter(properties.getHeaderName(), properties.getRequestHeaderName()));
        registration.addUrlPatterns("/*");
        registration.setName("ycrTraceFilter");
        registration.setOrder(properties.getFilterOrder());
        return registration;
    }
}
