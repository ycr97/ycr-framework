package com.ycr.framework.auth.oauth2.integration;

import com.ycr.framework.context.constant.ContextMdcConstants;
import com.ycr.framework.context.holder.TenantContextHolder;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.security.annotation.RequirePermission;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
class OAuth2WebTestController {

    @GetMapping("/api/public")
    Map<String, Object> publicEndpoint() {
        return Map.of("value", "public");
    }

    @GetMapping("/api/context")
    Map<String, Object> contextEndpoint() {
        UserContext context = UserContextHolder.get();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", context == null ? null : context.getUserId());
        result.put("username", context == null ? null : context.getUsername());
        result.put("tenantId", context == null ? null : context.getTenantId());
        result.put("clientId", context == null ? null : context.getClientId());
        result.put("source", context == null ? null : context.getSource());
        result.put("tenantContextId", TenantContextHolder.get() == null
                ? null : TenantContextHolder.get().getTenantId());
        result.put("mdcUserId", MDC.get(ContextMdcConstants.USER_ID));
        return result;
    }

    @RequirePermission("order:read")
    @GetMapping("/api/permission")
    Map<String, Object> permissionEndpoint() {
        return Map.of("value", "permission-granted");
    }
}
