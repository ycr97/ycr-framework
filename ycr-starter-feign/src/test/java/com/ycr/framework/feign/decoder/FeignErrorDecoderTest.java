package com.ycr.framework.feign.decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.core.exception.BizException;
import feign.Request;
import feign.Response;
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
    void 下游R响应应解码为BizException() {
        Response response = responseWithBody("{\"code\":\"USER_001\",\"message\":\"用户不存在\"}");

        Exception ex = decoder.decode("UserClient#get()", response);

        assertInstanceOf(BizException.class, ex);
        assertEquals("USER_001", ((BizException) ex).getCode());
        assertTrue(ex.getMessage().contains("用户不存在"));
    }

    @Test
    void 非框架响应应回退默认解码器() {
        Response response = responseWithBody("{\"foo\":\"bar\"}");

        Exception ex = decoder.decode("UserClient#get()", response);

        assertFalse(ex instanceof BizException, "无 message 字段应回退默认解码");
        assertInstanceOf(feign.FeignException.class, ex);
    }
}
