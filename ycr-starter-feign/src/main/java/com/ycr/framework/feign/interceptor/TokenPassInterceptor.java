package com.ycr.framework.feign.interceptor;

import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

/**
 * Authorization 原始 token 透传拦截器：把当前入站请求的 {@code Authorization} 头原样转发给下游。
 *
 * <p>ycr 默认走「分解身份」（userId/username/roles/deptId，见 {@link ContextPassInterceptor}），
 * 原始 token 透传属可选模式，由 {@code ycr.feign.token-pass-enabled} 控制，默认关闭。
 * token 取自当前 Servlet 请求头；无请求上下文或头为空时静默跳过。</p>
 *
 * @author ycr
 */
public class TokenPassInterceptor extends AbstractMatchableFeignInterceptor {

    @Override
    protected void doApply(RequestTemplate template) {
        String token = CurrentRequestHelper.header(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(token)) {
            template.header(HttpHeaders.AUTHORIZATION, token);
        }
    }
}
