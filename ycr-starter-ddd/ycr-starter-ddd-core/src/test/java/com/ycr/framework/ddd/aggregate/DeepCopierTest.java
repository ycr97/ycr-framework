package com.ycr.framework.ddd.aggregate;

import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;

class DeepCopierTest {

    @Data
    static class Box implements Serializable {
        private String label;
        private int[] nums;

        Box(String label, int[] nums) {
            this.label = label;
            this.nums = nums;
        }
    }

    @Test
    @DisplayName("serializableDeepCopier_拷贝与原对象互不影响")
    void shouldMatchExpectedBehavior001() {
        Box src = new Box("a", new int[]{1, 2, 3});
        Box copy = new SerializableDeepCopier().copy(src);

        assertThat(copy).isNotSameAs(src);
        copy.setLabel("b");
        copy.getNums()[0] = 99;

        assertThat(src.getLabel()).isEqualTo("a");
        assertThat(src.getNums()[0]).isEqualTo(1);
    }

    @Test
    @DisplayName("serializableDeepCopier_非Serializable应抛异常")
    void shouldMatchExpectedBehavior002() {
        Object notSerializable = new Object();
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new SerializableDeepCopier().copy(notSerializable));
    }

    @Test
    @DisplayName("javaUtilDeepComparator_深比较语义")
    void shouldMatchExpectedBehavior003() {
        DeepComparator comparator = new JavaUtilDeepComparator();
        Box a = new Box("a", new int[]{1, 2, 3});
        Box b = new Box("a", new int[]{1, 2, 3});
        Box c = new Box("a", new int[]{1, 2, 4});

        assertThat(comparator.isDeepEquals(a, b)).isTrue();
        assertThat(comparator.isDeepEquals(a, c)).isFalse();
    }
}
