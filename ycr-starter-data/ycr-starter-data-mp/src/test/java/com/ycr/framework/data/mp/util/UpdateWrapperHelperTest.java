package com.ycr.framework.data.mp.util;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UpdateWrapperHelper 构建更新包装器测试
 *
 * @author ycr
 */
class UpdateWrapperHelperTest {

    @Getter
    @Setter
    static class UserDO {
        @TableId("user_id")
        private Long id;

        @TableField("user_name")
        private String name;

        private Integer age;
    }

    private UserDO sample() {
        UserDO user = new UserDO();
        user.setId(1L);
        user.setName("zhang");
        user.setAge(20);
        return user;
    }

    @Test
    void 仅set变更字段并以主键作为条件() {
        UpdateWrapper<UserDO> wrapper = UpdateWrapperHelper.build(Set.of("name"), sample());

        // set 子句只含变更字段映射的列名；主键列走 where 条件，不应出现在 set 子句
        String setSql = wrapper.getSqlSet();
        assertTrue(setSql.contains("user_name"));
        assertFalse(setSql.contains("user_id"));
    }

    @Test
    void 字段名解析回退到字段本名() {
        UpdateWrapper<UserDO> wrapper = UpdateWrapperHelper.build(Set.of("age"), sample());
        assertTrue(wrapper.getSqlSet().contains("age"));
    }

    @Test
    void 多个变更字段都进入set() {
        UpdateWrapper<UserDO> wrapper = UpdateWrapperHelper.build(List.of("name", "age"), sample());
        String setSql = wrapper.getSqlSet();
        assertTrue(setSql.contains("user_name"));
        assertTrue(setSql.contains("age"));
    }

    @Test
    void 入参为空时抛异常() {
        assertThrows(IllegalArgumentException.class, () -> UpdateWrapperHelper.build(null, sample()));
        assertThrows(IllegalArgumentException.class, () -> UpdateWrapperHelper.build(Set.of("name"), null));
        assertThrows(IllegalArgumentException.class, () -> UpdateWrapperHelper.build(Set.of(), sample()));
    }
}
