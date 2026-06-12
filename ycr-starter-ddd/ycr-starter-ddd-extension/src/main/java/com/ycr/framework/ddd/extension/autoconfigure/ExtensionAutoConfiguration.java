package com.ycr.framework.ddd.extension.autoconfigure;

import com.ycr.framework.ddd.extension.ExtensionBootstrap;
import com.ycr.framework.ddd.extension.ExtensionExecutor;
import com.ycr.framework.ddd.extension.ExtensionRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * DDD 扩展点自动配置
 *
 * <p>注册扩展仓库、执行器与引导器；引导器在容器刷新时把 {@code @Extension} bean 注册进仓库。</p>
 *
 * @author ycr
 */
@AutoConfiguration
public class ExtensionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExtensionRepository extensionRepository() {
        return new ExtensionRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExtensionExecutor extensionExecutor(ExtensionRepository repository) {
        return new ExtensionExecutor(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExtensionBootstrap extensionBootstrap(ExtensionRepository repository) {
        return new ExtensionBootstrap(repository);
    }
}
