package com.ycr.framework.context.propagation;

import org.springframework.core.task.TaskDecorator;

import java.util.ArrayList;
import java.util.List;

/**
 * 在任务提交时捕获上下文，执行时恢复，并在结束后还原执行线程原状态。
 *
 * @author ycr
 */
public class ContextTaskDecorator implements TaskDecorator {

    private final List<ThreadContextAccessor> accessors;

    public ContextTaskDecorator(List<ThreadContextAccessor> accessors) {
        this.accessors = List.copyOf(accessors);
    }

    @Override
    public Runnable decorate(Runnable runnable) {
        List<Object> captured = captureAll();
        return () -> {
            List<Object> previous = captureAll();
            restoreAll(captured);
            try {
                runnable.run();
            } finally {
                restoreAll(previous);
            }
        };
    }

    private List<Object> captureAll() {
        List<Object> captured = new ArrayList<>(accessors.size());
        for (ThreadContextAccessor accessor : accessors) {
            captured.add(accessor.capture());
        }
        return captured;
    }

    private void restoreAll(List<Object> captured) {
        for (int i = accessors.size() - 1; i >= 0; i--) {
            accessors.get(i).restore(captured.get(i));
        }
    }
}
