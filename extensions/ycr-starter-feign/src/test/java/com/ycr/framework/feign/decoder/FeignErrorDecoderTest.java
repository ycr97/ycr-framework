package com.ycr.framework.feign.decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.core.exception.BizException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FeignErrorDecoder 行为测试（构造真实 feign.Response）
 *
 * @author ycr
 */
class FeignErrorDecoderTest {

    private final FeignErrorDecoder decoder = new FeignErrorDecoder(new ObjectMapper());

    private Response responseWithBody(String body) {
        Request request = Request.create(Request.HttpMethod.GET, "http://svc/api",
                new HashMap<>(), null, StandardCharsets.UTF_8);
        return Response.builder()
                .status(400)
                .reason("Bad Request")
                .request(request)
                .headers(new HashMap<>())
                .body(body, StandardCharsets.UTF_8)
                .build();
    }

    @Test
    @DisplayName("下游R响应应解码为BizException")
    void shouldMatchExpectedBehavior001() {
        Response response = responseWithBody("{\"code\":\"USER_001\",\"msg\":\"用户不存在\"}");

        Exception ex = decoder.decode("UserClient#get()", response);

        assertInstanceOf(BizException.class, ex);
        assertEquals("USER_001", ((BizException) ex).getCode());
        assertTrue(ex.getMessage().contains("用户不存在"));
    }

    @Test
    @DisplayName("下游旧Message字段也应兼容解码为BizException")
    void shouldMatchExpectedBehavior002() {
        Response response = responseWithBody("{\"code\":\"USER_002\",\"message\":\"用户禁用\"}");

        Exception ex = decoder.decode("UserClient#get()", response);

        assertInstanceOf(BizException.class, ex);
        assertEquals("USER_002", ((BizException) ex).getCode());
        assertTrue(ex.getMessage().contains("用户禁用"));
    }

    @Test
    @DisplayName("非框架响应应回退默认解码器")
    void shouldMatchExpectedBehavior003() {
        Response response = responseWithBody("{\"foo\":\"bar\"}");

        Exception ex = decoder.decode("UserClient#get()", response);

        assertFalse(ex instanceof BizException, "无 msg/message 字段应回退默认解码");
        assertInstanceOf(feign.FeignException.class, ex);
    }
}
