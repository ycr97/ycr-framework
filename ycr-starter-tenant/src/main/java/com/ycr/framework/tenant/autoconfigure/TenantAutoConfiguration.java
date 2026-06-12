package com.ycr.framework.tenant.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.ycr.framework.tenant.handler.YcrTenantLineHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 多租户自动配置
 *
 * <p>默认关闭，须显式 {@code ycr.tenant.enabled=true} 开启（fail-closed）。租户拦截器声明为
 * {@link InnerInterceptor} Bean，由 data-mp 的 {@code MybatisPlusAutoConfiguration} 自动收集并织入到分页之前。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@ConditionalOnClass(TenantLineInnerInterceptor.class)
@EnableConfigurationProperties(TenantProperties.class)
@ConditionalOnProperty(prefix = "ycr.tenant", name = "enabled", havingValue = "true")
public class TenantAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public YcrTenantLineHandler ycrTenantLineHandler(TenantProperties properties) {
        return new YcrTenantLineHandler(properties);
    }

    /**
     * 租户行级拦截器，作为 InnerInterceptor 由 data-mp 聚合到分页拦截器之前
     */
    @Bean
    @ConditionalOnMissingBean(name = "tenantLineInnerInterceptor")
    public InnerInterceptor tenantLineInnerInterceptor(YcrTenantLineHandler handler) {
        return new TenantLineInnerInterceptor(handler);
    }
}
