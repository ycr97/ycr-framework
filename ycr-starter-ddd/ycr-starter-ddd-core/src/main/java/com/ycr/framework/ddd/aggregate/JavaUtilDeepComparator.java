package com.ycr.framework.ddd.aggregate;

import com.ycr.framework.ddd.aggregate.deepequals.DeepEquals;

/**
 * JavaUtilDeepComparator use deepEquals, which is based on https://github.com/jdereg/java-util, to implement the DeepComparator interface.
 *
 * @author meixuesong
 */
public class JavaUtilDeepComparator implements DeepComparator {
    @Override
    public <T> boolean isDeepEquals(T a, T b) {
        return new DeepEquals().isDeepEquals(a, b);
    }
}
