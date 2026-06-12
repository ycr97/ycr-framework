package com.ycr.framework.feign.decoder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.core.exception.BizException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * Feign 统一错误解码器
 *
 * <p>把下游统一 {@code R{code, message}} 错误响应解码为框架 {@link BizException}（保留下游 code 与 message），
 * 使上游 {@code GlobalExceptionHandler} 复原业务错误；非框架响应回退 {@link ErrorDecoder.Default}。</p>
 *
 * @author ycr
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();
    private final ObjectMapper objectMapper;

    public FeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        byte[] body = readBody(response);
        if (body != null && body.length > 0) {
            try {
                JsonNode node = objectMapper.readTree(body);
                if (node.has("message")) {
                    String message = node.get("message").asText();
                    String code = node.has("code") ? node.get("code").asText() : String.valueOf(response.status());
                    log.error("Feign 调用失败: {} -> code={}, message={}", methodKey, code, message);
                    return new BizException(code, "[远程调用] " + message);
                }
            } catch (Exception e) {
                log.warn("Feign 错误解码失败，回退默认解码: {}", methodKey, e);
            }
            // body 已被读取消费，重建响应供默认解码器再次读取
            return defaultDecoder.decode(methodKey, response.toBuilder().body(body).build());
        }
        return defaultDecoder.decode(methodKey, response);
    }

    private byte[] readBody(Response response) {
        if (response.body() == null) {
            return null;
        }
        try {
            return response.body().asInputStream().readAllBytes();
        } catch (Exception e) {
            log.warn("读取 Feign 响应体失败", e);
            return null;
        }
    }
}
