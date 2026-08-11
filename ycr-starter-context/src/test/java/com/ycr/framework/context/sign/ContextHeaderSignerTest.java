package com.ycr.framework.context.sign;

import com.ycr.framework.context.exception.ContextAuthException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ContextHeaderSigner 测试。
 *
 * @author ycr
 */
class ContextHeaderSignerTest {

    private final ContextHeaderSigner signer = new ContextHeaderSigner();

    private ContextHeaderSnapshot snapshot() {
        ContextHeaderSnapshot snapshot = new ContextHeaderSnapshot();
        snapshot.setMethod("GET");
        snapshot.setPath("/api/orders");
        snapshot.setAudience("order-service");
        snapshot.setTimestamp("100000");
        snapshot.setNonce("nonce-1");
        snapshot.setUserId("100");
        snapshot.setUsername("alice");
        snapshot.setNickname("Alice");
        snapshot.setTenantId("1");
        snapshot.setTenantCode("tenant-a");
        snapshot.setDeptId("9");
        snapshot.setRoles("admin,user");
        snapshot.setPermissions("order:create");
        snapshot.setClientId("web");
        snapshot.setAppId("app-x");
        snapshot.setTraceId("trace-1");
        return snapshot;
    }

    @Test
    @DisplayName("相同快照和密钥应生成稳定签名")
    void shouldMatchExpectedBehavior001() {
        String first = signer.sign(snapshot(), "secret");
        String second = signer.sign(snapshot(), "secret");

        assertEquals(first, second);
        assertTrue(signer.verify(snapshot(), "secret", first));
    }

    @Test
    @DisplayName("字段变化应导致验签失败")
    void shouldMatchExpectedBehavior002() {
        ContextHeaderSnapshot snapshot = snapshot();
        String signature = signer.sign(snapshot, "secret");
        snapshot.setUserId("101");

        assertFalse(signer.verify(snapshot, "secret", signature));
    }

    @Test
    @DisplayName("附加上下文字段变化应导致验签失败")
    void shouldMatchExpectedBehavior003() {
        ContextHeaderSnapshot snapshot = snapshot();
        String signature = signer.sign(snapshot, "secret");

        snapshot.setTenantCode("tenant-b");

        assertFalse(signer.verify(snapshot, "secret", signature));
    }

    @Test
    @DisplayName("目标服务变化应导致验签失败")
    void shouldRejectSignatureWhenAudienceChanges() {
        ContextHeaderSnapshot snapshot = snapshot();
        String signature = signer.sign(snapshot, "secret");

        snapshot.setAudience("payment-service");

        assertFalse(signer.verify(snapshot, "secret", signature));
    }

    @Test
    @DisplayName("时间戳超过ttl应过期")
    void shouldMatchExpectedBehavior004() {
        Clock clock = Clock.fixed(Instant.ofEpochMilli(170000), ZoneOffset.UTC);

        assertTrue(signer.isExpired(snapshot(), Duration.ofSeconds(60), clock));
    }

    @Test
    @DisplayName("极端时间戳溢出时应视为过期")
    void shouldMatchExpectedBehavior005() {
        ContextHeaderSnapshot snapshot = snapshot();
        snapshot.setTimestamp(String.valueOf(Long.MIN_VALUE));
        Clock clock = Clock.fixed(Instant.ofEpochMilli(0), ZoneOffset.UTC);

        assertTrue(signer.isExpired(snapshot, Duration.ofSeconds(60), clock));
    }

    @Test
    @DisplayName("空密钥应failFast")
    void shouldMatchExpectedBehavior006() {
        assertThrows(ContextAuthException.class, () -> signer.sign(snapshot(), ""));
    }
}
