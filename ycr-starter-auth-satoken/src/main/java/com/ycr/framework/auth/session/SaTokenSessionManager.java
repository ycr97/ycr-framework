package com.ycr.framework.auth.session;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;

import java.util.HashSet;

/**
 * Sa-Token 登录会话生命周期管理器。
 *
 * <p>当前身份与权限读取统一使用 security 模块的 SecurityUtils；本类只负责登录、登出以及
 * Sa-Token 会话中的 UserContext 存取。</p>
 *
 * @author ycr
 */
public class SaTokenSessionManager {

    public static final String SESSION_KEY_USER_CONTEXT = "ycr_user_context";

    /**
     * 使用 Sa-Token 默认登录参数建立会话。
     */
    public SaTokenInfo login(UserContext userContext) {
        return login(userContext, new SaLoginParameter());
    }

    /**
     * 使用指定 Sa-Token 登录参数建立会话。
     */
    public SaTokenInfo login(UserContext userContext, SaLoginParameter loginParameter) {
        if (userContext == null || userContext.getUserId() == null) {
            throw new IllegalArgumentException("登录用户上下文及其 userId 不能为空");
        }
        if (loginParameter == null) {
            throw new IllegalArgumentException("Sa-Token 登录参数不能为空");
        }
        StpUtil.login(userContext.getUserId(), loginParameter);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        UserContext sessionContext = copyOf(userContext);
        StpUtil.getStpLogic()
                .getTokenSessionByToken(tokenInfo.getTokenValue(), true)
                .set(SESSION_KEY_USER_CONTEXT, sessionContext);
        UserContextHolder.set(copyOf(sessionContext));
        return tokenInfo;
    }

    /**
     * 注销当前 Sa-Token 登录态，并清理当前线程身份。
     */
    public void logout() {
        try {
            StpUtil.logout();
        } finally {
            UserContextHolder.clear();
        }
    }

    /**
     * 根据原始 token 恢复其绑定的用户上下文，不依赖当前 Servlet 请求线程。
     */
    public UserContext findUserContext(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) {
            return null;
        }
        StpLogic stpLogic = StpUtil.getStpLogic();
        Object loginId = stpLogic.getLoginIdByToken(tokenValue);
        if (loginId == null) {
            return null;
        }
        SaSession session = stpLogic.getTokenSessionByToken(tokenValue, false);
        UserContext userContext = session == null
                ? null
                : session.getModel(SESSION_KEY_USER_CONTEXT, UserContext.class);
        return copyOf(userContext);
    }

    private UserContext copyOf(UserContext source) {
        if (source == null) {
            return null;
        }
        UserContext copy = new UserContext();
        copy.setUserId(source.getUserId());
        copy.setUsername(source.getUsername());
        copy.setNickname(source.getNickname());
        copy.setTenantId(source.getTenantId());
        copy.setDeptId(source.getDeptId());
        copy.setRoles(source.getRoles() == null ? null : new HashSet<>(source.getRoles()));
        copy.setPermissions(source.getPermissions() == null ? null : new HashSet<>(source.getPermissions()));
        copy.setClientId(source.getClientId());
        copy.setSource(source.getSource());
        return copy;
    }
}
