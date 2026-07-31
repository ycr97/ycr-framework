package com.ycr.framework.context.sign;

/**
 * 旧名称兼容入口，行为已改为 fail-closed。
 *
 * @deprecated 仅保留类型兼容；生产请求不会绕过 nonce 校验。
 *
 * @author ycr
 */
@Deprecated
public class NoopContextReplayGuard extends FailClosedContextReplayGuard {
}
