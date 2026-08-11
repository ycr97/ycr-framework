# 文件存储

`ycr-starter-storage` 提供统一文件存储抽象 `FileStorageService`，内置**本地磁盘**与 **S3 兼容**（AWS S3 / MinIO / 阿里云 OSS 等）两种实现，按 `ycr.storage.type` 切换。两种后端的存储键一致（`yyyy/MM/dd/<uuid>.ext`），可平滑迁移。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-storage</artifactId>
</dependency>
```

> 使用 S3 后端时**额外**引入 AWS S3 SDK（版本由 `ycr-framework-bom` 管理，无需写版本）：
> ```xml
> <dependency>
>     <groupId>software.amazon.awssdk</groupId>
>     <artifactId>s3</artifactId>
> </dependency>
> ```
> 该依赖在 starter 内为 `optional`，本地存储用户不会被牵连引入。

## 配置

前缀 `ycr.storage`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.storage.enabled` | `false` | 是否启用，须显式开启 |
| `ycr.storage.type` | `local` | 存储类型：`local` 或 `s3` |
| `ycr.storage.max-file-size` | `100MB` | 单文件最大允许大小 |

**本地（`type=local`）**

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.storage.local.path` | `""` | 本地存储根目录，启用本地后端时必填 |
| `ycr.storage.local.url-prefix` | `/files` | 访问 URL 前缀 |

```yaml
ycr:
  storage:
    enabled: true
    type: local
    local:
      path: /data/uploads
      url-prefix: /files
```

**S3 兼容（`type=s3`）**

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.storage.s3.endpoint` | `""` | 服务端点；AWS 留空，MinIO/OSS 等填如 `https://minio.example.com` |
| `ycr.storage.s3.region` | `us-east-1` | 区域；MinIO 任意非空即可 |
| `ycr.storage.s3.access-key` | `""` | 访问密钥 ID |
| `ycr.storage.s3.secret-key` | `""` | 访问密钥 |
| `ycr.storage.s3.bucket` | `""` | 桶名（必填） |
| `ycr.storage.s3.path-style-access` | `false` | 路径风格访问；**MinIO/自建网关需 `true`**，AWS 保持 `false` |
| `ycr.storage.s3.url-prefix` | `""` | 公网访问 URL 前缀；留空则 `FileInfo.url` 为空 |

```yaml
# AWS S3
ycr:
  storage:
    enabled: true
    type: s3
    s3:
      region: us-east-1
      access-key: ${S3_AK}
      secret-key: ${S3_SK}
      bucket: my-bucket
      url-prefix: https://my-bucket.s3.us-east-1.amazonaws.com
```

```yaml
# MinIO
ycr:
  storage:
    enabled: true
    type: s3
    s3:
      endpoint: https://minio.example.com
      region: us-east-1
      access-key: ${MINIO_AK}
      secret-key: ${MINIO_SK}
      bucket: my-bucket
      path-style-access: true
      url-prefix: https://minio.example.com/my-bucket
```

## 用法

注入 `FileStorageService`：

```java
@RequiredArgsConstructor
@RestController
public class FileController {
    private final FileStorageService storage;

    @PostMapping("/upload")
    public R<FileInfo> upload(MultipartFile file) throws IOException {
        FileInfo info = storage.upload(file.getInputStream(), file.getSize(), file.getOriginalFilename());
        return R.ok(info);                 // originalFilename / filename / path / url / size / extension
    }

    @GetMapping("/download")
    public void download(String path, HttpServletResponse resp) throws IOException {
        try (InputStream in = storage.download(path)) { in.transferTo(resp.getOutputStream()); }
    }
}
```

接口方法：`upload(InputStream, contentLength, originalFilename)` 是推荐形式，S3 会直接流式上传；兼容的 `upload(InputStream, originalFilename)` 在长度未知时先写入受限临时文件，不会把整个对象读入 JVM 堆。

## 扩展其他存储

注册自己的 `FileStorageService` Bean 即可替换内置实现（框架以 `@ConditionalOnMissingBean` 让位）。S3 实现已覆盖绝大多数对象存储（凡支持 S3 协议者）；如需 FastDFS 等非 S3 协议存储，实现该接口即可。
