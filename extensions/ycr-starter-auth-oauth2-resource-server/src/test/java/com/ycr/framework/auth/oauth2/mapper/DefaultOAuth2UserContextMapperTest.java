package com.ycr.framework.auth.oauth2.mapper;

import com.ycr.framework.auth.oauth2.autoconfigure.OAuth2ResourceServerProperties;
import com.ycr.framework.context.model.UserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultOAuth2UserContextMapperTest {

    private final DefaultOAuth2UserContextMapper mapper =
            new DefaultOAuth2UserContextMapper(new OAuth2ResourceServerProperties());

    @Test
    @DisplayName("应映射数字和字符串身份字段以及基础claims")
    void mapsIdentityAndBasicClaims() {
        UserContext context = mapper.map(Map.of(
                "user_id", 1001L,
                "preferred_username", "alice",
                "name", "Alice",
                "tenant_id", "10",
                "dept_id", 20,
                "client_id", "web",
                "roles", List.of("admin", "user"),
                "permissions", new String[]{"order:read", "order:write"},
                "scope", "profile order:read"));

        assertThat(context.getUserId()).isEqualTo(1001L);
        assertThat(context.getUsername()).isEqualTo("alice");
        assertThat(context.getNickname()).isEqualTo("Alice");
        assertThat(context.getTenantId()).isEqualTo(10L);
        assertThat(context.getDeptId()).isEqualTo(20L);
        assertThat(context.getClientId()).isEqualTo("web");
        assertThat(context.getRoles()).containsExactlyInAnyOrder("admin", "user");
        assertThat(context.getPermissions()).containsExactlyInAnyOrder(
                "order:read", "order:write", "profile");
    }

    @Test
    @DisplayName("username和clientId应分别回退sub和azp")
    void fallsBackToSubjectAndAuthorizedParty() {
        UserContext context = mapper.map(Map.of(
                "sub", "subject-1",
                "azp", "client-1"));

        assertThat(context.getUsername()).isEqualTo("subject-1");
        assertThat(context.getClientId()).isEqualTo("client-1");
    }

    @Test
    @DisplayName("scp集合应与scope合并并去重")
    void mergesScopeAndScpClaims() {
        UserContext context = mapper.map(Map.of(
                "user_id", "1001",
                "scp", List.of("order:read", "profile"),
                "scope", "profile order:write"));

        assertThat(context.getPermissions()).containsExactlyInAnyOrder("order:read", "profile", "order:write");
    }

    @Test
    @DisplayName("非法数字claim应认证失败")
    void rejectsInvalidNumericClaim() {
        assertThatThrownBy(() -> mapper.map(Map.of("user_id", "not-a-number")))
                .isInstanceOf(OAuth2ClaimsMappingException.class)
                .hasMessageContaining("user_id");
    }

    @Test
    @DisplayName("缺失可证明身份的claim应认证失败")
    void rejectsMissingIdentityClaims() {
        assertThatThrownBy(() -> mapper.map(Map.of("scope", "profile")))
                .isInstanceOf(OAuth2ClaimsMappingException.class)
                .hasMessageContaining("user_id");
    }

    @Test
    @DisplayName("空值和非字符串集合元素应忽略")
    void ignoresEmptyAndNonStringCollectionElements() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", 1001);
        claims.put("roles", Arrays.asList("admin", "", 1, "admin"));
        claims.put("permissions", Arrays.asList("order:read", null, 2));

        UserContext context = mapper.map(claims);

        assertThat(context.getRoles()).containsExactly("admin");
        assertThat(context.getPermissions()).containsExactly("order:read");
    }
}
