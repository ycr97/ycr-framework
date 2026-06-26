package com.ycr.framework.log.autoconfigure;

import com.ycr.framework.log.enums.Include;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * 操作日志配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.log")
public class LogProperties {

    /**
     * 是否启用操作日志，默认启用。设为 {@code false} 关闭整条切面链路。
     */
    private boolean enabled = true;

    /**
     * 是否异步落库，默认 {@code false}（同步，取稳妥侧）。
     *
     * <p>置 {@code true} 时处理器在独立线程执行，避免阻塞业务线程；操作人/请求信息在方法执行前已同步
     * 采集进 {@code LogRecord}，因此异步不依赖线程上下文，规避 TTL 线程切换丢身份的问题。</p>
     */
    private boolean async = false;

    /**
     * 全局默认采集项，{@code @Log} 注解的 includes/excludes 在此基础上增减。默认仅采集 IP。
     */
    private Set<Include> includes = new HashSet<>(Set.of(Include.IP_ADDRESS));

    /**
     * 敏感参数键名（不区分大小写），命中后值脱敏为 {@code ******}，避免明文落库泄露。
     */
    private Set<String> sensitiveKeys = new HashSet<>(Set.of("password", "pwd", "idCard", "email", "phone"));

    /**
     * 请求体/响应体序列化的截断上限（字符数），默认 2000，防大 body 撑爆日志。
     */
    private int maxBodyLength = 2000;
}
