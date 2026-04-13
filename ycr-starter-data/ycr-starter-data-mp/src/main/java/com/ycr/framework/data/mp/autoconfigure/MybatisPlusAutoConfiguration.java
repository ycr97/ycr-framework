package com.ycr.framework.data.mp.autoconfigure;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.ycr.framework.data.mp.handler.AutoFillMetaObjectHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis-Plus 自动配置
 *
 * @author ycr
 */
@AutoConfiguration
@EnableConfigurationProperties(MybatisPlusProperties.class)
public class MybatisPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    @ConditionalOnProperty(prefix = "ycr.data.mp", name = "auto-fill-enabled", havingValue = "true", matchIfMissing = true)
    public AutoFillMetaObjectHandler autoFillMetaObjectHandler() {
        return new AutoFillMetaObjectHandler();
    }

    @Bean
    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    @ConditionalOnProperty(prefix = "ycr.data.mp", name = "pagination-enabled", havingValue = "true", matchIfMissing = true)
    public MybatisPlusInterceptor mybatisPlusInterceptor(MybatisPlusProperties properties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setMaxLimit(properties.getMaxLimit());
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }
}
