package com.ycr.framework.ddd.aggregate.deepequals;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * The recursive object used by DeepEquals
 * Based on the deep equals implementation of https://github.com/jdereg/java-util
 *
 * @author John DeRegnaucourt (john@cedarsoftware.com)
 * @author meixuesong
 */
class RecursiveObject {
    private Set<DualObject> visited = new HashSet<>();
    private Stack<DualObject> stack = new Stack<>();

    private void addVisited(DualObject object) {
        visited.add(object);
    }

    public void push(DualObject dk) {
        if (!visited.contains(dk)) {
            stack.push(dk);
        }
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public DualObject pop() {
        DualObject object = stack.pop();
        addVisited(object);

        return object;
    }
}
