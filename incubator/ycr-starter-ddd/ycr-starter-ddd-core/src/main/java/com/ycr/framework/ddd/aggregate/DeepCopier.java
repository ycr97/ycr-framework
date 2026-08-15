package com.ycr.framework.ddd.aggregate;

/**
 * DeepCopier is used to deep copy object. Aggregate use it to create aggregate root snapshot.
 *
 * @author meixuesong
 */
public interface DeepCopier {
    <T> T copy(T object);
}
