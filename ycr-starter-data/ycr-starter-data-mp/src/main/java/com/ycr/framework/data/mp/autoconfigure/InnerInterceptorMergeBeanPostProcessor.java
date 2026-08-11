package com.ycr.framework.data.mp.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

/**
 * 将独立的 {@link InnerInterceptor} Bean 合并到用户自定义的 MyBatis-Plus 聚合拦截器中。
 *
 * @author ycr
 */
final class InnerInterceptorMergeBeanPostProcessor implements BeanPostProcessor {

    private final ObjectProvider<InnerInterceptor> innerInterceptors;

    InnerInterceptorMergeBeanPostProcessor(ObjectProvider<InnerInterceptor> innerInterceptors) {
        this.innerInterceptors = innerInterceptors;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (!(bean instanceof MybatisPlusInterceptor interceptor)) {
            return bean;
        }
        List<InnerInterceptor> existing = new ArrayList<>(interceptor.getInterceptors());
        int originalSize = existing.size();
        innerInterceptors.orderedStream()
                .sorted(Comparator.comparingInt(this::priority))
                .filter(candidate -> existing.stream().noneMatch(current -> equivalent(current, candidate)))
                .forEach(candidate -> existing.add(insertionIndex(existing), candidate));
        if (existing.size() != originalSize) {
            interceptor.setInterceptors(existing);
        }
        return bean;
    }

    private int insertionIndex(List<InnerInterceptor> interceptors) {
        for (int i = 0; i < interceptors.size(); i++) {
            if (interceptors.get(i) instanceof PaginationInnerInterceptor) {
                return i;
            }
        }
        return interceptors.size();
    }

    private boolean equivalent(InnerInterceptor current, InnerInterceptor candidate) {
        return current == candidate || current.getClass() == candidate.getClass();
    }

    private int priority(InnerInterceptor interceptor) {
        if (interceptor instanceof TenantLineInnerInterceptor) {
            return 100;
        }
        if (interceptor instanceof DataPermissionInterceptor) {
            return 200;
        }
        return 300;
    }
}
