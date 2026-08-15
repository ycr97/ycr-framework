package com.ycr.framework.cache.jetcache.autoconfigure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JetCache 零配置默认值注入器。
 *
 * <p>JetCache 自带的 {@code JetCacheAutoConfiguration} 在<b>没有任何</b> {@code jetcache.*} 配置时，
 * area 为空，首次 {@code @Cached} 调用会因找不到缓存区域而失败。本处理器以
 * {@code addLast}（最低优先级）注入一组<b>可被消费方覆盖</b>的默认值，使开箱即可用：</p>
 *
 * <ul>
 *     <li>本地一级缓存：Caffeine（{@code jetcache-core} 传递依赖，常驻 classpath）；</li>
 *     <li>远程二级缓存：Redisson（复用 {@code ycr-starter-cache} 已装配的 RedissonClient，不新增连接）；</li>
 *     <li>键转换：Jackson（仅依赖已在的 Jackson）；值序列化：JDK 内置 java（零额外依赖）；</li>
 *     <li>统一键前缀 {@code ycr:cache:}，便于在 Redis 中归类与排障。</li>
 * </ul>
 *
 * <p>消费方可通过在 {@code application.yml} 中显式设置任意 {@code jetcache.*} 键来覆盖（例如调大
 * Caffeine 容量、改键前缀、切换 {@code valueEncoder} 为 kryo5 等）。</p>
 *
 * @author ycr
 */
public class JetCacheDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    /** 属性源名称，置于属性源链尾部，确保优先级最低、可被任意来源覆盖。 */
    private static final String PROPERTY_SOURCE_NAME = "ycr-jetcache-defaults";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 已存在则不重复注入（避免多次回调重复添加）
        if (environment.getPropertySources().contains(PROPERTY_SOURCE_NAME)) {
            return;
        }

        Map<String, Object> defaults = new LinkedHashMap<>();
        // 关闭周期统计日志（按需可由消费方开启）
        defaults.put("jetcache.statIntervalMinutes", 0);
        // 缓存名不拼接 area，保持键简洁
        defaults.put("jetcache.areaInCacheName", false);

        // 本地一级缓存：Caffeine（@Cached(cacheType = BOTH/LOCAL) 时生效）
        defaults.put("jetcache.local.default.type", "caffeine");
        defaults.put("jetcache.local.default.limit", 100);
        defaults.put("jetcache.local.default.keyConvertor", "jackson");

        // 远程二级缓存：Redisson，复用上下文已有的 RedissonClient
        defaults.put("jetcache.remote.default.type", "redisson");
        defaults.put("jetcache.remote.default.keyConvertor", "jackson");
        defaults.put("jetcache.remote.default.valueEncoder", "java");
        defaults.put("jetcache.remote.default.valueDecoder", "java");
        defaults.put("jetcache.remote.default.keyPrefix", "ycr:cache:");

        environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, defaults));
    }
}
