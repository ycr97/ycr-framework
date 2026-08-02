package com.ycr.framework.auth.oauth2.filter;

import com.ycr.framework.auth.oauth2.mapper.OAuth2ClaimsMappingException;
import com.ycr.framework.auth.oauth2.mapper.OAuth2UserContextMapper;
import com.ycr.framework.context.autoconfigure.ContextProperties;
import com.ycr.framework.context.enums.SecurityMode;
import com.ycr.framework.context.enums.UserContextSource;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.context.servlet.ServletContextBinder;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuth2UserContextFilterTest {

    @AfterEach
    void clearThreadContext() {
        new ServletContextBinder().clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("JWT认证结果应映射为TOKEN上下文并在请求结束后清理")
    void jwtAuthenticationIsMappedAndCleared() throws Exception {
        OAuth2UserContextMapper mapper = mock(OAuth2UserContextMapper.class);
        UserContext mapped = userContext(1001L, "alice", UserContextSource.MANUAL.name());
        when(mapper.map(any())).thenReturn(mapped);
        OAuth2UserContextFilter filter = filter(mapper, new ContextProperties(), mock(AuthenticationEntryPoint.class));
        setAuthentication(jwtAuthentication(Map.of("user_id", 1001L)));
        AtomicReference<UserContext> observed = new AtomicReference<>();
        FilterChain chain = (request, response) -> observed.set(UserContextHolder.get());

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(observed.get()).isSameAs(mapped);
        assertThat(observed.get().getSource()).isEqualTo(UserContextSource.TOKEN.name());
        assertThat(UserContextHolder.get()).isNull();
    }

    @Test
    @DisplayName("mapper异常应返回认证失败且不传播为500")
    void mapperFailureReturnsAuthenticationFailure() throws Exception {
        OAuth2UserContextMapper mapper = mock(OAuth2UserContextMapper.class);
        when(mapper.map(any())).thenThrow(new OAuth2ClaimsMappingException("missing identity"));
        AuthenticationEntryPoint entryPoint = mock(AuthenticationEntryPoint.class);
        OAuth2UserContextFilter filter = filter(mapper, new ContextProperties(), entryPoint);
        setAuthentication(jwtAuthentication(Map.of("sub", "subject-1")));

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        verify(entryPoint).commence(any(), any(), any());
        assertThat(UserContextHolder.get()).isNull();
    }

    @Test
    @DisplayName("自定义mapper返回空身份时应认证失败")
    void emptyMappedIdentityReturnsAuthenticationFailure() throws Exception {
        OAuth2UserContextMapper mapper = mock(OAuth2UserContextMapper.class);
        when(mapper.map(any())).thenReturn(new UserContext());
        AuthenticationEntryPoint entryPoint = mock(AuthenticationEntryPoint.class);
        OAuth2UserContextFilter filter = filter(mapper, new ContextProperties(), entryPoint);
        setAuthentication(jwtAuthentication(Map.of("sub", "subject-1")));

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        verify(entryPoint).commence(any(), any(), any());
        assertThat(UserContextHolder.get()).isNull();
    }

    @Test
    @DisplayName("MIXED模式应校验网关上下文与token身份并保留网关上下文")
    void mixedModeKeepsCompatibleGatewayContext() throws Exception {
        ContextProperties properties = new ContextProperties();
        properties.setSecurityMode(SecurityMode.MIXED);
        ServletContextBinder binder = new ServletContextBinder();
        MockHttpServletRequest request = new MockHttpServletRequest();
        UserContext gateway = userContext(1001L, "alice", UserContextSource.GATEWAY_HEADER.name());
        binder.bind(gateway, request);
        OAuth2UserContextMapper mapper = mock(OAuth2UserContextMapper.class);
        when(mapper.map(any())).thenReturn(userContext(1001L, "alice", null));
        OAuth2UserContextFilter filter = filter(mapper, properties, mock(AuthenticationEntryPoint.class), binder);
        setAuthentication(jwtAuthentication(Map.of("user_id", 1001L)));
        AtomicReference<UserContext> observed = new AtomicReference<>();
        FilterChain chain = (servletRequest, servletResponse) -> observed.set(UserContextHolder.get());

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(observed.get()).isSameAs(gateway);
        assertThat(UserContextHolder.get()).isSameAs(gateway);
    }

    @Test
    @DisplayName("MIXED模式身份冲突应认证失败并清理上下文")
    void mixedModeRejectsConflictingGatewayContext() throws Exception {
        ContextProperties properties = new ContextProperties();
        properties.setSecurityMode(SecurityMode.MIXED);
        ServletContextBinder binder = new ServletContextBinder();
        MockHttpServletRequest request = new MockHttpServletRequest();
        binder.bind(userContext(1001L, "alice", UserContextSource.GATEWAY_HEADER.name()), request);
        OAuth2UserContextMapper mapper = mock(OAuth2UserContextMapper.class);
        when(mapper.map(any())).thenReturn(userContext(2002L, "bob", null));
        AuthenticationEntryPoint entryPoint = mock(AuthenticationEntryPoint.class);
        OAuth2UserContextFilter filter = filter(mapper, properties, entryPoint, binder);
        setAuthentication(jwtAuthentication(Map.of("user_id", 2002L)));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        verify(entryPoint).commence(any(), any(), any());
        assertThat(UserContextHolder.get()).isNull();
    }

    @Test
    @DisplayName("已有非网关上下文时应拒绝双重认证污染")
    void rejectsUnexpectedExistingContext() throws Exception {
        ServletContextBinder binder = new ServletContextBinder();
        binder.bind(userContext(1001L, "alice", UserContextSource.MANUAL.name()), new MockHttpServletRequest());
        AuthenticationEntryPoint entryPoint = mock(AuthenticationEntryPoint.class);
        OAuth2UserContextFilter filter = filter(mock(OAuth2UserContextMapper.class),
                new ContextProperties(), entryPoint, binder);

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        verify(entryPoint).commence(any(), any(), any());
        assertThat(UserContextHolder.get()).isNull();
    }

    private OAuth2UserContextFilter filter(OAuth2UserContextMapper mapper,
                                            ContextProperties properties,
                                            AuthenticationEntryPoint entryPoint) {
        return filter(mapper, properties, entryPoint, new ServletContextBinder());
    }

    private OAuth2UserContextFilter filter(OAuth2UserContextMapper mapper,
                                            ContextProperties properties,
                                            AuthenticationEntryPoint entryPoint,
                                            ServletContextBinder binder) {
        return new OAuth2UserContextFilter(mapper, binder, properties, entryPoint);
    }

    private JwtAuthenticationToken jwtAuthentication(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "RS256");
        claims.forEach(builder::claim);
        return new JwtAuthenticationToken(builder.build(), List.of());
    }

    private void setAuthentication(JwtAuthenticationToken authentication) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private UserContext userContext(Long userId, String username, String source) {
        UserContext context = new UserContext();
        context.setUserId(userId);
        context.setUsername(username);
        context.setSource(source);
        return context;
    }
}
