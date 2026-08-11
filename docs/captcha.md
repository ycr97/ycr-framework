# 图形验证码

`ycr-starter-captcha` 基于 Hutool 生成图形验证码，验证码答案存 Redis（带过期）。需 Redis（传递依赖 `ycr-starter-cache`）。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-captcha</artifactId>
</dependency>
```

## 配置

前缀 `ycr.captcha`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.captcha.enabled` | `false` | 是否启用，须显式开启 |
| `ycr.captcha.width` | `130` | 图片宽度 |
| `ycr.captcha.height` | `48` | 图片高度 |
| `ycr.captcha.code-count` | `4` | 验证码字符数 |
| `ycr.captcha.line-count` | `5` | 干扰线条数 |
| `ycr.captcha.expiration-seconds` | `120` | 答案有效期（秒） |
| `ycr.captcha.key-prefix` | `ycr:captcha` | Redis 键前缀 |

## 用法

注入 `CaptchaService`：

```java
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {
    private final CaptchaService captchaService;

    @GetMapping
    public R<CaptchaResult> get() {
        return R.ok(captchaService.generate());   // id + imageBase64
    }

    @PostMapping("/verify")
    public R<Void> verify(@RequestParam String id, @RequestParam String code) {
        if (!captchaService.verify(id, code)) {
            throw new BizException(/* 你的错误码 */);
        }
        return R.ok();
    }
}
```

`CaptchaResult`：`id`（校验凭据，提交时回传）、`imageBase64`（前端 `<img src>` 直接渲染）。`verify` 使用 Redis 原子获取并删除，无论成功或失败均只能校验一次。显式开启但容器中没有 `RedissonClient` 时应用拒绝启动。

> 验证码接口通常需要匿名访问，请在网关、Spring Security 或业务认证过滤器中配置放行。
