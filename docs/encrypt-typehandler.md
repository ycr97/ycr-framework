# 字段加密 MyBatis TypeHandler

`ycr-starter-encrypt` 提供 `EncryptTypeHandler`，用于 MyBatis/MyBatis-Plus 字段级透明加解密：写入数据库前加密，读取结果时解密。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-encrypt</artifactId>
</dependency>
```

业务项目使用 MyBatis-Plus 时继续引入自己的 MyBatis-Plus starter。

## 配置

```yaml
ycr:
  encrypt:
    enabled: true
    algorithm: AES
    current-key-id: key-2026-08
    legacy-key-id: key-2025-01
    keys:
      key-2025-01: ${YCR_ENCRYPT_KEY_2025}
      key-2026-08: ${YCR_ENCRYPT_KEY_2026}
```

配置说明：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.encrypt.enabled` | `false` | 是否启用加密 starter，须显式开启 |
| `ycr.encrypt.algorithm` | `AES` | 当前仅支持 AES；其他值启动失败 |
| `ycr.encrypt.current-key-id` | `default` | 新密文写入使用的 key-id |
| `ycr.encrypt.keys` | `{}` | key-id 到 AES 密钥的映射；密钥须为 16/24/32 UTF-8 字节 |
| `ycr.encrypt.legacy-key-id` | current-key-id | 历史无版本 AES-CBC 密文的只读解密密钥 |
| `ycr.encrypt.aes-key` | 空 | 兼容旧版单密钥配置；不得与 `keys` 同时配置 |

新写入密文采用版本化 envelope：

```text
ycr:v1:aes-gcm:<key-id>:<base64url-nonce>:<base64url-ciphertext-and-tag>
```

算法、版本和 key-id 所在的 envelope header 参与 GCM AAD 校验。密文或元数据被篡改、key-id 不存在、
密钥长度非法时立即失败。业务侧声明自定义 `EncryptHandler` 后，自动配置不会创建默认 handler。

## MyBatis-Plus 实体示例

```java
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ycr.framework.encrypt.typehandler.EncryptTypeHandler;

@TableName(value = "sys_user", autoResultMap = true)
public class UserDO {

    private Long id;

    @TableField(typeHandler = EncryptTypeHandler.class)
    private String phone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
```

`autoResultMap = true` 是 MyBatis-Plus 读取字段 TypeHandler 的关键配置；没有它，查询结果映射可能不会走字段上的 `typeHandler`。

## MyBatis XML 示例

```xml
<resultMap id="UserResultMap" type="com.example.UserDO">
    <id column="id" property="id"/>
    <result column="phone"
            property="phone"
            typeHandler="com.ycr.framework.encrypt.typehandler.EncryptTypeHandler"/>
</resultMap>

<insert id="insert">
    insert into sys_user(id, phone)
    values (
        #{id},
        #{phone,typeHandler=com.ycr.framework.encrypt.typehandler.EncryptTypeHandler}
    )
</insert>

<select id="selectById" resultMap="UserResultMap">
    select id, phone
    from sys_user
    where id = #{id}
</select>
```

## 注解 Mapper 示例

```java
import com.ycr.framework.encrypt.typehandler.EncryptTypeHandler;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

interface UserMapper {

    @Insert("""
            insert into sys_user(id, phone)
            values(#{id}, #{phone,typeHandler=com.ycr.framework.encrypt.typehandler.EncryptTypeHandler})
            """)
    void insert(UserDO user);

    @Select("select id, phone from sys_user where id = #{id}")
    @Results(id = "UserResultMap", value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "phone", property = "phone", typeHandler = EncryptTypeHandler.class)
    })
    UserDO selectById(Long id);
}
```

## 行为边界

- 仅处理显式绑定 `EncryptTypeHandler` 的 `String` 字段。
- Java 代码侧看到明文，数据库中保存密文。
- 数据库原值为 `null` 时，读取结果仍为 `null`。
- `ycr.encrypt.enabled=false` 时不会创建默认 `EncryptHandler`，使用 `EncryptTypeHandler` 会因 handler 未初始化而失败。
- 数据库已有明文字段不能直接切换读取解密，需要先做数据迁移。
- 旧版无前缀 AES-CBC 密文保留只读兼容，由 `legacy-key-id` 指定唯一密钥；框架不会尝试多把 CBC 密钥。

## 数据迁移建议

新字段直接启用 TypeHandler。已有明文字段建议按以下步骤迁移：

1. 将旧密钥保留在 `keys`，并把 `legacy-key-id` 指向旧 CBC 密钥。
2. 增加新密钥并切换 `current-key-id`；此后新写入均使用新 key-id。
3. 批量读取旧 CBC/旧 key-id 密文，通过当前 `EncryptHandler` 解密后重新加密写回。
4. 统计确认旧格式及旧 key-id 密文归零。
5. 先清空 `legacy-key-id`，再在后续发布移除旧密钥。

旧版单密钥配置仍可运行：

```yaml
ycr.encrypt.aes-key: ${YCR_ENCRYPT_KEY}
```

该模式使用 `default` key-id，适合兼容升级，不建议用于长期密钥轮换管理。
