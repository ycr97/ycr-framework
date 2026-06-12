package com.ycr.framework.ddd.extension;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import java.util.Map;

/**
 * 扩展点引导器 —— 容器刷新时扫描 {@code @Extension} bean 并注册到 {@link ExtensionRepository}
 *
 * <p>代理安全：用 {@link AopProxyUtils#ultimateTargetClass} 取真实类再取注解与接口，避免被 AOP 代理
 * （如扩展上有 {@code @Transactional}）的 bean 因代理类无注解而漏注册。</p>
 *
 * @author ycr
 */
public class ExtensionBootstrap implements ApplicationListener<ContextRefreshedEvent> {

    private final ExtensionRepository repository;

    public ExtensionBootstrap(ExtensionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        Map<String, Object> beans = context.getBeansWithAnnotation(Extension.class);
        for (Object bean : beans.values()) {
            registerBean(bean);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerBean(Object bean) {
        // 取真实目标类（穿透 AOP 代理）
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        Extension annotation = AnnotationUtils.findAnnotation(targetClass, Extension.class);
        if (annotation == null) {
            return;
        }
        BizScenario scenario = BizScenario.of(annotation.bizId(), annotation.useCase(), annotation.scenario());
        for (Class<?> iface : ClassUtils.getAllInterfacesForClass(targetClass)) {
            if (ExtensionPointI.class.isAssignableFrom(iface) && iface != ExtensionPointI.class) {
                repository.register((Class) iface, scenario, (ExtensionPointI) bean);
            }
        }
    }
}
