package com.ycr.framework.captcha.autoconfigure;

import cn.hutool.captcha.CaptchaUtil;
import com.ycr.framework.captcha.service.CaptchaService;
import com.ycr.framework.captcha.service.HutoolCaptchaService;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 验证码自动配置
 *
 * <p>装配默认 Hutool 图形验证码实现。业务方实现 {@link CaptchaService} 即可覆盖（算术/中文/滑块等）。
 * 通过 {@code ycr.captcha.enabled=false} 关闭。验证码答案的 TTL 存取依赖 cache 模块的 RedisUtils。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@ConditionalOnClass(CaptchaUtil.class)
@EnableConfigurationProperties(CaptchaProperties.class)
@ConditionalOnProperty(prefix = "ycr.captcha", name = "enabled", havingValue = "true")
public class CaptchaAutoConfiguration {

    @Bean
    @ConditionalOnBean(RedissonClient.class)
    @ConditionalOnMissingBean
    public CaptchaService captchaService(CaptchaProperties properties, RedissonClient redissonClient) {
        return new HutoolCaptchaService(properties, redissonClient);
    }

    @Bean
    @ConditionalOnMissingBean({RedissonClient.class, CaptchaService.class})
    public Object captchaMissingRedissonClient() {
        throw new IllegalStateException(
                "ycr.captcha.enabled=true requires a RedissonClient; configure ycr-starter-cache and Redis");
    }
}
