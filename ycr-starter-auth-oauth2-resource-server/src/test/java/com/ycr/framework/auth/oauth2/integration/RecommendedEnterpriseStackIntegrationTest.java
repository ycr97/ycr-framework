package com.ycr.framework.auth.oauth2.integration;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.data.permission.rule.DataPermissionRule;
import com.ycr.framework.data.permission.rule.Predicate;
import com.ycr.framework.data.permission.scope.DataScope;
import com.ycr.framework.data.permission.scope.DataScopeResolver;
import com.ycr.framework.feign.interceptor.ContextPassInterceptor;
import feign.RequestTemplate;
import feign.Target;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RecommendedEnterpriseStackIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.application.name=order-api",
        "spring.datasource.url=jdbc:h2:mem:recommended_stack;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "ycr.auth.oauth2.resource-server.enabled=true",
        "ycr.auth.oauth2.resource-server.mode=jwt",
        "ycr.auth.oauth2.resource-server.jwt.issuer-uri=https://idp.example.com",
        "ycr.auth.oauth2.resource-server.jwt.audiences[0]=order-api",
        "ycr.tenant.enabled=true",
        "ycr.data.permission.enabled=true",
        "ycr.data.permission.governed-tables[0]=orders",
        "ycr.context.header-sign.secret=enterprise-stack-secret",
        "ycr.feign.context-pass-enabled=true",
        "ycr.feign.internal-clients[0]=inventory-service"
})
class RecommendedEnterpriseStackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MybatisPlusInterceptor mybatisPlusInterceptor;

    @BeforeEach
    void prepareDatabase() {
        jdbcTemplate.execute("drop table if exists orders");
        jdbcTemplate.execute("""
                create table orders (
                    id bigint primary key,
                    tenant_id bigint not null,
                    owner_id bigint not null,
                    name varchar(64) not null
                )
                """);
        jdbcTemplate.update("insert into orders values (1, 42, 1001, 'visible')");
        jdbcTemplate.update("insert into orders values (2, 42, 2002, 'other-owner')");
        jdbcTemplate.update("insert into orders values (3, 99, 1001, 'other-tenant')");
    }

    @Test
    @DisplayName("推荐企业栈应联合执行OAuth2、租户、数据权限和Feign签名")
    void recommendedStackShouldEnforceIdentitySqlIsolationAndFeignAudience() throws Exception {
        assertThat(mybatisPlusInterceptor.getInterceptors())
                .extracting(Object::getClass)
                .containsExactly(
                        TenantLineInnerInterceptor.class,
                        DataPermissionInterceptor.class,
                        PaginationInnerInterceptor.class);

        mockMvc.perform(get("/api/stack/orders")
                        .header("Authorization", "Bearer " + OAuth2WebTestSupport.validToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderIds[0]").value(1))
                .andExpect(jsonPath("$.orderIds.length()").value(1))
                .andExpect(jsonPath("$.downstreamUserId").value("1001"))
                .andExpect(jsonPath("$.downstreamTenantId").value("42"))
                .andExpect(jsonPath("$.downstreamAudience").value("inventory-service"))
                .andExpect(jsonPath("$.signed").value(true));

        assertThat(UserContextHolder.get()).isNull();
        assertThat(TenantContextHolder.get()).isNull();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @MapperScan(basePackageClasses = OrderMapper.class)
    @Import({StackConfiguration.class, StackController.class})
    static class TestApplication {
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class StackConfiguration {

        @Bean
        JwtDecoder jwtDecoder() {
            return OAuth2WebTestSupport.jwtDecoder();
        }

        @Bean
        DataScopeResolver dataScopeResolver() {
            return () -> DataScope.builder()
                    .dimension("ownerIds", List.of(UserContextHolder.getUserId()))
                    .build();
        }

        @Bean
        DataPermissionRule orderOwnerRule() {
            return new DataPermissionRule() {
                @Override
                public String table() {
                    return "orders";
                }

                @Override
                public Predicate predicate(DataScope scope) {
                    Collection<?> ownerIds = scope.values("ownerIds");
                    return ownerIds.isEmpty() ? Predicate.deny() : Predicate.in("owner_id", ownerIds);
                }
            };
        }
    }

    @Mapper
    interface OrderMapper {

        @Select("select id from orders order by id")
        List<Long> findVisibleOrderIds();
    }

    @RestController
    static class StackController {

        private final OrderMapper orderMapper;
        private final ContextPassInterceptor contextPassInterceptor;

        StackController(OrderMapper orderMapper, ContextPassInterceptor contextPassInterceptor) {
            this.orderMapper = orderMapper;
            this.contextPassInterceptor = contextPassInterceptor;
        }

        @GetMapping("/api/stack/orders")
        Map<String, Object> orders() {
            RequestTemplate template = new RequestTemplate();
            template.method("GET");
            template.uri("/internal/orders");
            template.feignTarget(new Target.HardCodedTarget<>(
                    Object.class, "inventory-service", "http://inventory-service"));
            contextPassInterceptor.apply(template);

            return Map.of(
                    "orderIds", orderMapper.findVisibleOrderIds(),
                    "downstreamUserId", firstHeader(template, ContextHeaderConstants.HEADER_USER_ID),
                    "downstreamTenantId", firstHeader(template, ContextHeaderConstants.HEADER_TENANT_ID),
                    "downstreamAudience", firstHeader(template, ContextHeaderConstants.HEADER_CONTEXT_AUDIENCE),
                    "signed", template.headers().containsKey(ContextHeaderConstants.HEADER_CONTEXT_SIGNATURE));
        }

        private String firstHeader(RequestTemplate template, String name) {
            return template.headers().get(name).iterator().next();
        }
    }
}
