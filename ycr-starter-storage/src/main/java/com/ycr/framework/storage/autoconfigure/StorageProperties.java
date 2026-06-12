package com.ycr.framework.storage.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.storage")
public class StorageProperties {

    /** 是否启用文件存储，默认启用 */
    private boolean enabled = true;

    /** 存储类型，默认 local；后续可扩展 s3/minio 等 */
    private String type = "local";

    /** 本地存储配置 */
    private Local local = new Local();

    @Data
    public static class Local {

        /** 存储根目录；留空则回退 {@code ${java.io.tmpdir}/ycr-storage} */
        private String path = "";

        /** 访问 URL 前缀（拼到存储键之前），如 {@code /files} */
        private String urlPrefix = "/files";
    }
}
