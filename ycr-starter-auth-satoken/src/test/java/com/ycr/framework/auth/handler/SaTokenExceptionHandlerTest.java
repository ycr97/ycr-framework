package com.ycr.framework.auth.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class SaTokenExceptionHandlerTest {

    private final SaTokenExceptionHandler handler = new SaTokenExceptionHandler();

    @Test
    @DisplayName("无效Token异常日志不得包含Token原文")
    void invalidTokenExceptionLogShouldNotContainRawToken(CapturedOutput output) {
        String rawToken = "secret-token-123";
        NotLoginException exception = new NotLoginException(
                "token 无效：" + rawToken,
                StpUtil.TYPE,
                NotLoginException.INVALID_TOKEN);

        handler.handleNotLogin(exception);

        assertThat(output).contains("未登录").doesNotContain(rawToken);
    }

    @Test
    @DisplayName("Sa-Token异常日志不得包含Token原文")
    void saTokenExceptionLogShouldNotContainRawToken(CapturedOutput output) {
        String rawToken = "secret-token-456";

        handler.handleSaToken(new SaTokenException(rawToken));

        assertThat(output).contains("认证异常").doesNotContain(rawToken);
    }
}
