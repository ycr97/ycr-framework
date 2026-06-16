package com.ycr.framework.cache.jetcache;

import com.alicp.jetcache.anno.Cached;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 声明式缓存真实行为端到端测试。
 *
 * <p>用 {@code linkedhashmap}（本地）+ {@code mock}（远程）后端，全程不依赖 Redis，可在 CI 直接跑。
 * 端到端证明 {@code CacheAdvisor} + {@code JetCacheInterceptor} + {@code ConfigMap} + CacheManager
 * 真实拦截并命中缓存。</p>
 *
 * @author ycr
 */
@SpringBootTest(classes = CachedBehaviorTest.TestApp.class)
@TestPropertySource(properties = {
        "jetcache.local.default.type=linkedhashmap",
        "jetcache.remote.default.type=mock",
        "ycr.cache.jetcache.base-packages=com.ycr.framework.cache.jetcache"
})
class CachedBehaviorTest {

    @Autowired
    private CountingService countingService;

    @Test
    void 同键二次调用命中缓存_方法体只执行一次() {
        assertThat(countingService.invocations()).isZero();

        Long first = countingService.compute(1L);
        Long second = countingService.compute(1L);

        assertThat(first).isEqualTo(second);
        // 第二次命中缓存，方法体不再执行，计数器仍为 1
        assertThat(countingService.invocations()).isEqualTo(1);
    }

    @Test
    void 不同键各自回源_计数器递增() {
        int before = countingService.invocations();

        countingService.compute(100L);
        countingService.compute(200L);
        countingService.compute(100L); // 命中

        assertThat(countingService.invocations()).isEqualTo(before + 2);
    }

    /** 最小可启动应用：触发 JetCache 自带与本模块自动配置，并显式声明被缓存的服务 Bean。 */
    @SpringBootApplication
    static class TestApp {

        @Bean
        CountingService countingService() {
            return new CountingService();
        }
    }

    /** 被缓存的服务：每次真实执行方法体时计数 +1，用于验证缓存是否命中。 */
    static class CountingService {

        private final AtomicInteger counter = new AtomicInteger();

        @Cached(name = "behavior:", key = "#id", expire = 60, timeUnit = TimeUnit.SECONDS)
        public Long compute(Long id) {
            counter.incrementAndGet();
            return id * 10;
        }

        int invocations() {
            return counter.get();
        }
    }
}
