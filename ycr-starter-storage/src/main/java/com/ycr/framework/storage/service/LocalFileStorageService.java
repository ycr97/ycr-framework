package com.ycr.framework.storage.service;

import com.ycr.framework.storage.model.FileInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * 本地文件系统存储实现
 *
 * <p>文件写到配置的根目录下，按「日期目录 + UUID + 原扩展名」生成存储键防冲突。下载/删除/存在均经
 * {@link #resolveSafe} 校验解析路径落在根目录内，防路径穿越。</p>
 *
 * @author ycr
 */
public class LocalFileStorageService implements FileStorageService {

    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final Path root;
    private final String urlPrefix;

    public LocalFileStorageService(String rootPath, String urlPrefix) {
        String base = (rootPath == null || rootPath.isBlank())
                ? System.getProperty("java.io.tmpdir") + "/ycr-storage"
                : rootPath;
        this.root = Paths.get(base).toAbsolutePath().normalize();
        this.urlPrefix = stripTrailingSlash(urlPrefix == null ? "" : urlPrefix);
    }

    @Override
    public FileInfo upload(InputStream content, String originalFilename) {
        String extension = extension(originalFilename);
        String storedName = UUID.randomUUID().toString().replace("-", "")
                + (extension.isEmpty() ? "" : "." + extension);
        String relativePath = LocalDate.now().format(DATE_DIR) + "/" + storedName;
        Path target = root.resolve(relativePath).normalize();
        try {
            Files.createDirectories(target.getParent());
            long size = Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);

            FileInfo info = new FileInfo();
            info.setOriginalFilename(originalFilename);
            info.setFilename(storedName);
            info.setPath(relativePath);
            info.setExtension(extension);
            info.setSize(size);
            info.setUrl(urlPrefix + "/" + relativePath);
            return info;
        } catch (IOException e) {
            throw new IllegalStateException("文件上传失败: " + originalFilename, e);
        }
    }

    @Override
    public InputStream download(String path) {
        Path target = resolveSafe(path);
        try {
            return Files.newInputStream(target);
        } catch (IOException e) {
            throw new IllegalStateException("文件下载失败: " + path, e);
        }
    }

    @Override
    public boolean delete(String path) {
        Path target = resolveSafe(path);
        try {
            return Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new IllegalStateException("文件删除失败: " + path, e);
        }
    }

    @Override
    public boolean exists(String path) {
        return Files.exists(resolveSafe(path));
    }

    /** 解析存储键为绝对路径并校验落在根目录内，防路径穿越 */
    private Path resolveSafe(String path) {
        Path target = root.resolve(path).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("非法的存储路径: " + path);
        }
        return target;
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
