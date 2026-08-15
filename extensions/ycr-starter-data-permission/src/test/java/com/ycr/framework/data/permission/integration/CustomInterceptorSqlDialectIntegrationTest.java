package com.ycr.framework.data.permission.integration;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.TenantContext;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.data.permission.rule.DataPermissionRule;
import com.ycr.framework.data.permission.rule.Predicate;
import com.ycr.framework.data.permission.scope.DataScope;
import com.ycr.framework.data.permission.scope.DataScopeResolver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "YCR_DIALECT_INTEGRATION_TESTS", matches = "true")
@Testcontainers
@SpringBootTest(classes = CustomInterceptorSqlDialectIntegrationTest.TestApplication.class)
@TestPropertySource(properties = {
        "ycr.tenant.enabled=true",
        "ycr.data.permission.enabled=true",
        "ycr.data.permission.governed-tables[0]=orders"
})
class CustomInterceptorSqlDialectIntegrationTest {

    @Container
    private static final JdbcDatabaseContainer<?> DATABASE = SqlDialectContainer.create();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MybatisPlusInterceptor interceptor;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.username", DATABASE::getUsername);
        registry.add("spring.datasource.password", DATABASE::getPassword);
        registry.add("spring.datasource.driver-class-name", DATABASE::getDriverClassName);
    }

    @BeforeEach
    void prepareDatabaseAndContext() {
        jdbcTemplate.execute("drop table if exists orders");
        jdbcTemplate.execute("""
                create table orders (
                    id bigint primary key,
                    tenant_id bigint not null,
                    owner_id bigint not null
                )
                """);
        jdbcTemplate.update("insert into orders values (1, 42, 1001)");
        jdbcTemplate.update("insert into orders values (2, 42, 2002)");
        jdbcTemplate.update("insert into orders values (3, 99, 1001)");

        TenantContext tenant = new TenantContext();
        tenant.setTenantId(42L);
        TenantContextHolder.set(tenant);
        UserContext user = new UserContext();
        user.setUserId(1001L);
        UserContextHolder.set(user);
    }

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("自定义MyBatis拦截器应自动合并安全插件并真实隔离数据")
    void customInterceptorShouldMergeSecurityPluginsAndIsolateData() {
        assertThat(interceptor.getInterceptors())
                .extracting(Object::getClass)
                .containsExactly(
                        TenantLineInnerInterceptor.class,
                        DataPermissionInterceptor.class,
                        PaginationInnerInterceptor.class);
        assertThat(orderMapper.findIds()).containsExactly(1L);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @MapperScan(basePackageClasses = OrderMapper.class)
    @Import(MatrixConfiguration.class)
    static class TestApplication {
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class MatrixConfiguration {

        @Bean
        MybatisPlusInterceptor customMybatisPlusInterceptor() {
            MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
            return interceptor;
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
        List<Long> findIds();
    }
}
