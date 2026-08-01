package com.ycr.framework.auth.integration;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoForRedisson;
import cn.dev33.satoken.plugin.SaTokenPluginForJackson;
import cn.dev33.satoken.session.SaSession;
import com.ycr.framework.context.model.UserContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "YCR_REDIS_INTEGRATION_TESTS", matches = "true")
class SaTokenRedisIntegrationTest {

    private static RedissonClient redissonClient;

    private static SaTokenDao firstNode;

    private static SaTokenDao secondNode;

    @BeforeAll
    static void connectRedis() {
        String address = requiredEnvironmentVariable("YCR_TEST_REDIS_ADDRESS");
        Config config = new Config();
        config.useSingleServer().setAddress(address);
        String password = System.getenv("YCR_TEST_REDIS_PASSWORD");
        if (password != null && !password.isBlank()) {
            config.useSingleServer().setPassword(password);
        }
        redissonClient = Redisson.create(config);
        new SaTokenPluginForJackson().install();
        firstNode = new SaTokenDaoForRedisson(redissonClient);
        secondNode = new SaTokenDaoForRedisson(redissonClient);
    }

    @AfterAll
    static void closeRedis() {
        if (redissonClient != null) {
            redissonClient.shutdown();
        }
    }

    @Test
    @DisplayName("两个应用节点应共享Token数据与TTL")
    void applicationNodesShouldShareTokenStateAndTtl() {
        String key = key("token");
        try {
            firstNode.set(key, "login-1001", 30);

            assertThat(secondNode.get(key)).isEqualTo("login-1001");
            assertThat(secondNode.getTimeout(key)).isBetween(1L, 30L);
        } finally {
            firstNode.delete(key);
        }
    }

    @Test
    @DisplayName("UserContext序列化后应可由另一节点恢复并删除")
    void userContextShouldRoundTripAcrossApplicationNodes() {
        String key = key("user-context");
        UserContext expected = new UserContext();
        expected.setUserId(1001L);
        expected.setUsername("alice");
        expected.setTenantId(10L);
        expected.setRoles(Set.of("user"));
        expected.setPermissions(Set.of("order:read"));
        SaSession session = new SaSession(key);
        session.set("ycr_user_context", expected);
        try {
            firstNode.setSession(session, 30);

            SaSession restored = secondNode.getSession(key);
            assertThat(restored.getModel("ycr_user_context", UserContext.class)).isEqualTo(expected);
            secondNode.deleteSession(key);
            assertThat(firstNode.getSession(key)).isNull();
        } finally {
            firstNode.deleteSession(key);
        }
    }

    private static String key(String suffix) {
        return "ycr:test:auth:" + suffix + ":" + UUID.randomUUID();
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be configured when Redis integration tests are enabled");
        }
        return value;
    }
}
