package com.ycr.framework.auth.oauth2.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * OAuth2 Resource Server 配置。
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.auth.oauth2.resource-server")
public class OAuth2ResourceServerProperties {

    private boolean enabled = false;

    private TokenMode mode;

    private EndpointPolicy endpointPolicy = EndpointPolicy.AUTHENTICATED;

    private List<String> permitPaths = new ArrayList<>(List.of("/error"));

    private Jwt jwt = new Jwt();

    private Opaque opaque = new Opaque();

    private Claims claims = new Claims();

    public enum TokenMode {
        JWT,
        OPAQUE
    }

    public enum EndpointPolicy {
        AUTHENTICATED,
        ANNOTATED
    }

    @Data
    public static class Jwt {

        private String issuerUri;

        private String jwkSetUri;

        private List<String> audiences = new ArrayList<>();

        private List<String> allowedAlgorithms = new ArrayList<>(List.of("RS256"));

        private Duration clockSkew = Duration.ofSeconds(60);
    }

    @Data
    public static class Opaque {

        private String introspectionUri;

        private String clientId;

        private String clientSecret;

        private List<String> audiences = new ArrayList<>();

        private String issuer;

        private Duration connectTimeout = Duration.ofSeconds(2);

        private Duration readTimeout = Duration.ofSeconds(2);
    }

    @Data
    public static class Claims {

        private String userId = "user_id";

        private String username = "preferred_username";

        private String nickname = "name";

        private String tenantId = "tenant_id";

        private String deptId = "dept_id";

        private String roles = "roles";

        private String permissions = "permissions";

        private String scopes = "scope";

        private String clientId = "client_id";
    }
}
