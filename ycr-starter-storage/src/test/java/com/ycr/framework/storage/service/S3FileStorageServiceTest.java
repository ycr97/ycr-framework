package com.ycr.framework.storage.service;

import com.ycr.framework.storage.model.FileInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * S3 存储实现测试：以 Mock {@link S3Client} 验证存储键生成、FileInfo 映射与增删查行为，无需真实 S3。
 *
 * @author ycr
 */
class S3FileStorageServiceTest {

    private final S3Client s3Client = mock(S3Client.class);
    private final S3FileStorageService service =
            new S3FileStorageService(s3Client, "my-bucket", "https://cdn.example.com/");

    @Test
    @DisplayName("构造时bucket为空应抛异常")
    void shouldMatchExpectedBehavior001() {
        assertThrows(IllegalArgumentException.class, () -> new S3FileStorageService(s3Client, "  ", ""));
    }

    @Test
    @DisplayName("upload_生成日期键并映射FileInfo")
    void shouldMatchExpectedBehavior002() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        FileInfo info = service.upload(new ByteArrayInputStream(data), "photo.PNG");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("my-bucket", captor.getValue().bucket());
        // 键形如 yyyy/MM/dd/<32位hex>.png（扩展名转小写）
        assertTrue(captor.getValue().key().matches("\\d{4}/\\d{2}/\\d{2}/[0-9a-f]{32}\\.png"),
                "存储键格式不符: " + captor.getValue().key());

        assertEquals("photo.PNG", info.getOriginalFilename());
        assertEquals("png", info.getExtension());
        assertEquals(data.length, info.getSize());
        assertEquals(captor.getValue().key(), info.getPath());
        assertEquals("https://cdn.example.com/" + info.getPath(), info.getUrl());
    }

    @Test
    @DisplayName("upload_urlPrefix为空时url为空")
    void shouldMatchExpectedBehavior003() {
        S3FileStorageService noUrl = new S3FileStorageService(s3Client, "my-bucket", "");
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        FileInfo info = noUrl.upload(new ByteArrayInputStream(new byte[]{1, 2, 3}), "a.bin");
        assertEquals("", info.getUrl());
    }

    @Test
    @DisplayName("download_返回对象流")
    void shouldMatchExpectedBehavior004() throws Exception {
        byte[] data = "file-content".getBytes(StandardCharsets.UTF_8);
        ResponseInputStream<GetObjectResponse> ris = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                AbortableInputStream.create(new ByteArrayInputStream(data)));
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(ris);

        try (InputStream in = service.download("2026/06/15/abc.png")) {
            assertEquals("file-content", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    @DisplayName("exists_命中返回true_不存在返回false")
    void shouldMatchExpectedBehavior005() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().build());
        assertTrue(service.exists("k1"));

        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());
        assertFalse(service.exists("k2"));
    }

    @Test
    @DisplayName("delete_存在则删除返回true")
    void shouldMatchExpectedBehavior006() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().build());
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        assertTrue(service.delete("k1"));
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("delete_不存在返回false且不调用删除")
    void shouldMatchExpectedBehavior007() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        assertFalse(service.delete("missing"));
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }
}
