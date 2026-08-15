package com.ycr.framework.context.resolver;

import com.ycr.framework.context.enums.SecurityMode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户上下文解析请求。
 *
 * @author ycr
 */
@Data
@AllArgsConstructor
public class UserContextResolveRequest {

    private HttpServletRequest request;

    private SecurityMode securityMode;

    private String traceId;
}
