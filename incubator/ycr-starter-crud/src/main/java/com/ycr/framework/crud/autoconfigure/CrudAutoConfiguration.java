package com.ycr.framework.crud.autoconfigure;

import com.ycr.framework.crud.mapping.CrudApiRequestMappingHandlerMapping;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

/**
 * 通用 CRUD 自动配置
 *
 * <p>通过 Spring Boot 官方扩展点 {@link WebMvcRegistrations} 注入自定义
 * {@link CrudApiRequestMappingHandlerMapping}，使 {@code @CrudApi} 关端点生效。
 * 容器仅允许一个 {@code WebMvcRegistrations}。应用自定义该扩展点时必须显式返回
 * {@link CrudApiRequestMappingHandlerMapping}，否则启动失败，避免 {@code @CrudApi(disable=...)} 静默失效。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(RequestMappingHandlerMapping.class)
@EnableConfigurationProperties(CrudProperties.class)
@ConditionalOnProperty(prefix = "ycr.crud", name = "enabled", havingValue = "true")
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

    @Bean
    public SmartInitializingSingleton crudWebMvcRegistrationsValidator(
            List<WebMvcRegistrations> registrations) {
        return () -> {
            if (registrations.size() != 1
                    || !(registrations.get(0).getRequestMappingHandlerMapping()
                    instanceof CrudApiRequestMappingHandlerMapping)) {
                throw new IllegalStateException(
                        "ycr.crud.enabled=true requires the single WebMvcRegistrations bean "
                                + "to return CrudApiRequestMappingHandlerMapping");
            }
        };
    }
}
