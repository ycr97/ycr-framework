package com.ycr.framework.storage.service;

import com.ycr.framework.storage.model.FileInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LocalFileStorageService 真实文件系统行为测试
 *
 * @author ycr
 */
class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageService service;

    @BeforeEach
    void setUp() {
        service = new LocalFileStorageService(tempDir.toString(), "/files");
    }

    @Test
    void 上传下载应往返一致并填充FileInfo() throws Exception {
        byte[] bytes = "hello-存储".getBytes(StandardCharsets.UTF_8);
        FileInfo info = service.upload(new ByteArrayInputStream(bytes), "photo.PNG");

        assertNotNull(info.getPath());
        assertTrue(info.getFilename().endsWith(".png"));
        assertEquals("png", info.getExtension());
        assertEquals(bytes.length, info.getSize());
        assertTrue(info.getUrl().startsWith("/files/"));
        assertTrue(info.getUrl().contains(info.getPath()));
        assertTrue(service.exists(info.getPath()), "上传后磁盘应存在");

        try (InputStream in = service.download(info.getPath())) {
            assertArrayEquals(bytes, in.readAllBytes());
        }
    }

    @Test
    void 删除后应不存在() {
        FileInfo info = service.upload(new ByteArrayInputStream("x".getBytes()), "a.txt");
        assertTrue(service.exists(info.getPath()));

        assertTrue(service.delete(info.getPath()));
        assertFalse(service.exists(info.getPath()));
    }

    @Test
    void 下载越权路径应被拒绝() {
        assertThrows(IllegalArgumentException.class, () -> service.download("../../etc/passwd"));
    }

    @Test
    void 删除越权路径应被拒绝() {
        assertThrows(IllegalArgumentException.class, () -> service.delete("../secret"));
    }
}
