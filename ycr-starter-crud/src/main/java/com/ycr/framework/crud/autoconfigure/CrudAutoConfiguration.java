package com.ycr.framework.crud.autoconfigure;

import com.ycr.framework.crud.mapping.CrudApiRequestMappingHandlerMapping;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 通用 CRUD 自动配置
 *
 * <p>通过 Spring Boot 官方扩展点 {@link WebMvcRegistrations} 注入自定义
 * {@link CrudApiRequestMappingHandlerMapping}，使 {@code @CrudApi} 关端点生效。
 * 容器仅允许一个 {@code WebMvcRegistrations}，故 {@code @ConditionalOnMissingBean} 让位应用自定义。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(RequestMappingHandlerMapping.class)
public class CrudAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(WebMvcRegistrations.class)
    public WebMvcRegistrations crudWebMvcRegistrations() {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new CrudApiRequestMappingHandlerMapping();
            }
        };
    }
}
