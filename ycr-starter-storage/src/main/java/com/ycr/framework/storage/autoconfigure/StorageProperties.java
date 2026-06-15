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

    /** 存储类型：{@code local}（默认）或 {@code s3}（S3 兼容：AWS S3 / MinIO / 阿里云 OSS 等） */
    private String type = "local";

    /** 本地存储配置 */
    private Local local = new Local();

    /** S3 兼容存储配置（{@code type=s3} 时生效） */
    private S3 s3 = new S3();

    @Data
    public static class Local {

        /** 存储根目录；留空则回退 {@code ${java.io.tmpdir}/ycr-storage} */
        private String path = "";

        /** 访问 URL 前缀（拼到存储键之前），如 {@code /files} */
        private String urlPrefix = "/files";
    }

    @Data
    public static class S3 {

        /** 服务端点；AWS 留空走默认，MinIO/OSS 等填如 {@code https://minio.example.com} */
        private String endpoint = "";

        /** 区域，如 {@code us-east-1}、{@code cn-north-1}；MinIO 任意非空即可 */
        private String region = "us-east-1";

        /** 访问密钥 ID */
        private String accessKey = "";

        /** 访问密钥 */
        private String secretKey = "";

        /** 桶名 */
        private String bucket = "";

        /**
         * 是否使用路径风格访问（{@code endpoint/bucket/key}）。
         *
         * <p>MinIO、部分自建 S3 网关需置 {@code true}；AWS S3 用虚拟主机风格，保持 {@code false}。</p>
         */
        private boolean pathStyleAccess = false;

        /**
         * 公网访问 URL 前缀（拼到存储键之前），如 {@code https://cdn.example.com} 或
         * {@code https://bucket.s3.us-east-1.amazonaws.com}。留空则 {@code FileInfo.url} 为空。
         */
        private String urlPrefix = "";
    }
}
