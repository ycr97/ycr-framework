package com.ycr.framework.auth.autoconfigure;

import com.ycr.framework.auth.handler.SaTokenExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 认证模块自动配置
 *
 * @author ycr
 */
@AutoConfiguration
public class AuthAutoConfiguration {

    @Bean
    public SaTokenExceptionHandler saTokenExceptionHandler() {
        return new SaTokenExceptionHandler();
    }
}
