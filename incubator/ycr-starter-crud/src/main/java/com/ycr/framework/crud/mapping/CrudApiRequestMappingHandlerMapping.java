package com.ycr.framework.crud.mapping;

import com.ycr.framework.crud.annotation.CrudApi;
import com.ycr.framework.crud.enums.Api;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * 支持 {@code @CrudApi} 关端点的 RequestMapping 处理器映射
 *
 * <p>映射注册期，若 Controller 类标注 {@link CrudApi} 且当前基类方法对应的 {@link Api} 在 {@code disable}
 * 列表中，则返回 {@code null} 跳过注册（该端点不可达）。其余委托父类正常注册。</p>
 *
 * @author ycr
 */
public class CrudApiRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    /** 基类方法名 → CRUD 操作 */
    private static final Map<String, Api> METHOD_API = Map.of(
            "page", Api.PAGE,
            "list", Api.LIST,
            "get", Api.GET,
            "create", Api.CREATE,
            "update", Api.UPDATE,
            "delete", Api.DELETE);

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        if (isDisabled(handlerType, method)) {
            return null;
        }
        return super.getMappingForMethod(method, handlerType);
    }

    /**
     * 判定该方法对应的 CRUD 端点是否被 {@code @CrudApi} 关闭（纯决策逻辑，便于单测）
     */
    boolean isDisabled(Class<?> handlerType, Method method) {
        CrudApi crudApi = AnnotatedElementUtils.findMergedAnnotation(handlerType, CrudApi.class);
        if (crudApi == null || crudApi.disable().length == 0) {
            return false;
        }
        Api api = METHOD_API.get(method.getName());
        if (api == null) {
            return false;
        }
        return Arrays.asList(crudApi.disable()).contains(api);
    }
}
