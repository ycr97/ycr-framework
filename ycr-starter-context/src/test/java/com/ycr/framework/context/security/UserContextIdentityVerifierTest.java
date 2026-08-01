package com.ycr.framework.context.security;

import com.ycr.framework.context.exception.ContextAuthException;
import com.ycr.framework.context.model.UserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserContextIdentityVerifierTest {

    @Test
    @DisplayName("userId相同且租户相同时应通过")
    void acceptsMatchingUserIdAndTenant() {
        UserContext trusted = context(100L, "alice", 10L);
        UserContext token = context(100L, "other-name", 10L);

        assertDoesNotThrow(() -> UserContextIdentityVerifier.verifyCompatible(trusted, token));
    }

    @Test
    @DisplayName("userId不同应拒绝")
    void rejectsDifferentUserId() {
        assertThrows(ContextAuthException.class,
                () -> UserContextIdentityVerifier.verifyCompatible(
                        context(100L, "alice", 10L), context(101L, "alice", 10L)));
    }

    @Test
    @DisplayName("缺少userId时应使用相同username校验")
    void acceptsMatchingUsernameWhenUserIdMissing() {
        assertDoesNotThrow(() -> UserContextIdentityVerifier.verifyCompatible(
                context(null, "alice", null), context(null, "alice", null)));
    }

    @Test
    @DisplayName("无法证明同一身份时应拒绝")
    void rejectsUnprovableIdentity() {
        assertThrows(ContextAuthException.class,
                () -> UserContextIdentityVerifier.verifyCompatible(
                        context(100L, null, null), context(null, "alice", null)));
    }

    @Test
    @DisplayName("租户不同应拒绝")
    void rejectsDifferentTenant() {
        assertThrows(ContextAuthException.class,
                () -> UserContextIdentityVerifier.verifyCompatible(
                        context(100L, "alice", 10L), context(100L, "alice", 11L)));
    }

    private UserContext context(Long userId, String username, Long tenantId) {
        UserContext context = new UserContext();
        context.setUserId(userId);
        context.setUsername(username);
        context.setTenantId(tenantId);
        return context;
    }
}
