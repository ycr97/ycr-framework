package com.ycr.framework.feign.interceptor;

import feign.RequestTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AbstractMatchableFeignInterceptor 匹配分发逻辑测试
 *
 * @author ycr
 */
class AbstractMatchableFeignInterceptorTest {

    /** 记录是否真正执行了 doApply 的桩拦截器 */
    static class RecordingInterceptor extends AbstractMatchableFeignInterceptor {
        boolean applied = false;

        @Override
        protected void doApply(RequestTemplate template) {
            applied = true;
        }
    }

    @Test
    void 默认匹配器命中时执行doApply() {
        RecordingInterceptor interceptor = new RecordingInterceptor();
        interceptor.apply(new RequestTemplate());
        assertTrue(interceptor.applied);
    }

    @Test
    void 命中notMatcher时跳过() {
        RecordingInterceptor interceptor = new RecordingInterceptor();
        interceptor.addNotMatcher(template -> true);
        interceptor.apply(new RequestTemplate());
        assertFalse(interceptor.applied);
    }

    @Test
    void 配置了matcher但未命中时跳过() {
        RecordingInterceptor interceptor = new RecordingInterceptor();
        interceptor.addMatcher(template -> false);
        interceptor.apply(new RequestTemplate());
        assertFalse(interceptor.applied);
    }

    @Test
    void 配置了matcher且命中时执行() {
        RecordingInterceptor interceptor = new RecordingInterceptor();
        interceptor.addMatcher(template -> true);
        interceptor.apply(new RequestTemplate());
        assertTrue(interceptor.applied);
    }

    @Test
    void notMatcher优先级高于matcher() {
        RecordingInterceptor interceptor = new RecordingInterceptor();
        interceptor.addMatcher(template -> true);
        interceptor.addNotMatcher(template -> true);
        interceptor.apply(new RequestTemplate());
        assertFalse(interceptor.applied);
    }
}
