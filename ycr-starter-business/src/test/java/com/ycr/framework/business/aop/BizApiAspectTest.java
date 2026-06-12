package com.ycr.framework.business.aop;

import com.ycr.framework.business.annotation.BizApi;
import com.ycr.framework.business.chain.BizContext;
import com.ycr.framework.business.chain.BizInterceptor;
import com.ycr.framework.business.chain.BizInterceptorChain;
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
    void 切面真织入_拦截器绕方法执行且上下文完整() {
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
}
