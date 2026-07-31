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
    aes-key: "1234567890abcdef"
```

配置说明：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.encrypt.enabled` | `false` | 是否启用加密 starter，须显式开启 |
| `ycr.encrypt.algorithm` | `AES` | 默认算法 |
| `ycr.encrypt.aes-key` | 空 | AES 密钥，长度必须是 16/24/32 字节 |

引入 starter 不会初始化加密生命周期。启用时必须提供合法 AES 密钥，或注册自定义 `EncryptHandler`；业务侧声明自定义 Bean 后，自动配置不会再创建默认 AES handler。

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
- 数据库已有明文字段不能直接切换读取解密，需要先做数据迁移或兼容解密策略。

## 数据迁移建议

新字段直接启用 TypeHandler。已有明文字段建议按以下步骤迁移：

1. 新增临时兼容读取逻辑或离线迁移脚本。
2. 批量读取明文，调用业务侧 `EncryptHandler.encrypt` 写回密文。
3. 验证数据库中目标字段不再出现明文样本。
4. 上线实体字段 `@TableField(typeHandler = EncryptTypeHandler.class)`。
5. 移除临时兼容逻辑。
