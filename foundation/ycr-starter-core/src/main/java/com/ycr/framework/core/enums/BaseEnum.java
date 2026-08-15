package com.ycr.framework.core.enums;

import java.io.Serializable;
import java.util.Objects;

public interface BaseEnum<T extends Serializable> {

    T getValue();

    String getDescription();

    static <E extends Enum<E> & BaseEnum<T>, T extends Serializable> E getByValue(T value, Class<E> clazz) {
        for (E e : clazz.getEnumConstants()) {
            if (Objects.equals(e.getValue(), value)) {
                return e;
            }
        }
        return null;
    }
}
