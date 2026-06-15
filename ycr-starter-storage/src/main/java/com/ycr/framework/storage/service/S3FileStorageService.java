package com.ycr.framework.storage.service;

import com.ycr.framework.storage.model.FileInfo;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * S3 兼容对象存储实现（AWS S3 / MinIO / 阿里云 OSS 等）
 *
 * <p>基于 AWS SDK v2 同步客户端 {@link S3Client}。存储键与本地实现一致（「日期目录 + UUID + 原扩展名」），
 * 保证两种后端可平滑切换。{@link S3Client} 由自动配置创建并管理生命周期，本类只使用不负责关闭。</p>
 *
 * @author ycr
 */
public class S3FileStorageService implements FileStorageService {

    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final S3Client s3Client;
    private final String bucket;
    private final String urlPrefix;

    public S3FileStorageService(S3Client s3Client, String bucket, String urlPrefix) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("S3 存储未配置 bucket（ycr.storage.s3.bucket）");
        }
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.urlPrefix = stripTrailingSlash(urlPrefix == null ? "" : urlPrefix);
    }

    @Override
    public FileInfo upload(InputStream content, String originalFilename) {
        byte[] bytes;
        try {
            bytes = content.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("文件读取失败: " + originalFilename, e);
        }
        String extension = extension(originalFilename);
        String storedName = UUID.randomUUID().toString().replace("-", "")
                + (extension.isEmpty() ? "" : "." + extension);
        String key = LocalDate.now().format(DATE_DIR) + "/" + storedName;

        PutObjectRequest.Builder request = PutObjectRequest.builder().bucket(bucket).key(key);
        String contentType = URLConnection.guessContentTypeFromName(originalFilename);
        if (contentType != null) {
            request.contentType(contentType);
        }
        s3Client.putObject(request.build(), RequestBody.fromBytes(bytes));

        FileInfo info = new FileInfo();
        info.setOriginalFilename(originalFilename);
        info.setFilename(storedName);
        info.setPath(key);
        info.setExtension(extension);
        info.setSize(bytes.length);
        info.setUrl(urlPrefix.isEmpty() ? "" : urlPrefix + "/" + key);
        return info;
    }

    @Override
    public InputStream download(String path) {
        try {
            return s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(path).build());
        } catch (NoSuchKeyException e) {
            throw new IllegalStateException("文件不存在: " + path, e);
        }
    }

    @Override
    public boolean delete(String path) {
        if (!exists(path)) {
            return false;
        }
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(path).build());
        return true;
    }

    @Override
    public boolean exists(String path) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(path).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    private String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
