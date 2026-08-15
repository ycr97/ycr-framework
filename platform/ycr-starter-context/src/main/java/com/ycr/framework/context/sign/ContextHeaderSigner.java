package com.ycr.framework.context.sign;

import com.ycr.framework.context.exception.ContextAuthException;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;

/**
 * 上下文 Header 签名器。
 *
 * @author ycr
 */
public class ContextHeaderSigner {

    private static final String ALGORITHM = "HmacSHA256";

    /**
     * 使用 HmacSHA256 计算签名。
     */
    public String sign(ContextHeaderSnapshot snapshot, String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new ContextAuthException("上下文签名密钥不能为空");
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] digest = mac.doFinal(canonical(snapshot).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (ContextAuthException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("上下文签名计算失败", e);
        }
    }

    /**
     * 校验签名。
     */
    public boolean verify(ContextHeaderSnapshot snapshot, String secret, String signature) {
        if (!StringUtils.hasText(signature)) {
            return false;
        }
        String expected = sign(snapshot, secret);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 判断签名时间戳是否过期。
     */
    public boolean isExpired(ContextHeaderSnapshot snapshot, Duration ttl, Clock clock) {
        if (snapshot == null || !StringUtils.hasText(snapshot.getTimestamp())
                || ttl == null || ttl.isZero() || ttl.isNegative()) {
            return true;
        }
        try {
            long timestamp = Long.parseLong(snapshot.getTimestamp());
            long now = clock.millis();
            long delta = Math.subtractExact(now, timestamp);
            long ttlMillis = ttl.toMillis();
            return delta > ttlMillis || delta < -ttlMillis;
        } catch (ArithmeticException e) {
            return true;
        }
    }

    /**
     * 按固定字段顺序构造签名原文。
     */
    public String canonical(ContextHeaderSnapshot snapshot) {
        return String.join("\n",
                value(snapshot.getMethod()),
                value(snapshot.getPath()),
                value(snapshot.getAudience()),
                value(snapshot.getTimestamp()),
                value(snapshot.getNonce()),
                value(snapshot.getUserId()),
                value(snapshot.getUsername()),
                value(snapshot.getNickname()),
                value(snapshot.getTenantId()),
                value(snapshot.getTenantCode()),
                value(snapshot.getDeptId()),
                value(snapshot.getRoles()),
                value(snapshot.getPermissions()),
                value(snapshot.getClientId()),
                value(snapshot.getAppId()),
                value(snapshot.getTraceId()));
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
