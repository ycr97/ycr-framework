package com.ycr.framework.cache.jetcache.autoconfigure;

import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.aop.CacheAdvisor;
import com.alicp.jetcache.anno.aop.JetCacheInterceptor;
import com.alicp.jetcache.anno.config.CommonConfiguration;
import com.alicp.jetcache.autoconfigure.JetCacheAutoConfiguration;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.context.annotation.AutoProxyRegistrar;

/**
 * JetCache 声明式缓存自动配置。
 *
 * <p>JetCache 原生的 {@code @EnableMethodCache#basePackages()} 是编译期常量、无法从配置注入，
 * 故本类不使用该注解，而是<b>手动复刻</b> {@code JetCacheProxyConfiguration} 的装配
 * （{@link CacheAdvisor} + {@link JetCacheInterceptor}），并把扫描包改为读取
 * {@link JetCacheAnnoProperties}，实现属性驱动的开箱即用。</p>
 *
 * <ul>
 *     <li>{@link AutoProxyRegistrar}：注册自动代理创建器，使切面得以织入。</li>
 *     <li>{@link CommonConfiguration}：提供 {@code ConfigMap} 等基础设施 Bean。</li>
 *     <li>{@code GlobalCacheConfig} / {@code CacheManager} 仍交由 JetCache 自带的
 *         {@link JetCacheAutoConfiguration} 依据 {@code jetcache.*} 构建，本类排在其后。</li>
 * </ul>
 *
 * @author ycr
 */
@AutoConfiguration(after = JetCacheAutoConfiguration.class)
@ConditionalOnClass({Cached.class, RedissonClient.class})
@ConditionalOnProperty(prefix = "ycr.cache.jetcache", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(JetCacheAnnoProperties.class)
@Import({AutoProxyRegistrar.class, CommonConfiguration.class})
public class JetCacheAnnoAutoConfiguration {

    /**
     * 方法缓存拦截器。无参构造，内部 {@code @Autowired} 注入 {@code ConfigMap} / {@code GlobalCacheConfig}
     * （分别由 {@link CommonConfiguration} 与 {@link JetCacheAutoConfiguration} 提供）。
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean
    public JetCacheInterceptor jetCacheInterceptor() {
        return new JetCacheInterceptor();
    }

    /**
     * 缓存增强器，扫描包与执行顺序均取自 {@link JetCacheAnnoProperties}，从而绕过
     * {@code @EnableMethodCache} 的编译期常量限制。
     */
    @Bean(name = CacheAdvisor.CACHE_ADVISOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(name = CacheAdvisor.CACHE_ADVISOR_BEAN_NAME)
    public CacheAdvisor jetcacheAdvisor(JetCacheInterceptor jetCacheInterceptor, JetCacheAnnoProperties properties) {
        CacheAdvisor advisor = new CacheAdvisor();
        advisor.setAdvice(jetCacheInterceptor);
        advisor.setBasePackages(properties.getBasePackages());
        advisor.setOrder(properties.getOrder());
        return advisor;
    }
}
