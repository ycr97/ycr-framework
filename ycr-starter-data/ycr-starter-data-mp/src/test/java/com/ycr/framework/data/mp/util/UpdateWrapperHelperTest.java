package com.ycr.framework.data.mp.util;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UpdateWrapperHelper 构建更新包装器测试
 *
 * <p>纯单元测试场景下 MyBatis-Plus 尚未懒加载 TableInfo，断言均覆盖反射 + 驼峰转下划线兜底路径。</p>
 *
 * @author ycr
 */
class UpdateWrapperHelperTest {

    @Getter
    @Setter
    static class BaseEntity {
        private String updateTime;
        private String updateUser;
    }

    @Getter
    @Setter
    static class UserDO extends BaseEntity {
        @TableId("user_id")
        private Long id;

        @TableField("user_name")
        private String name;

        private Integer age;

        /** 未标注 @TableField 的驼峰字段，期望按 MyBatis-Plus 默认规则转 nick_name */
        private String nickName;
    }

    private UserDO sample() {
        UserDO user = new UserDO();
        user.setId(1L);
        user.setName("zhang");
        user.setAge(20);
        user.setNickName("xiaozhang");
        user.setUpdateTime("2026-06-20");
        user.setUpdateUser("admin");
        return user;
    }

    @Test
    @DisplayName("仅set变更字段并以主键作为条件")
    void shouldMatchExpectedBehavior001() {
        UpdateWrapper<UserDO> wrapper = UpdateWrapperHelper.build(Set.of("name"), sample());

        // set 子句只含变更字段映射的列名；主键列走 where 条件，不应出现在 set 子句
        String setSql = wrapper.getSqlSet();
        assertTrue(setSql.contains("user_name"));
        assertFalse(setSql.contains("user_id"));
        // 主键作为 where 条件
        assertTrue(wrapper.getSqlSegment().contains("user_id"));
    }

    @Test
    @DisplayName("字段名解析回退到字段本名")
    void shouldMatchExpectedBehavior002() {
        UpdateWrapper<UserDO> wrapper = UpdateWrapperHelper.build(Set.of("age"), sample());
        assertTrue(wrapper.getSqlSet().contains("age"));
    }

    @Test
    @DisplayName("未标注驼峰字段转下划线列名")
    void shouldMatchExpectedBehavior003() {
        UpdateWrapper<UserDO> wrapper = UpdateWrapperHelper.build(Set.of("nickName"), sample());
        String setSql = wrapper.getSqlSet();
        assertTrue(setSql.contains("nick_name"));
        assertFalse(setSql.contains("nickName"));
    }

    @Test
    @DisplayName("继承的审计字段也能进入set")
    void shouldMatchExpectedBehavior004() {
        UpdateWrapper<UserDO> wrapper = UpdateWrapperHelper.build(List.of("updateTime", "updateUser"), sample());
        String setSql = wrapper.getSqlSet();
        assertTrue(setSql.contains("update_time"));
        assertTrue(setSql.contains("update_user"));
    }

    @Test
    @DisplayName("多个变更字段都进入set")
    void shouldMatchExpectedBehavior005() {
        UpdateWrapper<UserDO> wrapper = UpdateWrapperHelper.build(List.of("name", "age"), sample());
        String setSql = wrapper.getSqlSet();
        assertTrue(setSql.contains("user_name"));
        assertTrue(setSql.contains("age"));
    }

    @Test
    @DisplayName("变更字段含主键时主键不进入set")
    void shouldMatchExpectedBehavior006() {
        UpdateWrapper<UserDO> wrapper = UpdateWrapperHelper.build(List.of("id", "name"), sample());
        String setSql = wrapper.getSqlSet();
        assertTrue(setSql.contains("user_name"));
        assertFalse(setSql.contains("user_id"));
        assertTrue(wrapper.getSqlSegment().contains("user_id"));
    }

    @Test
    @DisplayName("变更字段在实体中不存在时抛异常")
    void shouldMatchExpectedBehavior007() {
        assertThrows(IllegalArgumentException.class,
                () -> UpdateWrapperHelper.build(Set.of("notExistField"), sample()));
    }

    @Test
    @DisplayName("入参为空时抛异常")
    void shouldMatchExpectedBehavior008() {
        assertThrows(IllegalArgumentException.class, () -> UpdateWrapperHelper.build(null, sample()));
        assertThrows(IllegalArgumentException.class, () -> UpdateWrapperHelper.build(Set.of("name"), null));
        assertThrows(IllegalArgumentException.class, () -> UpdateWrapperHelper.build(Set.of(), sample()));
    }
}
