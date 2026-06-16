package com.ycr.framework.cache.jetcache.autoconfigure;

import com.alicp.jetcache.anno.aop.CacheAdvisor;
import com.alicp.jetcache.anno.aop.JetCacheInterceptor;
import com.alicp.jetcache.autoconfigure.JetCacheAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JetCache 声明式缓存装配开关测试。
 *
 * <p>纳入 JetCache 自带 {@link JetCacheAutoConfiguration} 以满足 {@link JetCacheInterceptor}
 * 对 {@code ConfigProvider}/{@code CacheManager} 的注入；远程后端用 {@code mock}，本地用
 * {@code linkedhashmap}，全程不依赖 Redis。</p>
 *
 * @author ycr
 */
class JetCacheAnnoAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JetCacheAutoConfiguration.class, JetCacheAnnoAutoConfiguration.class))
            .withPropertyValues(
                    "jetcache.local.default.type=linkedhashmap",
                    "jetcache.remote.default.type=mock");

    @Test
    void 默认装配_拦截器与增强器均存在() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(JetCacheInterceptor.class);
            assertThat(context).hasBean(CacheAdvisor.CACHE_ADVISOR_BEAN_NAME);
            CacheAdvisor advisor = context.getBean(CacheAdvisor.CACHE_ADVISOR_BEAN_NAME, CacheAdvisor.class);
            assertThat(readBasePackages(advisor)).containsExactly("");
        });
    }

    @Test
    void 自定义扫描包_属性驱动生效() {
        runner.withPropertyValues("ycr.cache.jetcache.base-packages=com.acme")
                .run(context -> {
                    CacheAdvisor advisor = context.getBean(CacheAdvisor.CACHE_ADVISOR_BEAN_NAME, CacheAdvisor.class);
                    assertThat(readBasePackages(advisor)).containsExactly("com.acme");
                });
    }

    @Test
    void 关闭开关_两个基础设施Bean均不装配() {
        runner.withPropertyValues("ycr.cache.jetcache.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(JetCacheInterceptor.class);
                    assertThat(context).doesNotHaveBean(CacheAdvisor.CACHE_ADVISOR_BEAN_NAME);
                });
    }

    /** 读取 {@link CacheAdvisor} 的私有 {@code basePackages}，直接验证属性已透传至增强器。 */
    private static String[] readBasePackages(CacheAdvisor advisor) throws Exception {
        Field field = CacheAdvisor.class.getDeclaredField("basePackages");
        field.setAccessible(true);
        return (String[]) field.get(advisor);
    }
}
