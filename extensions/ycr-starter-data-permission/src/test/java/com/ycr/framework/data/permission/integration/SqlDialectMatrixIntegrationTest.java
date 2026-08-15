package com.ycr.framework.data.permission.integration;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.apache.ibatis.annotations.Param;
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
@SpringBootTest(classes = SqlDialectMatrixIntegrationTest.TestApplication.class)
@TestPropertySource(properties = {
        "ycr.tenant.enabled=true",
        "ycr.tenant.ignore-tables[0]=order_items",
        "ycr.data.permission.enabled=true",
        "ycr.data.permission.governed-tables[0]=orders"
})
class SqlDialectMatrixIntegrationTest {

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
        jdbcTemplate.execute("drop table if exists order_items");
        jdbcTemplate.execute("drop table if exists orders");
        jdbcTemplate.execute("""
                create table orders (
                    id bigint primary key,
                    tenant_id bigint not null,
                    owner_id bigint not null,
                    status varchar(32) not null
                )
                """);
        jdbcTemplate.execute("""
                create table order_items (
                    id bigint primary key,
                    order_id bigint not null,
                    product_name varchar(64) not null
                )
                """);
        jdbcTemplate.update("insert into orders values (1, 42, 1001, 'OPEN')");
        jdbcTemplate.update("insert into orders values (2, 42, 2002, 'OPEN')");
        jdbcTemplate.update("insert into orders values (3, 99, 1001, 'OPEN')");
        jdbcTemplate.update("insert into orders values (4, 42, 1001, 'CLOSED')");
        jdbcTemplate.update("insert into order_items values (11, 1, 'keyboard')");
        jdbcTemplate.update("insert into order_items values (12, 2, 'keyboard')");
        jdbcTemplate.update("insert into order_items values (13, 3, 'keyboard')");
        jdbcTemplate.update("insert into order_items values (14, 4, 'mouse')");

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
    @DisplayName("真实数据库应按租户和数据权限隔离别名查询")
    void dialectShouldIsolateAliasedQuery() {
        assertThat(orderMapper.findOpenOrderIds()).containsExactly(1L);
    }

    @Test
    @DisplayName("真实数据库应改写JOIN与EXISTS子查询")
    void dialectShouldRewriteJoinAndExistsSubquery() {
        assertThat(orderMapper.findIdsWithProduct("keyboard")).containsExactly(1L);
        assertThat(orderMapper.findIdsWithAnyItem()).containsExactly(1L, 4L);
    }

    @Test
    @DisplayName("真实数据库分页应在安全拦截器之后执行")
    void paginationShouldRunAfterSecurityInterceptors() {
        assertThat(interceptor.getInterceptors())
                .extracting(Object::getClass)
                .containsExactly(
                        TenantLineInnerInterceptor.class,
                        DataPermissionInterceptor.class,
                        PaginationInnerInterceptor.class);
        IPage<Long> page = orderMapper.findVisiblePage(Page.of(1, 1));
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getRecords()).containsExactly(1L);
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

        @Select("select o.id from orders o where o.status = 'OPEN' order by o.id")
        List<Long> findOpenOrderIds();

        @Select("""
                select o.id
                from orders o
                join order_items i on i.order_id = o.id
                where i.product_name = #{product}
                order by o.id
                """)
        List<Long> findIdsWithProduct(@Param("product") String product);

        @Select("""
                select o.id
                from orders o
                where exists (select 1 from order_items i where i.order_id = o.id)
                order by o.id
                """)
        List<Long> findIdsWithAnyItem();

        @Select("select o.id from orders o order by o.id")
        IPage<Long> findVisiblePage(IPage<Long> page);
    }
}
