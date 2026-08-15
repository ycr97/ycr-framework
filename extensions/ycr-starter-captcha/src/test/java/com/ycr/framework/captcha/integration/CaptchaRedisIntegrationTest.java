package com.ycr.framework.captcha.integration;

import com.ycr.framework.captcha.autoconfigure.CaptchaProperties;
import com.ycr.framework.captcha.service.HutoolCaptchaService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "YCR_REDIS_INTEGRATION_TESTS", matches = "true")
class CaptchaRedisIntegrationTest {

    private static RedissonClient redissonClient;

    @BeforeAll
    static void connectRedis() {
        Config config = new Config();
        config.useSingleServer().setAddress(requiredEnvironmentVariable("YCR_TEST_REDIS_ADDRESS"));
        String password = System.getenv("YCR_TEST_REDIS_PASSWORD");
        if (password != null && !password.isBlank()) {
            config.useSingleServer().setPassword(password);
        }
        redissonClient = Redisson.create(config);
    }

    @AfterAll
    static void closeRedis() {
        if (redissonClient != null) {
            redissonClient.shutdown();
        }
    }

    @Test
    @DisplayName("并发校验同一验证码时只能成功一次")
    void concurrentVerificationShouldConsumeCaptchaOnlyOnce() throws Exception {
        CaptchaProperties properties = new CaptchaProperties();
        properties.setKeyPrefix("ycr:test:captcha:" + UUID.randomUUID());
        HutoolCaptchaService service = new HutoolCaptchaService(properties, redissonClient);
        String id = UUID.randomUUID().toString();
        String redisKey = properties.getKeyPrefix() + ":" + id;
        redissonClient.<String>getBucket(redisKey).set("A1B2", Duration.ofSeconds(30));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Boolean> first = executor.submit(() -> {
                start.await();
                return service.verify(id, "a1b2");
            });
            Future<Boolean> second = executor.submit(() -> {
                start.await();
                return service.verify(id, "A1B2");
            });
            start.countDown();

            int successCount = (first.get(5, TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(5, TimeUnit.SECONDS) ? 1 : 0);
            assertThat(successCount).isEqualTo(1);
            assertThat(redissonClient.getBucket(redisKey).isExists()).isFalse();
        } finally {
            executor.shutdownNow();
            redissonClient.getBucket(redisKey).delete();
        }
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be configured when Redis integration tests are enabled");
        }
        return value;
    }
}
