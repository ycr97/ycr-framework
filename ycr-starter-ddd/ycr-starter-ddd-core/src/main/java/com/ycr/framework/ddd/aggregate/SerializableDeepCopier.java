package com.ycr.framework.ddd.aggregate;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

/**
 * This deepcopier use Apache common lang to clone object. Objects need to implement Serializable interface.
 * Use this deepcopier if your entity has no default constructor and setter methods.
 *
 * @author meixuesong
 */
public class SerializableDeepCopier implements DeepCopier {
    @Override
    @SuppressWarnings("unchecked")
    public <T> T copy(T object) {
        if (object instanceof Serializable) {
            try {
                return (T) SerializationUtils.clone((Serializable) object);
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(String.format("%s should be a serializable object.", object.getClass().getName()), exception);
            }
        }
        throw new IllegalArgumentException(String.format("%s should be a serializable object.", object.getClass().getName()));
    }
}
