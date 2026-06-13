# 文件存储

`ycr-starter-storage` 提供统一文件存储抽象 `FileStorageService`，内置本地磁盘实现，可扩展对象存储。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-storage</artifactId>
</dependency>
```

## 配置

前缀 `ycr.storage`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.storage.enabled` | `true` | 是否启用 |
| `ycr.storage.type` | `local` | 存储类型（当前内置 `local`） |
| `ycr.storage.local.path` | `""` | 本地存储根目录 |
| `ycr.storage.local.url-prefix` | `/files` | 访问 URL 前缀 |

```yaml
ycr:
  storage:
    type: local
    local:
      path: /data/uploads
      url-prefix: /files
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
        FileInfo info = storage.upload(file.getInputStream(), file.getOriginalFilename());
        return R.ok(info);                 // originalFilename / filename / path / url / size / extension
    }

    @GetMapping("/download")
    public void download(String path, HttpServletResponse resp) throws IOException {
        try (InputStream in = storage.download(path)) { in.transferTo(resp.getOutputStream()); }
    }
}
```

接口方法：`upload(InputStream, originalFilename)` → `FileInfo`、`download(path)` → `InputStream`、`delete(path)`、`exists(path)`。

## 扩展其他存储

注册自己的 `FileStorageService` Bean（如 OSS/MinIO 实现）即可替换内置的 `LocalFileStorageService`。
