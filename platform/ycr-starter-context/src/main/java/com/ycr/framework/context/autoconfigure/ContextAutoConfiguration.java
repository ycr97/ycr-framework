package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.sign.ContextHeaderSigner;
import com.ycr.framework.context.sign.ContextReplayGuard;
import com.ycr.framework.context.sign.FailClosedContextReplayGuard;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 上下文模块自动配置
 *
 * <p>提供与运行环境无关的上下文属性、签名和防重放能力。
 * Servlet 请求解析与 Filter 由 {@link ContextServletAutoConfiguration} 独立装配。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@EnableConfigurationProperties(ContextProperties.class)
public class ContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ContextHeaderSigner contextHeaderSigner() {
        return new ContextHeaderSigner();
    }

    @Bean
    @ConditionalOnMissingBean
    public ContextReplayGuard contextReplayGuard() {
        return new FailClosedContextReplayGuard();
    }

}
