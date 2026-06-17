package com.ycr.framework.ddd.aggregate;

/**
 * DeepComparator will be used to deep compare two object. Aggregate use it to find out whether entity is changed.
 * @author meixuesong
 */
public interface DeepComparator {
    <T> boolean isDeepEquals(T a, T b);
}
