package com.ycr.framework.sdk;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Feign 客户端服务基类
 *
 * <p>SDK 发布模式：服务提供方在 client 模块定义 Feign 接口，并继承本类封装远程调用；消费方引入 client jar
 * 后直接注入使用。子类绑定的具体 Feign 接口由 Spring 的泛型感知注入填入 {@link #feignClient}。</p>
 *
 * <pre>{@code
 * class UserSdkService extends AbstractFeignClientService<UserClient> {
 *     public UserDTO getUser(Long id) { return getClient().getById(id); }
 * }
 * }</pre>
 *
 * @param <T> Feign 客户端接口类型
 * @author ycr
 */
public abstract class AbstractFeignClientService<T> {

    /** 由子类泛型绑定的 Feign 客户端，按类型注入 */
    @Autowired
    protected T feignClient;

    /**
     * 获取 Feign 客户端实例
     */
    protected T getClient() {
        return feignClient;
    }
}
