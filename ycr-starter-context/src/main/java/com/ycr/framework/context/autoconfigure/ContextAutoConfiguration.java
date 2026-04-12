package com.ycr.framework.context.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * 上下文模块自动配置
 *
 * @author ycr
 */
@AutoConfiguration
public class ContextAutoConfiguration {
    // Context Holder 为静态工具类，无需注册 Bean
    // 后续 Feign 拦截器和 Filter 在各自模块中注册
}
