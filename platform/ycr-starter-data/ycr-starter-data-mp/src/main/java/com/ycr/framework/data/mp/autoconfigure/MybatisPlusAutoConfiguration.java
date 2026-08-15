package com.ycr.framework.data.mp.autoconfigure;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.ycr.framework.data.mp.handler.AutoFillMetaObjectHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;

import java.util.Comparator;

/**
 * MyBatis-Plus 自动配置
 *
 * @author ycr
 */
@AutoConfiguration
@EnableConfigurationProperties(MybatisPlusProperties.class)
public class MybatisPlusAutoConfiguration {

    @Bean
    public static BeanPostProcessor innerInterceptorMergeBeanPostProcessor(
            ObjectProvider<InnerInterceptor> innerInterceptors) {
        return new InnerInterceptorMergeBeanPostProcessor(innerInterceptors);
    }

    @Bean
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    @ConditionalOnProperty(prefix = "ycr.data.mp", name = "auto-fill-enabled", havingValue = "true", matchIfMissing = true)
    public AutoFillMetaObjectHandler autoFillMetaObjectHandler() {
        return new AutoFillMetaObjectHandler();
    }

    @Bean
    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    public MybatisPlusInterceptor mybatisPlusInterceptor(MybatisPlusProperties properties,
                                                          ObjectProvider<InnerInterceptor> innerInterceptors) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        innerInterceptors.orderedStream()
                .sorted(Comparator.comparingInt(InnerInterceptorMergeBeanPostProcessor::priority))
                .forEach(interceptor::addInnerInterceptor);
        if (properties.isPaginationEnabled()) {
            PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
            paginationInnerInterceptor.setMaxLimit(properties.getMaxLimit());
            interceptor.addInnerInterceptor(paginationInnerInterceptor);
        }
        return interceptor;
    }
}
