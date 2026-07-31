package com.ycr.framework.ddd.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 领域注解组合与 value 真转发测试
 *
 * @author ycr
 */
class DomainServiceAnnotationTest {

    @DomainService("orderDomainService")
    static class SampleDomainService {
    }

    @ApplicationService("orderAppService")
    static class SampleAppService {
    }

    @Test
    @DisplayName("DomainService组合出Service且value真转发")
    void shouldMatchExpectedBehavior001() {
        MergedAnnotation<Service> service = MergedAnnotations
                .from(SampleDomainService.class, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .get(Service.class);

        assertTrue(service.isPresent(), "应组合出 @Service");
        assertEquals("orderDomainService", service.getString("value"));
    }

    @Test
    @DisplayName("ApplicationService组合出Service且value真转发")
    void shouldMatchExpectedBehavior002() {
        MergedAnnotation<Service> service = MergedAnnotations
                .from(SampleAppService.class, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .get(Service.class);

        assertTrue(service.isPresent());
        assertEquals("orderAppService", service.getString("value"));
    }
}
