package com.ycr.framework.storage.service;

import com.ycr.framework.storage.model.FileInfo;

import java.io.InputStream;

/**
 * 文件存储服务
 *
 * <p>存储后端抽象。业务方可实现此接口替换为 S3/MinIO 等云存储，框架通过 {@code @ConditionalOnMissingBean}
 * 让自定义实现覆盖默认的本地实现。</p>
 *
 * @author ycr
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param content          文件内容流
     * @param originalFilename 原始文件名（用于提取扩展名）
     * @return 文件信息（含存储键 path 与访问 url）
     */
    FileInfo upload(InputStream content, String originalFilename);

    /**
     * 上传已知长度的文件。S3 等远程存储可据此直接流式上传，避免缓冲整个文件。
     *
     * @param content          文件内容流
     * @param contentLength    文件字节数
     * @param originalFilename 原始文件名
     * @return 文件信息
     */
    default FileInfo upload(InputStream content, long contentLength, String originalFilename) {
        return upload(content, originalFilename);
    }

    /**
     * 下载文件
     *
     * @param path 存储键（{@link FileInfo#getPath()}）
     * @return 内容流
     */
    InputStream download(String path);

    /**
     * 删除文件
     *
     * @param path 存储键
     * @return 是否删除了文件（不存在返回 false）
     */
    boolean delete(String path);

    /**
     * 文件是否存在
     *
     * @param path 存储键
     */
    boolean exists(String path);
}
