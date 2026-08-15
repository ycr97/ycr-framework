package com.ycr.framework.auth.oauth2.mapper;

import com.ycr.framework.auth.oauth2.autoconfigure.OAuth2ResourceServerProperties;
import com.ycr.framework.context.model.UserContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 默认平面 claims 映射器。
 *
 * @author ycr
 */
public class DefaultOAuth2UserContextMapper implements OAuth2UserContextMapper {

    private final OAuth2ResourceServerProperties.Claims properties;

    public DefaultOAuth2UserContextMapper(OAuth2ResourceServerProperties properties) {
        this.properties = properties.getClaims();
    }

    @Override
    public UserContext map(Map<String, Object> claims) {
        if (claims == null) {
            throw new OAuth2ClaimsMappingException("OAuth2 claims must not be null");
        }

        UserContext userContext = new UserContext();
        userContext.setUserId(longClaim(claims, properties.getUserId()));
        userContext.setUsername(stringClaim(claims, properties.getUsername(), "sub"));
        userContext.setNickname(stringClaim(claims, properties.getNickname()));
        userContext.setTenantId(longClaim(claims, properties.getTenantId()));
        userContext.setDeptId(longClaim(claims, properties.getDeptId()));
        userContext.setRoles(values(claims.get(properties.getRoles()), false));
        userContext.setPermissions(values(claims.get(properties.getPermissions()), false));
        userContext.setPermissions(merge(userContext.getPermissions(), values(claims.get(properties.getScopes()), true)));
        userContext.setPermissions(merge(userContext.getPermissions(), values(claims.get("scp"), true)));
        userContext.setClientId(stringClaim(claims, properties.getClientId(), "azp"));

        if (userContext.getUserId() == null && !StringUtils.hasText(userContext.getUsername())) {
            throw new OAuth2ClaimsMappingException(
                    "OAuth2 claims must contain " + properties.getUserId() + " or " + properties.getUsername()
                            + "/sub");
        }
        return userContext;
    }

    private Long longClaim(Map<String, Object> claims, String claimName) {
        if (!StringUtils.hasText(claimName)) {
            return null;
        }
        Object value = claims.get(claimName);
        if (value == null || value instanceof String string && !StringUtils.hasText(string)) {
            return null;
        }
        if (!(value instanceof Number) && !(value instanceof CharSequence)) {
            throw invalidClaim(claimName, "a number or decimal string");
        }
        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException e) {
            throw invalidClaim(claimName, "a number or decimal string", e);
        }
    }

    private String stringClaim(Map<String, Object> claims, String claimName, String fallbackClaimName) {
        String value = stringClaim(claims, claimName);
        if (StringUtils.hasText(value) && !value.isBlank()) {
            return value;
        }
        return fallbackClaimName == null ? null : stringClaim(claims, fallbackClaimName);
    }

    private String stringClaim(Map<String, Object> claims, String claimName) {
        if (!StringUtils.hasText(claimName)) {
            return null;
        }
        Object value = claims.get(claimName);
        if (value == null) {
            return null;
        }
        if (!(value instanceof CharSequence) && !(value instanceof Number)) {
            throw invalidClaim(claimName, "a string or number");
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private Set<String> values(Object value, boolean scopeValue) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (value == null) {
            return values;
        }
        if (value instanceof CharSequence text) {
            String delimiter = scopeValue ? "\\s+" : "[,\\s]+";
            for (String item : text.toString().split(delimiter)) {
                addString(values, item);
            }
            return values;
        }
        if (value instanceof Collection<?> collection) {
            collection.forEach(item -> addString(values, item));
            return values;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                addString(values, Array.get(value, i));
            }
        }
        return values;
    }

    private void addString(Set<String> values, Object value) {
        if (value instanceof CharSequence text && StringUtils.hasText(text)) {
            values.add(text.toString().trim());
        }
    }

    private Set<String> merge(Set<String> first, Set<String> second) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (first != null) {
            merged.addAll(first);
        }
        if (second != null) {
            merged.addAll(second);
        }
        return merged;
    }

    private OAuth2ClaimsMappingException invalidClaim(String claimName, String expected) {
        return invalidClaim(claimName, expected, null);
    }

    private OAuth2ClaimsMappingException invalidClaim(String claimName, String expected, Throwable cause) {
        return new OAuth2ClaimsMappingException("OAuth2 claim '" + claimName + "' must be " + expected, cause);
    }
}
