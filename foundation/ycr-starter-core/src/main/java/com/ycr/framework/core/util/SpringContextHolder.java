package com.ycr.framework.core.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public static <T> T getBean(Class<T> requiredType) {
        return requireContext().getBean(requiredType);
    }

    public static Object getBean(String name) {
        return requireContext().getBean(name);
    }

    public static <T> T getBean(String name, Class<T> requiredType) {
        return requireContext().getBean(name, requiredType);
    }

    private static ApplicationContext requireContext() {
        if (context == null) {
            throw new IllegalStateException("Spring ApplicationContext 尚未初始化");
        }
        return context;
    }
}
