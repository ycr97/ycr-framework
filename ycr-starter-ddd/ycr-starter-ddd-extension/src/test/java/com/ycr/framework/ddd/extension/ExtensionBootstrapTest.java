package com.ycr.framework.ddd.extension;

import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ExtensionBootstrap 扫描注册测试（含 AOP 代理安全）
 *
 * @author ycr
 */
class ExtensionBootstrapTest {

    interface PayExtPt extends ExtensionPointI {
        String pay();
    }

    @Extension(bizId = "vip")
    static class VipPay implements PayExtPt {
        @Override
        public String pay() {
            return "vip";
        }
    }

    private void fireRefresh(ExtensionBootstrap bootstrap, Map<String, Object> extBeans) {
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeansWithAnnotation(Extension.class)).thenReturn(extBeans);
        bootstrap.onApplicationEvent(new ContextRefreshedEvent(context));
    }

    @Test
    void 扫描注册普通Extension_按身份可路由() {
        ExtensionRepository repo = new ExtensionRepository();
        VipPay bean = new VipPay();
        fireRefresh(new ExtensionBootstrap(repo), Map.of("vipPay", bean));

        assertSame(bean, repo.getExt(PayExtPt.class, BizScenario.of("vip")));
    }

    @Test
    void 代理安全_AOP代理的Extension仍按目标类注册() {
        ExtensionRepository repo = new ExtensionRepository();
        VipPay target = new VipPay();
        // 构造 Spring AOP 代理（代理类上没有 @Extension，注解在目标类）
        PayExtPt proxy = (PayExtPt) new ProxyFactory(target).getProxy();

        fireRefresh(new ExtensionBootstrap(repo), Map.of("vipPay", proxy));

        // 仍能注册并路由到代理实例（ultimateTargetClass 穿透代理取注解/接口）
        assertSame(proxy, repo.getExt(PayExtPt.class, BizScenario.of("vip")));
    }
}
