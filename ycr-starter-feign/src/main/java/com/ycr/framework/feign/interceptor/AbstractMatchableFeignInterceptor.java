package com.ycr.framework.feign.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;

/**
 * 可选择性匹配的 Feign 拦截器基类。
 *
 * <p>实现 feign 原生 {@link RequestInterceptor}：先按规则判定是否对当前请求生效，命中再执行子类的
 * {@link #doApply(RequestTemplate)}。匹配语义——命中任一 notMatcher 直接跳过；否则若配置了 matcher，
 * 需命中其一才执行；未配置任何 matcher 时按默认匹配器（默认 {@link RequestTemplateMatchers#allMatch()}）。</p>
 *
 * @author ycr
 */
public abstract class AbstractMatchableFeignInterceptor implements RequestInterceptor, Ordered {

    private final List<RequestTemplateMatcher> matchers = new ArrayList<>();
    private final List<RequestTemplateMatcher> notMatchers = new ArrayList<>();
    private RequestTemplateMatcher defaultMatcher = RequestTemplateMatchers.allMatch();
    private int order = Ordered.LOWEST_PRECEDENCE;

    public AbstractMatchableFeignInterceptor addMatcher(RequestTemplateMatcher matcher) {
        matchers.add(matcher);
        return this;
    }

    public AbstractMatchableFeignInterceptor addNotMatcher(RequestTemplateMatcher matcher) {
        notMatchers.add(matcher);
        return this;
    }

    public void setDefaultMatcher(RequestTemplateMatcher defaultMatcher) {
        this.defaultMatcher = defaultMatcher;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (notMatchers.stream().anyMatch(matcher -> matcher.match(template))) {
            return;
        }
        if (!matchers.isEmpty()) {
            if (matchers.stream().noneMatch(matcher -> matcher.match(template))) {
                return;
            }
        } else if (defaultMatcher == null || !defaultMatcher.match(template)) {
            return;
        }
        doApply(template);
    }

    /**
     * 命中匹配后执行的实际透传逻辑。
     *
     * @param template feign 请求模板
     */
    protected abstract void doApply(RequestTemplate template);
}
