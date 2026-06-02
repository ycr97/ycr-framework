package com.ycr.framework.data.mp.autoconfigure;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.ycr.framework.data.mp.handler.AutoFillMetaObjectHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MybatisPlusAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MybatisPlusAutoConfiguration.class));

    @Test
    void shouldRegisterDefaultBeans() {
        contextRunner.run(context -> {
            assertEquals(1, context.getBeansOfType(AutoFillMetaObjectHandler.class).size());
            assertEquals(1, context.getBeansOfType(MybatisPlusInterceptor.class).size());
        });
    }

    @Test
    void shouldRespectDisableProperties() {
        contextRunner
                .withPropertyValues(
                        "ycr.data.mp.auto-fill-enabled=false",
                        "ycr.data.mp.pagination-enabled=false"
                )
                .run(context -> {
                    assertEquals(0, context.getBeansOfType(AutoFillMetaObjectHandler.class).size());
                    // MybatisPlusInterceptor 容器始终保留（用于承载数据权限等其它 InnerInterceptor），
                    // 关闭分页只是不再追加 PaginationInnerInterceptor，因此此处内部拦截器列表应为空
                    assertEquals(1, context.getBeansOfType(MybatisPlusInterceptor.class).size());
                    MybatisPlusInterceptor interceptor = context.getBean(MybatisPlusInterceptor.class);
                    assertEquals(0, interceptor.getInterceptors().size());
                });
    }

    @Test
    void shouldBackOffWhenUserProvidesBeans() {
        contextRunner
                .withUserConfiguration(CustomBeansConfiguration.class)
                .run(context -> {
                    assertEquals(1, context.getBeansOfType(MetaObjectHandler.class).size());
                    assertEquals(1, context.getBeansOfType(MybatisPlusInterceptor.class).size());
                    assertEquals(CustomMetaObjectHandler.class, context.getBean(MetaObjectHandler.class).getClass());
                });
    }

    @Test
    void shouldBindMaxLimitToPaginationInterceptor() {
        contextRunner
                .withPropertyValues("ycr.data.mp.max-limit=256")
                .run(context -> {
                    MybatisPlusInterceptor interceptor = context.getBean(MybatisPlusInterceptor.class);
                    List<InnerInterceptor> interceptors = interceptor.getInterceptors();
                    PaginationInnerInterceptor paginationInterceptor = (PaginationInnerInterceptor) interceptors.get(0);
                    assertEquals(256L, paginationInterceptor.getMaxLimit());
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomBeansConfiguration {

        @Bean
        MetaObjectHandler customMetaObjectHandler() {
            return new CustomMetaObjectHandler();
        }

        @Bean
        MybatisPlusInterceptor customMybatisPlusInterceptor() {
            return new MybatisPlusInterceptor();
        }
    }

    static class CustomMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(org.apache.ibatis.reflection.MetaObject metaObject) {
        }

        @Override
        public void updateFill(org.apache.ibatis.reflection.MetaObject metaObject) {
        }
    }
}
