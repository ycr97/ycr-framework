package com.ycr.framework.crud.mapping;

import com.ycr.framework.crud.annotation.CrudApi;
import com.ycr.framework.crud.controller.AbstractCrudController;
import com.ycr.framework.crud.enums.Api;
import com.ycr.framework.data.model.BaseDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @CrudApi 关端点决策逻辑测试
 *
 * @author ycr
 */
class CrudApiRequestMappingHandlerMappingTest {

    static class Demo extends BaseDO {
    }

    @CrudApi(disable = {Api.DELETE, Api.CREATE})
    static class RestrictedController extends AbstractCrudController<Demo, Long, Object> {
    }

    static class OpenController extends AbstractCrudController<Demo, Long, Object> {
    }

    private final CrudApiRequestMappingHandlerMapping mapping = new CrudApiRequestMappingHandlerMapping();

    private Method method(String name) throws Exception {
        for (Method m : AbstractCrudController.class.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        throw new NoSuchMethodException(name);
    }

    @Test
    @DisplayName("disable命中的端点被判定关闭")
    void shouldMatchExpectedBehavior001() throws Exception {
        assertTrue(mapping.isDisabled(RestrictedController.class, method("delete")));
        assertTrue(mapping.isDisabled(RestrictedController.class, method("create")));
    }

    @Test
    @DisplayName("未在disable中的端点不关闭")
    void shouldMatchExpectedBehavior002() throws Exception {
        assertFalse(mapping.isDisabled(RestrictedController.class, method("page")));
        assertFalse(mapping.isDisabled(RestrictedController.class, method("get")));
    }

    @Test
    @DisplayName("无CrudApi注解时全部不关闭")
    void shouldMatchExpectedBehavior003() throws Exception {
        assertFalse(mapping.isDisabled(OpenController.class, method("delete")));
    }
}
