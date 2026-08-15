package com.ycr.framework.data.mp.handler.support;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ycr.framework.data.model.BaseDO;
import lombok.Getter;
import lombok.Setter;

/**
 * 审计字段集成测试实体。
 *
 * @author ycr
 */
@Getter
@Setter
@TableName("audit_entity")
public class AuditEntity extends BaseDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String name;
}
