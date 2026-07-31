package com.ycr.framework.sdk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * AbstractFeignClientService 行为测试
 *
 * @author ycr
 */
class AbstractFeignClientServiceTest {

    interface DummyClient {
        String hello();
    }

    static class DummySdk extends AbstractFeignClientService<DummyClient> {
    }

    @Test
    @DisplayName("getClient应返回注入的Feign客户端")
    void shouldMatchExpectedBehavior001() throws Exception {
        DummySdk sdk = new DummySdk();
        DummyClient client = () -> "hi";

        Field field = AbstractFeignClientService.class.getDeclaredField("feignClient");
        field.setAccessible(true);
        field.set(sdk, client);

        assertSame(client, sdk.getClient());
    }
}
