package com.ycr.framework.auth.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * YCR Sa-Token 认证适配器配置。
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.auth.satoken")
public class SaTokenAuthProperties {

    /** 是否启用 YCR Sa-Token 认证适配器。 */
    private boolean enabled;

    /** 端点保护策略。 */
    private EndpointPolicy endpointPolicy = EndpointPolicy.AUTHENTICATED;

    /** 允许匿名访问的路径；默认包含 /error，防止错误分派再次触发认证。 */
    private List<String> permitPaths = new ArrayList<>(List.of("/error"));

    /** 登录会话存储。 */
    private SessionStore sessionStore = SessionStore.MEMORY;

    public enum EndpointPolicy {
        /** 所有端点默认要求登录，白名单除外。 */
        AUTHENTICATED,
        /** 仅使用方法注解声明登录与权限要求。 */
        ANNOTATED
    }

    public enum SessionStore {
        /** 进程内会话，仅适合本地开发、测试或明确的单实例应用。 */
        MEMORY,
        /** 基于现有 RedissonClient 的分布式会话。 */
        REDIS
    }
}
