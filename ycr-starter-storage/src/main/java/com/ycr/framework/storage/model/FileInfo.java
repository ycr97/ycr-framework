package com.ycr.framework.storage.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件信息
 *
 * @author ycr
 */
@Data
public class FileInfo implements Serializable {

    /** 原始文件名 */
    private String originalFilename;

    /** 存储文件名（UUID + 扩展名） */
    private String filename;

    /** 存储键（相对路径，如 {@code 2026/06/11/uuid.png}），下载/删除以此为入参 */
    private String path;

    /** 访问 URL（urlPrefix + 存储键），静态服务由宿主应用提供 */
    private String url;

    /** 文件大小（字节） */
    private long size;

    /** 扩展名（小写，不含点） */
    private String extension;
}
