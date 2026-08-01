package com.ycr.framework.context.security;

import com.ycr.framework.context.exception.ContextAuthException;
import com.ycr.framework.context.model.UserContext;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 验证两个身份上下文是否可以证明属于同一用户。
 *
 * @author ycr
 */
public final class UserContextIdentityVerifier {

    private UserContextIdentityVerifier() {
    }

    /**
     * 校验受信上下文与 token 上下文的身份和租户是否兼容。
     *
     * @param trusted 受信上游上下文
     * @param token token 解析出的上下文
     */
    public static void verifyCompatible(UserContext trusted, UserContext token) {
        if (trusted == null || token == null) {
            throw new ContextAuthException("身份上下文不能为空");
        }

        boolean sameIdentity;
        if (trusted.getUserId() != null && token.getUserId() != null) {
            sameIdentity = Objects.equals(trusted.getUserId(), token.getUserId());
        } else if (StringUtils.hasText(trusted.getUsername()) && StringUtils.hasText(token.getUsername())) {
            sameIdentity = Objects.equals(trusted.getUsername(), token.getUsername());
        } else {
            sameIdentity = false;
        }
        if (!sameIdentity) {
            throw new ContextAuthException("签名上下文与 token 身份不一致");
        }
        if (trusted.getTenantId() != null
                && token.getTenantId() != null
                && !Objects.equals(trusted.getTenantId(), token.getTenantId())) {
            throw new ContextAuthException("签名上下文与 token 租户不一致");
        }
    }
}
