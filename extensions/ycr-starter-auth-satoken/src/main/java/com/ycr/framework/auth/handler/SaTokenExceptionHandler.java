package com.ycr.framework.auth.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import com.ycr.framework.core.model.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * SaToken 异常处理器
 *
 * @author ycr
 */
@Slf4j
@RestControllerAdvice
@Order(-1)
public class SaTokenExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<Void> handleNotLogin(NotLoginException e) {
        log.warn("未登录，异常类型={}", e.getClass().getSimpleName());
        return R.fail(401, "未登录或登录已过期");
    }

    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleNotPermission(NotPermissionException e) {
        log.warn("权限不足: {}", e.getPermission());
        return R.fail(403, "权限不足");
    }

    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleNotRole(NotRoleException e) {
        log.warn("角色不足: {}", e.getRole());
        return R.fail(403, "权限不足");
    }

    @ExceptionHandler(SaTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<Void> handleSaToken(SaTokenException e) {
        log.warn("认证异常，异常类型={}", e.getClass().getSimpleName());
        return R.fail(401, "认证异常");
    }
}
