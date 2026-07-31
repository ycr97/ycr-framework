package com.ycr.framework.ddd.aggregate;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DataObjectUtilsTest {

    @Getter
    @Setter
    public static class SampleDO {
        private String id;
        private Integer length;
        private Double area;
    }

    private SampleDO of(String id, Integer length, Double area) {
        SampleDO o = new SampleDO();
        o.setId(id);
        o.setLength(length);
        o.setArea(area);
        return o;
    }

    @Test
    @DisplayName("getDelta_仅变更字段有值未变更为null")
    void shouldMatchExpectedBehavior001() {
        SampleDO old = of("1", 10, 1.0);
        SampleDO current = of("1", 10, 2.0);

        SampleDO delta = DataObjectUtils.getDelta(old, current);

        assertThat(delta.getId()).isNull();
        assertThat(delta.getLength()).isNull();
        assertThat(delta.getArea()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("getDelta_可忽略指定字段")
    void shouldMatchExpectedBehavior002() {
        SampleDO old = of("1", 10, 1.0);
        SampleDO current = of("1", 20, 2.0);

        SampleDO delta = DataObjectUtils.getDelta(old, current, "length");

        assertThat(delta.getLength()).isNull();
        assertThat(delta.getArea()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("getChangedFields_返回变更字段名集合")
    void shouldMatchExpectedBehavior003() {
        SampleDO old = of("1", 10, 1.0);
        SampleDO current = of("1", 20, 2.0);

        Set<String> changed = DataObjectUtils.getChangedFields(old, current);

        assertThat(changed).containsExactlyInAnyOrder("length", "area");
    }
}
