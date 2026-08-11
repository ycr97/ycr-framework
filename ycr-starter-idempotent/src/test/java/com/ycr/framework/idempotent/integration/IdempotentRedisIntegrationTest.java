package com.ycr.framework.idempotent.integration;

import com.ycr.framework.idempotent.annotation.Idempotent;
import com.ycr.framework.idempotent.aop.IdempotentAspect;
import com.ycr.framework.idempotent.autoconfigure.IdempotentProperties;
import com.ycr.framework.idempotent.exception.IdempotentException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnabledIfEnvironmentVariable(named = "YCR_REDIS_INTEGRATION_TESTS", matches = "true")
class IdempotentRedisIntegrationTest {

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
    @DisplayName("旧请求异常不得删除超时后新请求持有的幂等键")
    void expiredOwnerFailureShouldNotDeleteReplacementOwner() throws Exception {
        IdempotentProperties properties = new IdempotentProperties();
        properties.setKeyPrefix("ycr:test:idempotent:" + UUID.randomUUID());
        BlockingService target = new BlockingService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new IdempotentAspect(properties, redissonClient));
        BlockingService proxy = factory.getProxy();
        String requestId = UUID.randomUUID().toString();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> expiredOwner = executor.submit(() -> proxy.submit(requestId));
            assertThat(target.firstEntered.await(5, TimeUnit.SECONDS)).isTrue();

            Thread.sleep(1_200);
            Future<String> replacementOwner = executor.submit(() -> proxy.submit(requestId));
            assertThat(target.secondEntered.await(5, TimeUnit.SECONDS)).isTrue();

            target.releaseFirst.countDown();
            assertThatThrownBy(() -> get(expiredOwner))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("first request failed");

            assertThatThrownBy(() -> proxy.submit(requestId))
                    .isInstanceOf(IdempotentException.class);

            target.releaseSecond.countDown();
            assertThat(replacementOwner.get(5, TimeUnit.SECONDS)).isEqualTo("ok");
        } finally {
            target.releaseFirst.countDown();
            target.releaseSecond.countDown();
            executor.shutdownNow();
            redissonClient.getKeys().deleteByPattern(properties.getKeyPrefix() + ":*");
        }
    }

    private static String get(Future<String> future) throws Exception {
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException exception) {
            if (exception.getCause() instanceof Exception cause) {
                throw cause;
            }
            throw exception;
        }
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be configured when Redis integration tests are enabled");
        }
        return value;
    }

    public static class BlockingService {

        private final CountDownLatch firstEntered = new CountDownLatch(1);
        private final CountDownLatch secondEntered = new CountDownLatch(1);
        private final CountDownLatch releaseFirst = new CountDownLatch(1);
        private final CountDownLatch releaseSecond = new CountDownLatch(1);

        @Idempotent(key = "#requestId", timeout = 1, unit = TimeUnit.SECONDS)
        public String submit(String requestId) throws InterruptedException {
            if (firstEntered.getCount() > 0) {
                firstEntered.countDown();
                releaseFirst.await();
                throw new IllegalStateException("first request failed");
            }
            secondEntered.countDown();
            releaseSecond.await();
            return "ok";
        }
    }
}
