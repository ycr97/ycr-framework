package com.ycr.framework.feign.interceptor;

import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("默认匹配器命中时执行doApply")
    void shouldMatchExpectedBehavior001() {
        RecordingInterceptor interceptor = new RecordingInterceptor();
        interceptor.apply(new RequestTemplate());
        assertTrue(interceptor.applied);
    }

    @Test
    @DisplayName("命中notMatcher时跳过")
    void shouldMatchExpectedBehavior002() {
        RecordingInterceptor interceptor = new RecordingInterceptor();
        interceptor.addNotMatcher(template -> true);
        interceptor.apply(new RequestTemplate());
        assertFalse(interceptor.applied);
    }

    @Test
    @DisplayName("配置了matcher但未命中时跳过")
    void shouldMatchExpectedBehavior003() {
        RecordingInterceptor interceptor = new RecordingInterceptor();
        interceptor.addMatcher(template -> false);
        interceptor.apply(new RequestTemplate());
        assertFalse(interceptor.applied);
    }

    @Test
    @DisplayName("配置了matcher且命中时执行")
    void shouldMatchExpectedBehavior004() {
        RecordingInterceptor interceptor = new RecordingInterceptor();
        interceptor.addMatcher(template -> true);
        interceptor.apply(new RequestTemplate());
        assertTrue(interceptor.applied);
    }

    @Test
    @DisplayName("notMatcher优先级高于matcher")
    void shouldMatchExpectedBehavior005() {
        RecordingInterceptor interceptor = new RecordingInterceptor();
        interceptor.addMatcher(template -> true);
        interceptor.addNotMatcher(template -> true);
        interceptor.apply(new RequestTemplate());
        assertFalse(interceptor.applied);
    }
}
