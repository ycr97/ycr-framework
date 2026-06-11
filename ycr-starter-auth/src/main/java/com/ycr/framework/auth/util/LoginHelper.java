package com.ycr.framework.auth.util;

import cn.dev33.satoken.stp.StpUtil;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;

/**
 * 登录辅助工具类
 *
 * <p>封装 SaToken 登录态与框架 {@link UserContext} 的双向联动：</p>
 * <ul>
 *     <li><b>登录</b>：执行 SaToken 登录签发 token，并把完整用户上下文写入 Sa 会话，
 *         同时填充当前线程的 {@link UserContextHolder}。</li>
 *     <li><b>还原</b>：当当前线程尚无上下文（例如未经 ContextFilter 还原、直接凭 token 调用）时，
 *         从 SaToken 会话懒加载还原用户上下文。</li>
 *     <li><b>登出</b>：注销 SaToken 登录态并清理当前线程上下文。</li>
 * </ul>
 *
 * @author ycr
 */
public final class LoginHelper {

    /** 用户上下文在 SaToken 会话中的存储键 */
    public static final String SESSION_KEY_USER_CONTEXT = "ycr_user_context";

    private LoginHelper() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    /**
     * 执行登录：签发 token、写入会话、填充当前线程上下文。
     *
     * @param userContext 用户上下文，{@code userId} 不能为空
     */
    public static void login(UserContext userContext) {
        if (userContext == null || userContext.getUserId() == null) {
            throw new IllegalArgumentException("登录用户上下文及其 userId 不能为空");
        }
        // 以 userId 作为 SaToken 登录主体，签发 token 并建立会话
        StpUtil.login(userContext.getUserId());
        // 将完整用户上下文存入会话，供后续凭 token 的请求还原
        StpUtil.getSession().set(SESSION_KEY_USER_CONTEXT, userContext);
        // 填充当前线程，便于本次登录请求内直接读取
        UserContextHolder.set(userContext);
    }

    /**
     * 登出：注销 SaToken 登录态，并清理当前线程上下文（清理在 finally 中保证执行）。
     */
    public static void logout() {
        try {
            StpUtil.logout();
        } finally {
            UserContextHolder.clear();
        }
    }

    /**
     * 当前是否已登录
     */
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }

    /**
     * 获取当前 token 值
     */
    public static String getTokenValue() {
        return StpUtil.getTokenValue();
    }

    /**
     * 获取当前用户上下文。
     *
     * <p>优先返回当前线程已有的上下文；若线程内为空但 SaToken 判定为已登录，
     * 则从会话懒加载还原并回填线程，以兼容「未经 Filter 还原、直接凭 token 调用」的场景。</p>
     *
     * @return 用户上下文，未登录时返回 {@code null}
     */
    public static UserContext getUserContext() {
        UserContext ctx = UserContextHolder.get();
        if (ctx != null) {
            return ctx;
        }
        if (StpUtil.isLogin()) {
            UserContext restored = StpUtil.getSession().getModel(SESSION_KEY_USER_CONTEXT, UserContext.class);
            if (restored != null) {
                UserContextHolder.set(restored);
            }
            return restored;
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getUserId() {
        UserContext ctx = getUserContext();
        return ctx != null ? ctx.getUserId() : null;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getUsername() {
        UserContext ctx = getUserContext();
        return ctx != null ? ctx.getUsername() : null;
    }

    /**
     * 直接设置当前线程用户上下文（不触发 SaToken 登录，供 Filter/手动场景使用）
     */
    public static void setUserContext(UserContext userContext) {
        UserContextHolder.set(userContext);
    }

    /**
     * 清除当前线程用户上下文（不注销 SaToken 登录态）
     */
    public static void clearContext() {
        UserContextHolder.clear();
    }
}
