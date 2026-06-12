package com.ycr.framework.business.autoconfigure;

import com.ycr.framework.business.aop.BizApiAspect;
import com.ycr.framework.business.chain.BizInterceptor;
import com.ycr.framework.business.chain.BizInterceptorChain;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 接入层拦截链自动配置
 *
 * <p>聚合容器内所有 {@link BizInterceptor}（含应用自定义）构建 {@link BizInterceptorChain}，并注册
 * {@link BizApiAspect} 织入 {@code @BizApi}。无任何拦截器时链为空、切面空转，不影响调用。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@ConditionalOnClass(ProceedingJoinPoint.class)
@EnableConfigurationProperties(BusinessProperties.class)
@ConditionalOnProperty(prefix = "ycr.business", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BusinessAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BizInterceptorChain bizInterceptorChain(ObjectProvider<BizInterceptor> interceptors) {
        List<BizInterceptor> list = interceptors.orderedStream().toList();
        return new BizInterceptorChain(list);
    }

    @Bean
    @ConditionalOnMissingBean
    public BizApiAspect bizApiAspect(BizInterceptorChain chain) {
        return new BizApiAspect(chain);
    }
}
