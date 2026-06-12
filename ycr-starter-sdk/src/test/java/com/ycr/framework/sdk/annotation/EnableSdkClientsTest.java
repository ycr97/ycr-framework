package com.ycr.framework.sdk.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EnableSdkClients 组合注解转发测试
 *
 * @author ycr
 */
class EnableSdkClientsTest {

    @EnableSdkClients(basePackages = "com.example.client")
    static class SampleConfig {
    }

    @Test
    void basePackages应真转发给EnableFeignClients() {
        MergedAnnotation<EnableFeignClients> merged = MergedAnnotations
                .from(SampleConfig.class, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .get(EnableFeignClients.class);

        assertTrue(merged.isPresent(), "应组合出 @EnableFeignClients");
        assertArrayEquals(new String[]{"com.example.client"}, merged.getStringArray("basePackages"));
    }
}
