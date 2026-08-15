package com.ycr.framework.business.aop;

import com.ycr.framework.business.annotation.BizApi;
import com.ycr.framework.business.chain.BizContext;
import com.ycr.framework.business.chain.BizInterceptor;
import com.ycr.framework.business.chain.BizInterceptorChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * BizApiAspect 织入测试：经 AspectJ 代理真实绕方法执行，断言上下文与生命周期
 *
 * @author ycr
 */
class BizApiAspectTest {

    static class OrderService {
        @BizApi("createOrder")
        public String create(String name) {
            return "created:" + name;
        }
    }

    @Test
    @DisplayName("切面真织入_拦截器绕方法执行且上下文完整")
    void shouldMatchExpectedBehavior001() {
        List<String> trace = new ArrayList<>();
        BizContext[] captured = new BizContext[1];

        BizInterceptor recorder = new BizInterceptor() {
            @Override
            public void before(BizContext c) {
                trace.add("before:" + c.getName());
                captured[0] = c;
            }

            @Override
            public void after(BizContext c) {
                trace.add("after:" + c.getResult());
            }
        };
        BizInterceptorChain chain = new BizInterceptorChain(List.of(recorder));

        AspectJProxyFactory factory = new AspectJProxyFactory(new OrderService());
        factory.addAspect(new BizApiAspect(chain));
        OrderService proxy = factory.getProxy();

        String result = proxy.create("A");

        assertEquals("created:A", result);
        // before 用 @BizApi 名、after 拿到返回值
        assertEquals(List.of("before:createOrder", "after:created:A"), trace);
        // 上下文携带方法元数据与参数
        assertEquals("create", captured[0].getMethod().getName());
        assertEquals("A", captured[0].getArgs()[0]);
    }

    @Test
    @DisplayName("before改写入参_目标方法以新参执行")
    void shouldMatchExpectedBehavior002() {
        // 在 before 阶段把入参规整为大写，目标方法应收到改写后的值
        BizInterceptor normalizer = new BizInterceptor() {
            @Override
            public void before(BizContext c) {
                Object[] args = c.getArgs();
                args[0] = ((String) args[0]).toUpperCase();
                c.setArgs(args);
            }
        };
        BizInterceptorChain chain = new BizInterceptorChain(List.of(normalizer));

        AspectJProxyFactory factory = new AspectJProxyFactory(new OrderService());
        factory.addAspect(new BizApiAspect(chain));
        OrderService proxy = factory.getProxy();

        assertEquals("created:ABC", proxy.create("abc"));
    }
}
